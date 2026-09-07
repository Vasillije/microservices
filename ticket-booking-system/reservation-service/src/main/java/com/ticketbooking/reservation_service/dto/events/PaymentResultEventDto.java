package com.ticketbooking.reservation_service.dto.events;

public class PaymentResultEventDto {
    private String eventType; // "PAYMENT_SUCCESS" ili "PAYMENT_FAILED"
    private Long reservationId;
    private String transactionId;
    private String reason;

    public PaymentResultEventDto() {
    }

    public PaymentResultEventDto(String eventType, Long reservationId, String transactionId, String reason) {
        this.eventType = eventType;
        this.reservationId = reservationId;
        this.transactionId = transactionId;
        this.reason = reason;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}