from datetime import datetime

from pydantic import BaseModel, Field


class TicketCategory(BaseModel):
    name: str
    price: float
    available_quantity: int = Field(ge=0)


class Event(BaseModel):
    title: str
    description: str
    category: str
    venue: str
    city: str
    date: datetime
    ticket_categories: list[TicketCategory]
    active: bool = True


class EventOut(Event):
    id: str

class ReserveTicketDTO(BaseModel):
    event_id: str = Field(..., alias="eventId")
    ticket_category_name: str = Field(..., alias="ticketCategory")
    quantity: int

    class Config:
        populate_by_name = True