from fastapi import FastAPI, Request, Response
import httpx

app = FastAPI(title="API Gateway")

# HTTP klijent za prosleđivanje zahteva
client = httpx.AsyncClient()

# SVI tvoji servisi se mapiraju ovde (ime u mapi : naziv Docker servisa i njegov unutrašnji port)
SERVICES = {
    "catalog": "http://catalog-service:8000",
    "reservations": "http://reservation-service:8081",
    "payments": "http://payment-service:8002",
}

@app.api_route("/api/v1/{service_name}/{path:path}", methods=["GET", "POST", "PUT", "DELETE", "PATCH"])
async def gateway_proxy(service_name: str, path: str, request: Request):
    # 1. Provera da li zahtevani servis postoji u mapi
    if service_name not in SERVICES:
        return Response(content='{"error": "Service not found"}', status_code=404, media_type="application/json")

    # 2. Pravljenje ciljne adrese (npr. http://catalog-service:8000/tickets)
    target_url = f"{SERVICES[service_name]}/{path}"

    # 3. Preuzimanje tela zahteva, hedera i query parametara
    body = await request.body()
    headers = dict(request.headers)
    headers.pop("host", None)  # Uklanjamo originalni host header radi izbegavanja konflikata

    # 4. Konstruisanje i slanje zahteva unutrašnjem servisu
    req = client.build_request(
        method=request.method,
        url=target_url,
        headers=headers,
        content=body,
        params=request.query_params
    )
    
    response = await client.send(req)
    
    # 5. Vraćanje odgovora klijentu
    return Response(
        content=response.content,
        status_code=response.status_code,
        headers=dict(response.headers)
    )