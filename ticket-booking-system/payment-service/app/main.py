import asyncio
from fastapi import FastAPI
from app.services.kafka_consumer import start_kafka_listener

app = FastAPI(title="Payment Service", version="1.0.0")

@app.on_event("startup")
async def startup_event():
    # Pokrecemo Kafka slusaca u pozadinskom zadatku (async task)
    asyncio.create_task(start_kafka_listener())

@app.get("/")
def read_root():
    return {"service": "Payment Service", "status": "running"}

@app.get("/health")
def health_check():
    return {"status": "UP"}