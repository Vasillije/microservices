package com.ticketbooking.reservation_service.dto.events;

public class ReservationCreatedEventDto {
    private Long reservationId;
    private String userId;
    private String eventId;
    private String ticketCategory;
    private Integer quantity;
    private Double amount;

    public ReservationCreatedEventDto() {
    }

    public ReservationCreatedEventDto(Long reservationId, String userId, String eventId, String ticketCategory, Integer quantity, Double amount) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.eventId = eventId;
        this.ticketCategory = ticketCategory;
        this.quantity = quantity;
        this.amount = amount;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(String ticketCategory) {
        this.ticketCategory = ticketCategory;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}