from fastapi import APIRouter, HTTPException, status

from app.database import get_database
from app.models.event import Event, EventOut, ReserveTicketDTO

from app.services.event_service import reserve_ticket_capacity, release_ticket_capacity

router = APIRouter(prefix="/api/events", tags=["events"])

COLLECTION = "events"


def _to_event_out(document: dict) -> EventOut:
    return EventOut(id=str(document["_id"]), **{k: v for k, v in document.items() if k != "_id"})


@router.post("", response_model=EventOut, status_code=status.HTTP_201_CREATED)
async def create_event(event: Event) -> EventOut:
    db = get_database()
    result = await db[COLLECTION].insert_one(event.model_dump())
    created = await db[COLLECTION].find_one({"_id": result.inserted_id})
    if created is None:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Event was inserted but could not be read back",
        )
    return _to_event_out(created)


@router.get("", response_model=list[EventOut])
async def list_events() -> list[EventOut]:
    db = get_database()
    documents = await db[COLLECTION].find().to_list(length=None)
    return [_to_event_out(document) for document in documents]


@router.post("/reserve", status_code=status.HTTP_200_OK)
async def reserve_tickets(dto: ReserveTicketDTO):
    return await reserve_ticket_capacity(
        event_id=dto.event_id,
        ticket_category_name=dto.ticket_category_name,
        quantity=dto.quantity
    )

@router.post("/release", status_code=status.HTTP_200_OK)
async def release_tickets(dto: ReserveTicketDTO):
    return await release_ticket_capacity(
        event_id=dto.event_id,
        ticket_category_name=dto.ticket_category_name,
        quantity=dto.quantity
    )