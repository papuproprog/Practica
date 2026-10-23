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
print("COMPREHENSIVE TEST SUITE - CORS & MICROSERVICES ARCHITECTURE")
print("=" * 60)

# -------------------------------------------------------------
# 1. CORS TESTS (Gateway & Microservices)
# -------------------------------------------------------------
print("\n--- 1. Testing CORS Preflight (OPTIONS) & Headers ---")

cors_endpoints = [
    ("/api/auth/login", "Security Service Auth"),
    ("/api/barbers", "Barbers Service"),
    ("/api/clients", "Clients Service"),
    ("/api/dates", "Dates Service"),
]

for endpoint, desc in cors_endpoints:
    url = f"{BASE_URL}{endpoint}"
    status, headers, body = request(
        url,
        method="OPTIONS",
        headers={
            "Origin": "http://localhost:3000",
            "Access-Control-Request-Method": "POST",
            "Access-Control-Request-Headers": "Authorization,Content-Type"
        }
    )
    # Checking CORS headers
    allow_origin = headers.get("Access-Control-Allow-Origin") or headers.get("access-control-allow-origin")
    allow_methods = headers.get("Access-Control-Allow-Methods") or headers.get("access-control-allow-methods")
    
    has_cors = (status in [200, 204]) and (allow_origin is not None)
    report(f"CORS Preflight on {endpoint} ({desc})", has_cors, f"[status={status}, allow-origin={allow_origin}]")

# -------------------------------------------------------------
# 2. GATEWAY AUTHENTICATION & SECURITY ENFORCEMENT
# -------------------------------------------------------------
print("\n--- 2. Gateway Global JWT Security Filter ---")

# Accessing protected endpoints without token must return 401
for ep in ["/api/barbers", "/api/clients", "/api/dates"]:
    status, _, _ = request(f"{BASE_URL}{ep}", method="GET")
    report(f"Reject Unauthenticated GET {ep}", status == 401, f"[Expected 401, got {status}]")

# Accessing protected endpoints with fake token must return 401
for ep in ["/api/barbers", "/api/clients", "/api/dates"]:
    status, _, _ = request(f"{BASE_URL}{ep}", method="GET", headers={"Authorization": "Bearer invalid.fake.token"})
    report(f"Reject Invalid Token GET {ep}", status == 401, f"[Expected 401, got {status}]")

# Public endpoint /api/auth/login allows access without token
status, _, body = request(
    f"{BASE_URL}/api/auth/login",
    method="POST",
    headers={"Content-Type": "application/json"},
    body={"email": "admin@mishoo.com", "password": "adminpassword123"}
)
token = body.get("token") if isinstance(body, dict) else None
report("Public Route Login (admin@mishoo.com)", status == 200 and token is not None, f"[status={status}]")

# -------------------------------------------------------------
# 3. SECURITY SERVICE VALIDATION & ERROR HANDLING
# -------------------------------------------------------------
print("\n--- 3. Security Service Features & Exceptions ---")

# Wrong password -> 401 Unauthorized
status, _, err_body = request(
    f"{BASE_URL}/api/auth/login",
    method="POST",
    headers={"Content-Type": "application/json"},
    body={"email": "admin@mishoo.com", "password": "wrongpassword"}
)
report("Invalid Password Handling", status == 401, f"[status={status}, error={err_body}]")

# Register with invalid/empty fields -> 400 Bad Request
status, _, err_body = request(
    f"{BASE_URL}/api/auth/register",
    method="POST",
    headers={"Content-Type": "application/json"},
    body={"name": "", "email": "not-an-email", "password": ""}
)
report("Validation Error on Register", status == 400, f"[status={status}, errors={err_body}]")

# Register a unique admin user
unique_id = f"{int(time.time() * 1000)}_{random.randint(100, 999)}"
new_email = f"admin_{unique_id}@mishoo.com"
status, _, reg_body = request(
    f"{BASE_URL}/api/auth/register",
    method="POST",
    headers={"Content-Type": "application/json"},
    body={"name": "Admin Test", "email": new_email, "password": "Password123!"}
)
report(f"Register New Admin ({new_email})", status == 201 and "id" in reg_body, f"[status={status}, id={reg_body.get('id') if isinstance(reg_body, dict) else None}]")

# Duplicate email registration -> 409 Conflict
status, _, err_body = request(
    f"{BASE_URL}/api/auth/register",
    method="POST",
    headers={"Content-Type": "application/json"},
    body={"name": "Admin Duplicate", "email": new_email, "password": "Password123!"}
)
report("Duplicate Email Conflict (409)", status == 409, f"[status={status}, error={err_body}]")

# List users with Bearer token
auth_headers = {
    "Authorization": f"Bearer {token}",
    "Content-Type": "application/json"
}
status, _, users = request(f"{BASE_URL}/api/auth/users", method="GET", headers=auth_headers)
report("Get Users List with Token", status == 200 and isinstance(users, list), f"[status={status}, count={len(users) if isinstance(users, list) else 0}]")

# -------------------------------------------------------------
# 4. BARBERS SERVICE CRUD & GLOBAL EXCEPTIONS
# -------------------------------------------------------------
print("\n--- 4. Barbers Service CRUD & Exceptions ---")

# GET all barbers
status, _, barbers_list = request(f"{BASE_URL}/api/barbers", method="GET", headers=auth_headers)
report("GET /api/barbers", status == 200 and isinstance(barbers_list, list), f"[status={status}, count={len(barbers_list) if isinstance(barbers_list, list) else 0}]")

# POST create new barber
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

# GET barber by id
status, _, barber_found = request(f"{BASE_URL}/api/barbers/{new_barber_id}", method="GET", headers=auth_headers)
report(f"GET /api/barbers/{new_barber_id}", status == 200 and barber_found.get("name") == barber_name, f"[status={status}]")

# PUT update barber
updated_last = "Master"
status, _, barber_updated = request(
    f"{BASE_URL}/api/barbers/{new_barber_id}",
    method="PUT",
    headers=auth_headers,
    body={"name": barber_name, "lastName": updated_last}
)
report(f"PUT /api/barbers/{new_barber_id}", status == 200 and barber_updated.get("lastName") == updated_last, f"[status={status}]")

# GET non-existent barber -> 404
status, _, err_body = request(f"{BASE_URL}/api/barbers/999999", method="GET", headers=auth_headers)
report("GET non-existent barber (404)", status == 404, f"[status={status}, error={err_body}]")

# -------------------------------------------------------------
# 5. CLIENTS SERVICE CRUD & GLOBAL EXCEPTIONS
# -------------------------------------------------------------
print("\n--- 5. Clients Service CRUD & Exceptions ---")

# GET all clients
status, _, clients_list = request(f"{BASE_URL}/api/clients", method="GET", headers=auth_headers)
report("GET /api/clients", status == 200 and isinstance(clients_list, list), f"[status={status}, count={len(clients_list) if isinstance(clients_list, list) else 0}]")

# POST create new client with guaranteed unique name and 10-digit phone
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

# GET client by ID
status, _, client_found = request(f"{BASE_URL}/api/clients/{new_client_id}", method="GET", headers=auth_headers)
report(f"GET /api/clients/{new_client_id}", status == 200 and client_found.get("phone") == client_phone, f"[status={status}]")

# Duplicate client phone -> 409 Conflict
status, _, err_body = request(
    f"{BASE_URL}/api/clients",
    method="POST",
    headers=auth_headers,
    body={"name": f"Other{random.randint(10000, 99999)}", "lastName": "Test", "phone": client_phone}
)
report("Duplicate Phone Conflict (409)", status == 409, f"[status={status}, error={err_body}]")

# -------------------------------------------------------------
# 6. DATES SERVICE & INTERSERVICE VALIDATION (EUREKA CLIENTS)
# -------------------------------------------------------------
print("\n--- 6. Dates Service & Interservice Communication ---")

# Valid appointment date: tomorrow at 10:00 AM (Within business hours 08:00 - 20:00)
# Use a random day ahead (between 2 and 5 days) so slots are clean
random_days = random.randint(2, 5)
appointment_time = (datetime.datetime.now() + datetime.timedelta(days=random_days)).replace(hour=10, minute=0, second=0, microsecond=0)
app_date_str = appointment_time.strftime("%Y-%m-%dT%H:%M:%S")

# Booking a valid appointment (uses BarbersServiceClient & ClientsServiceClient via Eureka)
status, _, new_date = request(
    f"{BASE_URL}/api/dates",
    method="POST",
    headers=auth_headers,
    body={
        "client": new_client_id,
        "barber": new_barber_id,
        "service": "HAIRCUT",
        "status": "PENDING",
        "dateTime": app_date_str
    }
)
new_date_id = new_date.get("id") if isinstance(new_date, dict) else None
report(f"POST /api/dates (Client {new_client_id} + Barber {new_barber_id} @ {app_date_str})", status == 201 and new_date_id is not None, f"[status={status}, date_id={new_date_id}]")

# GET /api/dates/{id}
status, _, date_found = request(f"{BASE_URL}/api/dates/{new_date_id}", method="GET", headers=auth_headers)
report(f"GET /api/dates/{new_date_id}", status == 200 and date_found.get("service") == "HAIRCUT", f"[status={status}]")

# GET all dates
status, _, dates_list = request(f"{BASE_URL}/api/dates", method="GET", headers=auth_headers)
report("GET /api/dates", status == 200 and isinstance(dates_list, list), f"[status={status}, count={len(dates_list) if isinstance(dates_list, list) else 0}]")

# Test Business Rule: Outside Business Hours (e.g. 05:00 AM)
early_date = appointment_time.replace(hour=5, minute=0, second=0, microsecond=0).strftime("%Y-%m-%dT%H:%M:%S")
status, _, err_body = request(
    f"{BASE_URL}/api/dates",
    method="POST",
    headers=auth_headers,
    body={
        "client": new_client_id,
        "barber": new_barber_id,
        "service": "SHAVE",
        "status": "PENDING",
        "dateTime": early_date
    }
)
report("Business Rule: Outside Business Hours rejected (400)", status == 400, f"[status={status}, error={err_body}]")

# Test Business Rule: Barber Time Slot Overlap (trying to book same barber within 30 min of existing appointment with a different client)
overlap_date = appointment_time.replace(minute=15).strftime("%Y-%m-%dT%H:%M:%S")
status, _, err_body = request(
    f"{BASE_URL}/api/dates",
    method="POST",
    headers=auth_headers,
    body={
        "client": 1, # JUAN PEREZ
        "barber": new_barber_id,
        "service": "BEARD_TRIM",
        "status": "PENDING",
        "dateTime": overlap_date
    }
)
report("Business Rule: Barber Slot Overlap rejected (409)", status == 409, f"[status={status}, error={err_body}]")

# PUT update date status to DONE
status, _, date_updated = request(
    f"{BASE_URL}/api/dates/{new_date_id}",
    method="PUT",
    headers=auth_headers,
    body={
        "client": new_client_id,
        "barber": new_barber_id,
        "service": "HAIRCUT_AND_BEARD",
        "status": "DONE",
        "dateTime": app_date_str
    }
)
report(f"PUT /api/dates/{new_date_id} (Status to DONE)", status in [200, 201] and date_updated.get("status") == "DONE", f"[status={status}]")

# DELETE /api/dates/{id}
status, _, _ = request(f"{BASE_URL}/api/dates/{new_date_id}", method="DELETE", headers=auth_headers)
report(f"DELETE /api/dates/{new_date_id}", status == 204, f"[status={status}]")

# Verify deleted -> 404
status, _, _ = request(f"{BASE_URL}/api/dates/{new_date_id}", method="GET", headers=auth_headers)
report("GET deleted date returns 404", status == 404, f"[status={status}]")

# -------------------------------------------------------------
# SUMMARY
# -------------------------------------------------------------
print("\n" + "=" * 60)
print(f"RESULTS: {passed_tests} PASSED, {failed_tests} FAILED")
print("=" * 60)

if failed_tests > 0:
    sys.exit(1)
