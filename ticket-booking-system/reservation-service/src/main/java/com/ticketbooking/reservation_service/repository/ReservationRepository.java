package com.ticketbooking.reservation_service.repository;

import com.ticketbooking.reservation_service.models.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
