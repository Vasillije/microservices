package com.ticketbooking.reservation_service.service.impl;

import com.ticketbooking.reservation_service.dto.events.ReservationCreatedEventDto;
import com.ticketbooking.reservation_service.dto.reservations.CreateReservationDto;
import com.ticketbooking.reservation_service.dto.reservations.FastApiReserveRequestDto;
import com.ticketbooking.reservation_service.dto.reservations.UpdateReservationDto;
import com.ticketbooking.reservation_service.kafka.ReservationKafkaProducer;
import com.ticketbooking.reservation_service.models.Reservation;
import com.ticketbooking.reservation_service.repository.ReservationRepository;
import com.ticketbooking.reservation_service.service.ReservationService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ReservationServiceImpl implements ReservationService {

    private ReservationRepository reservationRepository;

    private ReservationKafkaProducer kafkaProducer;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedissonClient redissonClient;

    private final String FASTAPI_URL = "http://localhost:8000/api/events/reserve";

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository, ReservationKafkaProducer kafkaProducer){
        this.reservationRepository = reservationRepository;
        this.kafkaProducer = kafkaProducer;
    }

    @Override
    public Optional<Reservation> getReservationById(Long id) {
        return reservationRepository.findById(id);
    }

    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }



    @Override
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rezervacija sa ID-em " + id + " nije pronađena."));

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new RuntimeException("Rezervacija je već otkazana.");
        }

        reservation.setStatus("CANCELLED");

        reservationRepository.save(reservation);
    }


    @Override
    public Reservation createReservation(CreateReservationDto dto) {
        // Ključ zaključavanja vezujemo za eventId
        String lockKey = "lock:event:" + dto.getEventId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Čekamo najviše 5s da dobijemo lock, automatski se oslobađa posle 10s
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Sistem je trenutno zauzet. Pokušajte ponovo.");
            }

            // --- KRITIČNA ZONA (Zaključano za isti eventId) ---
            FastApiReserveRequestDto fastApiDto = new FastApiReserveRequestDto(
                    dto.getEventId(),
                    dto.getTicketCategory(),
                    dto.getQuantity()
            );

            try {
                restTemplate.postForEntity(FASTAPI_URL, fastApiDto, String.class);
            } catch (HttpClientErrorException e) {
                throw new RuntimeException("Rezervacija neuspešna: " + e.getResponseBodyAsString());
            }

            Reservation reservation = new Reservation();
            reservation.setEventId(dto.getEventId());
            reservation.setUserId(dto.getUserId());
            reservation.setTicketCategory(dto.getTicketCategory());
            reservation.setQuantity(dto.getQuantity());
            reservation.setStatus("PENDING");
            reservation.setCreatedAt(LocalDateTime.now());

            Reservation savedReservation = reservationRepository.save(reservation);

            //Kreiranje DTO-a i slanje dogadjaja na Kafku
            Double totalAmount = dto.getQuantity() * 50.0; // Izračunaj cenu ili stavi odgovarajući iznos

            ReservationCreatedEventDto eventDto = new ReservationCreatedEventDto(
                    savedReservation.getId(),
                    savedReservation.getUserId(),
                    savedReservation.getEventId(),
                    savedReservation.getTicketCategory(),
                    savedReservation.getQuantity(),
                    totalAmount
            );

            kafkaProducer.sendReservationCreatedEvent(eventDto);

            return savedReservation;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Nit je prekinuta.");
        } finally {
            // Uvek oslobađamo lock u finally bloku
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

    }

    @Override
    public Reservation updateReservation(UpdateReservationDto dto) {
        Optional<Reservation> reservationOptional = reservationRepository.findById(dto.getReservationId());
        if (!reservationOptional.isPresent()) {
            throw new RuntimeException("Rezervacija sa ID-em " + dto.getReservationId() + " nije pronađena.");
        }

        Reservation reservation = reservationOptional.get();
        String lockKey = "lock:event:" + reservation.getEventId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new RuntimeException("Sistem je zauzet. Pokušajte ponovo.");
            }

            // --- KRITIČNA ZONA ---
            if (dto.getQuantity() != null && !dto.getQuantity().equals(reservation.getQuantity())) {
                int oldQuantity = reservation.getQuantity();
                int newQuantity = dto.getQuantity();
                int diff = newQuantity - oldQuantity;

                try {
                    if (diff > 0) {
                        FastApiReserveRequestDto fastApiDto = new FastApiReserveRequestDto(
                                reservation.getEventId(),
                                reservation.getTicketCategory(),
                                diff
                        );
                        restTemplate.postForEntity("http://localhost:8000/api/events/reserve", fastApiDto, String.class);
                    } else if (diff < 0) {
                        FastApiReserveRequestDto fastApiDto = new FastApiReserveRequestDto(
                                reservation.getEventId(),
                                reservation.getTicketCategory(),
                                -diff
                        );
                        restTemplate.postForEntity("http://localhost:8000/api/events/release", fastApiDto, String.class);
                    }
                } catch (RestClientException e) {
                    throw new RuntimeException("Greška pri komunikaciji sa Catalog servisom (FastAPI): " + e.getMessage());
                }

                reservation.setQuantity(newQuantity);
            }

            if (dto.getStatus() != null) {
                reservation.setStatus(dto.getStatus());
            }

            return reservationRepository.save(reservation);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Nit je prekinuta.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
