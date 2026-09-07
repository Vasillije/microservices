package com.ticketbooking.reservation_service.controller;

import com.ticketbooking.reservation_service.dto.reservations.CreateReservationDto;
import com.ticketbooking.reservation_service.dto.reservations.UpdateReservationDto;
import com.ticketbooking.reservation_service.models.Reservation;
import com.ticketbooking.reservation_service.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservation")
public class ReservationController {
    private ReservationService reservationService;

    @Autowired
    public ReservationController(ReservationService reservationService){
        this.reservationService = reservationService;
    }

    @GetMapping("getReservationById/{id}")
    public ResponseEntity<?> getReservationById(@PathVariable Long id){
        try{
            return new ResponseEntity<Optional<Reservation>>(this.reservationService.getReservationById(id), HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("getAllReservations")
    public ResponseEntity<?> getAllReservations(){
        try{
            return new ResponseEntity<List<Reservation>>(this.reservationService.getAllReservations(), HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("updateReservation")
    public ResponseEntity<?> updateReservation(@RequestBody UpdateReservationDto dto){
        try{
            return new ResponseEntity<Reservation>(this.reservationService.updateReservation(dto), HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("deleteReservation/{id}")
    public ResponseEntity<?> deleteReservation(@PathVariable Long id){
        try{
            this.reservationService.deleteReservation(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("createReservation")
    public ResponseEntity<?> createReservation(@RequestBody CreateReservationDto dto){
        try{
            return new ResponseEntity<Reservation>(this.reservationService.createReservation(dto), HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
