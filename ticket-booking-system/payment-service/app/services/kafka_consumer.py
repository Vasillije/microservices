import asyncio
import json
import logging
import random
from aiokafka import AIOKafkaConsumer, AIOKafkaProducer
from app.schemas.payment import ReservationEventDTO, PaymentResultEventDTO

logging.basicConfig(level=logging.INFO)

KAFKA_BOOTSTRAP_SERVERS = "localhost:9092"
CONSUME_TOPIC = "reservation-events"
PRODUCE_TOPIC = "payment-events"

async def process_payment_logic(event: ReservationEventDTO) -> PaymentResultEventDTO:
    logging.info(f"Obrada placanja za Rezervaciju ID: {event.reservationId} | Iznos: {event.amount} EUR")
    
    # Simulacija mreznog kasnjenja banke (1.5 sekundi)
    await asyncio.sleep(1.5)

    # Simuliramo uspeh (80% sanse uspeh, 20% neuspeh radi testiranja SAGA kompenzacije)
    if random.random() < 0.2:
        logging.warning(f"NEUSPESNO placanje za rezervaciju ID: {event.reservationId}")
        return PaymentResultEventDTO(
            eventType="PAYMENT_FAILED",
            reservationId=event.reservationId,
            reason="Nedovoljno sredstava na kartici."
        )
    else:
        logging.info(f"USPESNO placanje za rezervaciju ID: {event.reservationId}")
        return PaymentResultEventDTO(
            eventType="PAYMENT_SUCCESS",
            reservationId=event.reservationId,
            transactionId=f"TXN-{random.randint(100000, 999999)}"
        )

async def start_kafka_listener():
    consumer = AIOKafkaConsumer(
        CONSUME_TOPIC,
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        group_id="payment-service-group",
        value_deserializer=lambda v: json.loads(v.decode("utf-8")),
        auto_offset_reset="earliest"
    )

    producer = AIOKafkaProducer(
        bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
        value_serializer=lambda v: json.dumps(v.model_dump()).encode("utf-8")
    )

    await consumer.start()
    await producer.start()
    logging.info("Payment Kafka Consumer & Producer su pokrenuti...")

    try:
        async for msg in consumer:
            logging.info(f"Primljena Kafka poruka: {msg.value}")
            try:
                event_dto = ReservationEventDTO(**msg.value)
                result_dto = await process_payment_logic(event_dto)
                
                # Slanje rezultata nazad na Kafku
                await producer.send_and_wait(PRODUCE_TOPIC, result_dto)
                logging.info(f"📤 Poslat ishod na topic '{PRODUCE_TOPIC}': {result_dto.model_dump()}")
            except Exception as e:
                logging.error(f"Greška pri obradi poruke: {e}")
    finally:
        await consumer.stop()
        await producer.stop()