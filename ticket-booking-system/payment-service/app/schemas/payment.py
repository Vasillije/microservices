from pydantic import BaseModel
from typing import Optional

class ReservationEventDTO(BaseModel):
    reservationId: int
    userId: Optional[str] = "user_1"
    eventId: str
    ticketCategory: str
    quantity: int
    amount: float = 100.0

class PaymentResultEventDTO(BaseModel):
    eventType: str  # "PAYMENT_SUCCESS" ili "PAYMENT_FAILED"
    reservationId: int
    transactionId: Optional[str] = None
    reason: Optional[str] = None
