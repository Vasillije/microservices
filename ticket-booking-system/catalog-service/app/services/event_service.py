from bson import ObjectId
from fastapi import HTTPException, status
from app.database import get_database  # <-- Uvozimo funkciju iz app.database


async def reserve_ticket_capacity(
    event_id: str, ticket_category_name: str, quantity: int
):
    # 1. Pozivamo funkciju da dobijemo konekciju sa bazom
    db = get_database()

    # 2. Provera validnosti ObjectId-ja
    if not ObjectId.is_valid(event_id):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Neispravan Event ID",
        )

    # 3. Atomska provera i smanjenje kapaciteta u MongoDB-ju
    result = await db.events.update_one(
        {
            "_id": ObjectId(event_id),
            "ticket_categories.name": ticket_category_name,
            "ticket_categories.available_quantity": {"$gte": quantity},
        },
        {"$inc": {"ticket_categories.$.available_quantity": -quantity}},
    )

    # 4. Provera ishoda operacije
    if result.modified_count == 0:
        event = await db.events.find_one({"_id": ObjectId(event_id)})
        if not event:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="Event nije pronađen",
            )

        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Nema dovoljno slobodnih karata ili izabrana kategorija ne postoji",
        )

    return {"message": "Kapacitet uspešno ažuriran"}



async def release_ticket_capacity(event_id: str, ticket_category_name: str, quantity: int):
    db = get_database()

    if not ObjectId.is_valid(event_id):
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Neispravan Event ID")

    # Uvećavamo available_quantity za vraćenu količinu (+quantity)
    result = await db.events.update_one(
        {
            "_id": ObjectId(event_id),
            "ticket_categories.name": ticket_category_name
        },
        {
            "$inc": {"ticket_categories.$.available_quantity": quantity}
        }
    )

    if result.modified_count == 0:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Event ili kategorija nisu pronađeni")

    return {"message": "Kapacitet uspešno vraćen"}