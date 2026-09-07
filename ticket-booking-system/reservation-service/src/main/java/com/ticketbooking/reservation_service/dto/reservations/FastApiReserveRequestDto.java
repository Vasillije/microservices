package com.ticketbooking.reservation_service.dto.reservations;

public class FastApiReserveRequestDto {
    private String event_id;
    private String ticket_category_name;
    private Integer quantity;

    public FastApiReserveRequestDto() {}

    public FastApiReserveRequestDto(String event_id, String ticket_category_name, Integer quantity) {
        this.event_id = event_id;
        this.ticket_category_name = ticket_category_name;
        this.quantity = quantity;
    }

    public String getEvent_id() { return event_id; }
    public void setEvent_id(String event_id) { this.event_id = event_id; }

    public String getTicket_category_name() { return ticket_category_name; }
    public void setTicket_category_name(String ticket_category_name) { this.ticket_category_name = ticket_category_name; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
