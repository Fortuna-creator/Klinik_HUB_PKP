# Panduan Testing API dengan Postman Collection

## 📦 Postman Collection: Hub API v2.1

File Postman Collection sudah dibuat: `Hub_API_v2.1_Postman_Collection.json`

**Updated Features:**
- OTP Verification with Rate Limiting (429 responses)
- HttpOnly Cookie Authentication
- CORS with Credentials Support
- Complete endpoint documentation with authentication requirements

---

## 🚀 Cara Import ke Postman

### Method 1: Import File
1. Buka Postman
2. Klik **Import** (di pojok kiri atas)
3. Pilih file: `Hub_Balai_Offline_Calendar_Postman_Collection.json`
4. Klik **Import**
5. Collection "Hub Balai Offline Calendar API" akan muncul di sidebar

### Method 2: Drag & Drop
1. Buka Postman
2. Drag file `Hub_Balai_Offline_Calendar_Postman_Collection.json` ke Postman window
3. Collection otomatis ter-import

---

## ⚙️ Setup Collection Variables

Setelah import, setup variables di Collection Settings:

1. Klik kanan pada collection "Hub Balai Offline Calendar API"
2. Pilih **Edit**
3. Buka tab **Variables**
4. Update nilai variables:

| Variable | Default Value | Keterangan | Contoh |
|----------|---------------|------------|--------|
| `base_url` | `http://localhost:8080` | URL backend server | Biarkan default atau ubah sesuai environment |
| `jwt_token` | _(kosong)_ | Deprecated - Token now in HttpOnly cookie | Not needed with credentials: 'include' |
| `user_id` | _(kosong)_ | User ID untuk testing | Ganti dengan user ID yang valid (5001-5005 untuk homeowner) |
| `email` | `test@example.com` | Email untuk login/registration | Updated dengan email test |
| `password` | `password123` | Password untuk testing | Default password untuk semua test accounts |
| `otp_code` | `123456` | OTP code untuk verification | Use 123456 untuk test data |
| `consultant_id` | _(kosong)_ | Consultant ID untuk consultation | Ganti dengan ID konsultan yang valid (6001-6005) |
| `consultation_id` | _(kosong)_ | Consultation ID | Auto-populated setelah create |
| `project_id` | _(kosong)_ | Project ID untuk operations | Auto-populated setelah create project |
| `session_key` | _(kosong)_ | Session key dari confirmation | Auto-populated setelah confirm consultation |
| `retry_after_seconds` | _(kosong)_ | Retry time untuk rate limiting | Auto-populated dari 429 response |

---

## 📝 Testing Workflow

### Step 1: Setup JWT Token (Authentication)

Sebelum test endpoint yang butuh auth, kamu perlu JWT token:

#### New: HttpOnly Cookie Authentication (Recommended)
1. **No need to copy JWT tokens manually**
2. Login endpoint automatically sets `auth_token` HttpOnly cookie
3. Postman automatically includes cookie in subsequent requests
4. Token is securely stored in browser (XSS protected)

#### Step-by-Step Login Flow
1. POST to `/api/auth/register` with email, password, confirmPassword
2. POST to `/api/auth/resend-otp` with email to get OTP
3. POST to `/api/auth/verify-otp` with userId and otpCode (123456 for test)
4. Cookie `auth_token` is set automatically
5. All subsequent requests use the cookie automatically

**Test Account Credentials:**
```
Email: homeowner1@example.com
Password: password
OTP: 123456
Organization: Balai PKP
```

**For Postman Testing:**
- Ensure collection has cookie support enabled
- Responses will include `Set-Cookie: auth_token=...` header
- Token automatically used in following requests
- No manual token copying needed!

---

### Step 2: Get Balai ID

Pastikan kamu punya Balai ID yang valid di database:

```bash
GET http://localhost:8080/api/offline/balai
```

Ambil ID dari salah satu Balai dan update variable `balai_id`.

---

### Step 3: Test Calendar Features (Public Endpoints)

#### 3.1 Get Available Time Slots ⭐
**Tidak perlu authentication**

1. Buka request: **1. Calendar Features → Get Available Time Slots**
2. Klik **Send**
3. Check response:
   - `availableSlots`: Slot yang bisa di-book
   - `bookedSlots`: Slot yang sudah terbooked

**Expected Result:**
```json
{
  "status": "success",
  "data": {
    "availableSlots": ["08:00:00", "09:00:00", "11:00:00"],
    "bookedSlots": ["10:00:00", "12:00:00"],
    "totalAvailableSlots": 3,
    "totalBookedSlots": 2
  }
}
```

**Testing Tips:**
- Ubah parameter `date` untuk cek tanggal berbeda
- Ubah parameter `interval` (30, 60, 90) untuk test interval berbeda
- Test dengan tanggal yang sudah lewat (expect error)

---

#### 3.2 Get Booked Dates Calendar ⭐
**Tidak perlu authentication**

1. Buka request: **1. Calendar Features → Get Booked Dates Calendar**
2. Klik **Send**
3. Check response: List tanggal dengan info booking

**Expected Result:**
```json
{
  "status": "success",
  "data": {
    "dates": [
      {
        "date": "2025-11-15",
        "totalBookings": 5,
        "isFullyBooked": false,
        "bookingPercentage": 55.56
      }
    ]
  }
}
```

**Testing Tips:**
- Test bulan yang berbeda
- Lihat tanggal mana yang fully booked

---

### Step 4: Test Booking Creation

#### 4.1 Create Consultation (Book Slot) ✅
**Perlu authentication**

1. Pastikan `jwt_token` sudah diisi
2. Buka request: **4. Consultation CRUD → Create Consultation (Book)**
3. Check request body:
```json
{
  "userId": 1,
  "balaiId": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
  "consultationDate": "2025-11-20",
  "consultationTime": "09:00:00"
}
```
4. Klik **Send**
5. Consultation ID akan auto-saved ke variable `consultation_id`

**Expected Result:**
```json
{
  "status": "success",
  "message": "Consultation created successfully.",
  "data": {
    "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
    "status": "pending"
  }
}
```

**Testing Scenarios:**
- ✅ Valid booking (expect: 201 Created)
- ❌ Tanggal di masa lalu (expect: 400 Bad Request)
- ❌ Waktu di luar jam kerja (expect: 400 Bad Request)
- ❌ Slot sudah di-book (expect: 409 Conflict)

---

### Step 5: Test My Bookings

#### 5.1 Get My Bookings ⭐
**Perlu authentication**

1. Buka request: **2. My Bookings → Get My Bookings (User History)**
2. Klik **Send**
3. Check response:
   - `upcomingConsultations`: Appointment yang akan datang
   - `pastConsultations`: Riwayat
   - `statusSummary`: Summary by status

**Expected Result:**
```json
{
  "status": "success",
  "data": {
    "upcomingConsultations": [...],
    "pastConsultations": [...],
    "statusSummary": {
      "pending": 1,
      "confirmed": 2,
      "completed": 5
    }
  }
}
```

---

### Step 6: Test Admin Features

#### 6.1 Get Daily Schedule
**Perlu authentication**

1. Buka request: **3. Admin Features → Get Daily Schedule**
2. Klik **Send**
3. Check response: Timeline jadwal hari ini

---

### Step 7: Test Status Update

#### 7.1 Update Consultation Status
**Perlu authentication**

1. Buka request: **4. Consultation CRUD → Update Consultation Status**
2. Ubah body:
```json
{
  "status": "confirmed"
}
```
3. Klik **Send**

**Test Status Transitions:**
- pending → confirmed ✅
- confirmed → completed ✅
- confirmed → cancelled ✅
- completed → confirmed ❌ (expect error)

---

### Step 8: Test Delete

#### 8.1 Delete Consultation
**Perlu authentication**

1. Buka request: **4. Consultation CRUD → Delete Consultation**
2. Klik **Send**
3. Expected: 200 OK

---

## 🎯 Complete Testing Checklist

### Calendar Features (Public)
- [ ] Get Available Slots - Date di masa depan
- [ ] Get Available Slots - Date di masa lalu (error)
- [ ] Get Available Slots - Interval 30 menit
- [ ] Get Available Slots - Interval 60 menit
- [ ] Get Booked Dates - Bulan berjalan
- [ ] Get Booked Dates - Bulan depan

### Booking Management (Authenticated)
- [ ] Create Consultation - Valid data
- [ ] Create Consultation - Invalid date (past)
- [ ] Create Consultation - Invalid time (outside business hours)
- [ ] Create Consultation - Duplicate booking
- [ ] Get My Bookings - User with bookings
- [ ] Get My Bookings - User without bookings
- [ ] Update Status - pending to confirmed
- [ ] Update Status - confirmed to completed
- [ ] Update Status - confirmed to cancelled
- [ ] Delete Consultation

### Admin Features (Authenticated)
- [ ] Get Daily Schedule - Today
- [ ] Get Daily Schedule - Future date
- [ ] Get Consultations by User
- [ ] Get Consultations by Balai

---

## 🔧 Troubleshooting

### Issue: 401 Unauthorized
**Problem:** JWT token tidak valid atau expired

**Solution:**
1. Login ulang untuk dapatkan token baru
2. Copy token dari response
3. Update variable `jwt_token`
4. Retry request

---

### Issue: 404 Not Found - Balai/User not found
**Problem:** ID di variable tidak ada di database

**Solution:**
1. Check database atau call GET /api/offline/balai untuk list balai
2. Update variable `balai_id` atau `user_id` dengan ID yang valid
3. Retry request

---

### Issue: 400 Bad Request - Invalid date
**Problem:** Tanggal yang digunakan sudah lewat

**Solution:**
1. Update variable `test_date` dengan tanggal di masa depan
2. Format: YYYY-MM-DD (contoh: 2025-11-20)
3. Retry request

---

### Issue: 409 Conflict - Slot already booked
**Problem:** Slot waktu sudah di-book oleh user yang sama

**Solution:**
1. Ubah `test_time` ke waktu lain
2. Atau delete consultation sebelumnya
3. Retry request

---

## 📊 Expected Response Codes

| Endpoint | Success Code | Description |
|----------|--------------|-------------|
| Get Available Slots | 200 | OK |
| Get Booked Dates | 200 | OK |
| Get My Bookings | 200 | OK |
| Get Daily Schedule | 200 | OK |
| Create Consultation | 201 | Created |
| Update Status | 200 | OK |
| Delete Consultation | 200 | OK |

---

## 💡 Pro Tips

### 1. Environment Variables
Create different environments (Dev, Staging, Prod) di Postman:
- Manage Environments → Add environment
- Set different `base_url` untuk setiap environment
- Switch environment sesuai kebutuhan

### 2. Pre-request Scripts
Collection sudah include auto-save consultation_id. Untuk auto-login, tambahkan pre-request script:
```javascript
// Auto login before requests that need auth
if (!pm.collectionVariables.get('jwt_token')) {
    // Add login logic here
}
```

### 3. Tests Scripts
Tambahkan test assertions untuk validate response:
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has success status", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.status).to.eql("success");
});
```

### 4. Collection Runner
Run semua tests sekaligus:
1. Klik Collection → Run
2. Select requests yang ingin di-run
3. Klik "Run Hub Balai Offline Calendar API"
4. Check results

---

## 📞 Support

Jika ada issue atau pertanyaan:
- Check dokumentasi API: `BALAI_OFFLINE_CALENDAR_API_DOCUMENTATION.md`
- Check backend logs untuk error details
- Verify database state untuk data consistency

---

**Happy Testing! 🚀**
