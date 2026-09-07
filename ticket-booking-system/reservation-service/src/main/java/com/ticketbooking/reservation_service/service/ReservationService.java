package com.ticketbooking.reservation_service.service;

import com.ticketbooking.reservation_service.dto.reservations.CreateReservationDto;
import com.ticketbooking.reservation_service.dto.reservations.UpdateReservationDto;
import com.ticketbooking.reservation_service.models.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationService {
    public Optional<Reservation> getReservationById(Long id);
    public List<Reservation> getAllReservations();
    public Reservation updateReservation(UpdateReservationDto dto);
    public void deleteReservation(Long id);
    public Reservation createReservation(CreateReservationDto dto);

}
