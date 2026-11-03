import urllib.request
import urllib.error
import json
import datetime
import random
import time
import sys

BASE_URL = "http://localhost:8080"

passed_tests = 0
failed_tests = 0

def report(name, success, details=""):
    global passed_tests, failed_tests
    if success:
        passed_tests += 1
        print(f"✅ PASS: {name} {details}")
    else:
        failed_tests += 1
        print(f"❌ FAIL: {name} {details}")

def request(url, method="GET", headers=None, body=None):
    if headers is None:
        headers = {}
    data = json.dumps(body).encode('utf-8') if body is not None else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as response:
            status = response.status
            resp_headers = dict(response.headers)
            content = response.read().decode('utf-8')
            try:
                parsed_body = json.loads(content) if content else None
            except:
                parsed_body = content
            return status, resp_headers, parsed_body
    except urllib.error.HTTPError as e:
        content = e.read().decode('utf-8')
        try:
            parsed_body = json.loads(content) if content else None
        except:
            parsed_body = content
        return e.code, dict(e.headers), parsed_body
    except Exception as e:
        return 0, {}, str(e)

print("=" * 60)
print("COMPREHENSIVE TEST SUITE - MULTI-TENANT & SERVICES ARCHITECTURE")
print("=" * 60)

# -------------------------------------------------------------
# 1. CORS TESTS (Gateway & Microservices)
# -------------------------------------------------------------
print("\n--- 1. Testing CORS Preflight (OPTIONS) & Headers ---")

cors_endpoints = [
    ("/api/auth/login", "Security Service Auth"),
    ("/api/barbers", "Barbers Service"),
    ("/api/services", "Services Catalog"),
    ("/api/branches", "Branches Endpoint"),
    ("/api/clients", "Clients Service"),
    ("/api/dates", "Dates Service"),
]

for endpoint, desc in cors_endpoints:
    url = f"{BASE_URL}{endpoint}"
    status, headers, body = request(
        url,
        method="OPTIONS",
        headers={
            "Origin": "http://localhost:4200",
            "Access-Control-Request-Method": "POST",
            "Access-Control-Request-Headers": "Authorization,Content-Type,X-Branch-Id"
        }
    )
    allow_origin = headers.get("Access-Control-Allow-Origin") or headers.get("access-control-allow-origin")
    has_cors = (status in [200, 204]) and (allow_origin is not None)
    report(f"CORS Preflight on {endpoint} ({desc})", has_cors, f"[status={status}, allow-origin={allow_origin}]")

# -------------------------------------------------------------
# 2. GATEWAY AUTHENTICATION & SECURITY ENFORCEMENT
# -------------------------------------------------------------
print("\n--- 2. Gateway Global JWT Security Filter ---")

for ep in ["/api/barbers", "/api/services", "/api/clients", "/api/dates"]:
    status, _, _ = request(f"{BASE_URL}{ep}", method="GET")
    report(f"Reject Unauthenticated GET {ep}", status == 401, f"[Expected 401, got {status}]")

for ep in ["/api/barbers", "/api/services", "/api/clients", "/api/dates"]:
    status, _, _ = request(f"{BASE_URL}{ep}", method="GET", headers={"Authorization": "Bearer invalid.fake.token"})
    report(f"Reject Invalid Token GET {ep}", status == 401, f"[Expected 401, got {status}]")

# Public endpoint /api/auth/login allows access without token
status, _, body = request(
    f"{BASE_URL}/api/auth/login",
    method="POST",
    headers={"Content-Type": "application/json", "X-Branch-Id": "centro"},
    body={"email": "admin@mishoo.com", "password": "adminpassword123"}
)
token = body.get("token") if isinstance(body, dict) else None
report("Public Route Login (admin@mishoo.com)", status == 200 and token is not None, f"[status={status}]")

auth_headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json",
    "X-Branch-Id": "centro"
}

# -------------------------------------------------------------
# 3. BRANCHES ENDPOINT
# -------------------------------------------------------------
print("\n--- 3. Branches / Sucursales Configuration ---")
status, _, branches = request(f"{BASE_URL}/api/branches", method="GET", headers=auth_headers)
has_branches = status == 200 and isinstance(branches, list) and len(branches) >= 3
report("GET /api/branches (centro, norte, sur)", has_branches, f"[status={status}, count={len(branches) if isinstance(branches, list) else 0}]")

# -------------------------------------------------------------
# 4. BARBERS & SERVICES ENTITY CRUD
# -------------------------------------------------------------
print("\n--- 4. Barbers & Services Entity CRUD ---")

# Barbers CRUD
barber_name = f"Barber{random.randint(1000, 9999)}"
barber_last = "Tester"
status, _, new_barber = request(
    f"{BASE_URL}/api/barbers",
    method="POST",
    headers=auth_headers,
    body={"name": barber_name, "lastName": barber_last}
)
new_barber_id = new_barber.get("id") if isinstance(new_barber, dict) else None
report(f"POST /api/barbers ({barber_name} {barber_last})", status == 201 and new_barber_id is not None, f"[status={status}, id={new_barber_id}]")

# Services Entity CRUD (Changed from Enum to DB Entity with name and description)
service_name = f"Corte Personalizado {random.randint(100, 999)}"
status, _, new_service = request(
    f"{BASE_URL}/api/services",
    method="POST",
    headers=auth_headers,
    body={
        "name": service_name,
        "description": "Corte de diseño exclusivo con acabado a navaja",
        "durationMinutes": 45,
        "price": 220.0
    }
)
new_service_id = new_service.get("id") if isinstance(new_service, dict) else None
report(f"POST /api/services (Entidad en BD: {service_name})", status == 201 and new_service_id is not None, f"[status={status}, id={new_service_id}]")

# GET service by id
status, _, service_found = request(f"{BASE_URL}/api/services/{new_service_id}", method="GET", headers=auth_headers)
report(f"GET /api/services/{new_service_id}", status == 200 and service_found.get("name") == service_name, f"[status={status}, desc={service_found.get('description') if isinstance(service_found, dict) else ''}]")

# -------------------------------------------------------------
# 5. CLIENTS SERVICE CRUD
# -------------------------------------------------------------
print("\n--- 5. Clients Service CRUD ---")

client_unique_name = f"Client{random.randint(10000, 99999)}"
client_phone = str(random.randint(2000000000, 9999999999))
status, _, new_client = request(
    f"{BASE_URL}/api/clients",
    method="POST",
    headers=auth_headers,
    body={"name": client_unique_name, "lastName": "VIP", "phone": client_phone}
)
new_client_id = new_client.get("id") if isinstance(new_client, dict) else None
report(f"POST /api/clients ({client_unique_name}, Phone {client_phone})", status == 201 and new_client_id is not None, f"[status={status}, id={new_client_id}]")

# -------------------------------------------------------------
# 6. DATES SERVICE (APPOINTMENTS) USING SERVICE ENTITY
# -------------------------------------------------------------
print("\n--- 6. Dates Service & Interservice Validation ---")

random_days = random.randint(2, 5)
appointment_time = (datetime.datetime.now() + datetime.timedelta(days=random_days)).replace(hour=10, minute=0, second=0, microsecond=0)
app_date_str = appointment_time.strftime("%Y-%m-%dT%H:%M:%S")

# Booking a valid appointment referencing service entity ID
status, _, new_date = request(
    f"{BASE_URL}/api/dates",
    method="POST",
    headers=auth_headers,
    body={
        "client": new_client_id,
        "barber": new_barber_id,
        "service": new_service_id,
        "status": "PENDING",
        "dateTime": app_date_str
    }
)
new_date_id = new_date.get("id") if isinstance(new_date, dict) else None
report(f"POST /api/dates with Service Entity #{new_service_id}", status == 201 and new_date_id is not None, f"[status={status}, date_id={new_date_id}]")

# -------------------------------------------------------------
# 7. MULTI-TENANT ISOLATION (EACH SUCURSAL HAS SEPARATE DB)
# -------------------------------------------------------------
print("\n--- 7. Multi-Tenant Branch Isolation ('Nada se comparte') ---")

# In 'centro', we have new_barber_id
status, _, barber_in_centro = request(f"{BASE_URL}/api/barbers/{new_barber_id}", method="GET", headers={"Authorization": f"Bearer {token}", "X-Branch-Id": "centro"})
report("Barber exists in Sucursal Centro", status == 200, f"[status={status}]")

# Querying 'norte' for the same ID must NOT find it because it's a completely distinct database
status, _, barber_in_norte = request(f"{BASE_URL}/api/barbers/{new_barber_id}", method="GET", headers={"Authorization": f"Bearer {token}", "X-Branch-Id": "norte"})
report("Barber from Centro does NOT exist in Sucursal Norte (Isolated DB)", status == 404, f"[status={status} (Expected 404)]")

# Querying 'sur' for the same ID must NOT find it
status, _, barber_in_sur = request(f"{BASE_URL}/api/barbers/{new_barber_id}", method="GET", headers={"Authorization": f"Bearer {token}", "X-Branch-Id": "sur"})
report("Barber from Centro does NOT exist in Sucursal Sur (Isolated DB)", status == 404, f"[status={status} (Expected 404)]")

# -------------------------------------------------------------
# SUMMARY
# -------------------------------------------------------------
print("\n" + "=" * 60)
print(f"RESULTS: {passed_tests} PASSED, {failed_tests} FAILED")
print("=" * 60)

if failed_tests > 0:
    sys.exit(1)
