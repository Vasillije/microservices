package com.ticketbooking.reservation_service.kafka;

import com.ticketbooking.reservation_service.dto.events.PaymentResultEventDto;
import com.ticketbooking.reservation_service.models.Reservation;
import com.ticketbooking.reservation_service.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ReservationKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationKafkaListener.class);

    // URL tvog FastAPI Catalog servisa
    private static final String FASTAPI_RELEASE_URL = "http://localhost:8000/release";

    private final ReservationRepository reservationRepository;
    private final RestTemplate restTemplate;

    public ReservationKafkaListener(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
        this.restTemplate = new RestTemplate();
    }

    @KafkaListener(topics = "payment-events", groupId = "reservation-service-group")
    @Transactional
    public void handlePaymentResult(PaymentResultEventDto event) {
        log.info("Stigao PaymentResultEvent sa Kafke za Rezervaciju ID={}: {}", event.getReservationId(), event.getEventType());

        Reservation reservation = reservationRepository.findById(event.getReservationId())
                .orElseThrow(() -> new RuntimeException("Rezervacija nije pronadjena sa ID: " + event.getReservationId()));

        if ("PAYMENT_SUCCESS".equalsIgnoreCase(event.getEventType())) {
            reservation.setStatus("CONFIRMED");
            reservationRepository.save(reservation);
            log.info("SAGA uspeh: Rezervacija ID={} je uspesno POTVRĐENA.", reservation.getId());

        } else if ("PAYMENT_FAILED".equalsIgnoreCase(event.getEventType())) {
            // 1. Otkazujemo rezervaciju u PostgreSQL bazi
            reservation.setStatus("CANCELLED");
            reservationRepository.save(reservation);

            // 2. SAGA Kompenzacija: Pozivamo FastAPI /release da vrati zauzete karte
            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("eventId", reservation.getEventId());
                requestBody.put("ticketCategory", reservation.getTicketCategory());
                requestBody.put("quantity", reservation.getQuantity());

                restTemplate.postForEntity(FASTAPI_RELEASE_URL, requestBody, String.class);
                log.warn("SAGA Kompenzacija: Placanje nije uspelo! Rezervacija ID={} je OTKAZANA i karte su vracene u Catalog.", reservation.getId());
            } catch (Exception e) {
                log.error("Greska prilikom pozivanja /release kompenzacije na Catalog servisu: {}", e.getMessage());
            }
        }
    }
}