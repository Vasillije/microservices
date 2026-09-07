from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.database import close_mongo_connection, connect_to_mongo
from app.routers.events import router as events_router


@asynccontextmanager
async def lifespan(_app: FastAPI):
    await connect_to_mongo()
    yield
    await close_mongo_connection()


app = FastAPI(title="Catalog Service", lifespan=lifespan)
app.include_router(events_router)


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok"}
