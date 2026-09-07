package com.ticketbooking.reservation_service.kafka;

import com.ticketbooking.reservation_service.dto.events.ReservationCreatedEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ReservationKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(ReservationKafkaProducer.class);
    private static final String TOPIC = "reservation-events";

    private final KafkaTemplate<String, ReservationCreatedEventDto> kafkaTemplate;

    public ReservationKafkaProducer(KafkaTemplate<String, ReservationCreatedEventDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendReservationCreatedEvent(ReservationCreatedEventDto event) {
        log.info("Saljem ReservationCreatedEvent na Kafka topic '{}' za Rezervaciju ID: {}", TOPIC, event.getReservationId());
        this.kafkaTemplate.send(TOPIC, String.valueOf(event.getReservationId()), event);
    }
}