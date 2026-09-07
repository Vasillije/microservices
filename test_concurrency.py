import concurrent.futures
import requests
import json

# --- KONFIGURACIJA ---
BASE_URL = "http://localhost:8081"  # Port tvog Spring Boot servisa

# ZAMENI SA STVARNIM VREDNOSTIMA IZ BAZA:
EVENT_ID = "6a8ee13e7c7348f999de5b33"          # Mongo ID događaja
EXISTING_RESERVATION_ID = "1" # UUID ili ID postojeće rezervacije za Update test

# Zahtev za KREIRANJE nove rezervacije (+1 karta)
create_payload = {
    "eventId": EVENT_ID,
    "userId": "user_create_test",
    "ticketCategory": "VIP",
    "quantity": 1
}

# Zahtev za AŽURIRANJE postojeće rezervacije (+2 karte)
update_payload = {
    "reservationId": EXISTING_RESERVATION_ID,
    "quantity": 3  # Pretpostavljamo da je stara količina bila 1, pa je diff = +2
}

def send_create_request(request_num):
    url = f"{BASE_URL}/api/reservation/createReservation"
    try:
        response = requests.post(url, json=create_payload, timeout=10)
        return f"[CREATE #{request_num}] Status: {response.status_code} | Response: {response.text}"
    except Exception as e:
        return f"[CREATE #{request_num}] Error: {str(e)}"

def send_update_request(request_num):
    url = f"{BASE_URL}/api/reservation/updateReservation"
    try:
        response = requests.put(url, json=update_payload, timeout=10)
        return f"[UPDATE #{request_num}] Status: {response.status_code} | Response: {response.text}"
    except Exception as e:
        return f"[UPDATE #{request_num}] Error: {str(e)}"

def main():
    print("🚀 Pokrećem kombinovani test konkurentnosti (CREATE + UPDATE)...")
    print("=" * 60)
    
    tasks = []
    
    # Pripremamo 10 simultanih CREATE zahteva i 10 simultanih UPDATE zahteva
    with concurrent.futures.ThreadPoolExecutor(max_workers=20) as executor:
        for i in range(1, 11):
            tasks.append(executor.submit(send_create_request, i))
            tasks.append(executor.submit(send_update_request, i))
        
        # Prikaz rezultata kako pristižu
        for future in concurrent.futures.as_completed(tasks):
            print(future.result())

    print("=" * 60)
    print("🏁 Test završen! Proveri stanje u MongoDB-ju i PostgreSQL-u.")

if __name__ == "__main__":
    main()