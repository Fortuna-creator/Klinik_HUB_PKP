# Balai Offline Calendar & Booking API Documentation

## Overview
API ini menyediakan endpoint untuk fitur kalender dan booking konsultasi offline di Balai. User dapat melihat slot waktu yang tersedia, membuat booking, dan mengelola riwayat konsultasi mereka.

## Base URL
```
/api/offline/balai/consultations
```

---

## Table of Contents
1. [Available Slots API](#1-get-available-time-slots)
2. [Booked Dates Calendar API](#2-get-booked-dates-calendar)
3. [My Bookings API](#3-get-my-bookings)
4. [Daily Schedule API](#4-get-daily-schedule)
5. [User Consultations API](#5-get-consultations-by-user)
6. [Balai Consultations API](#6-get-consultations-by-balai)

---

## Endpoints

### 1. Get Available Time Slots

**Purpose:** Mengecek slot waktu yang masih tersedia untuk booking di tanggal tertentu.

**Endpoint:**
```
GET /api/offline/balai/consultations/available-slots
```

**Authentication:** Not required (Public)

**Query Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| balaiId | UUID | Yes | ID Balai yang ingin dicek | `7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1` |
| date | Date | Yes | Tanggal yang ingin dicek (format: YYYY-MM-DD) | `2025-11-15` |
| interval | Integer | No | Interval slot dalam menit (default: 60) | `60` |

**Request Headers:**
```
Accept: application/json
```

**Request Example:**
```http
GET /api/offline/balai/consultations/available-slots?balaiId=7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1&date=2025-11-15&interval=60
```

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "date": "2025-11-15",
    "balaiId": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
    "balaiName": "Balai Pelatihan Konstruksi Jakarta",
    "businessHours": {
      "start": "08:00:00",
      "end": "17:00:00",
      "slotIntervalMinutes": 60
    },
    "availableSlots": [
      "08:00:00",
      "09:00:00",
      "11:00:00",
      "14:00:00",
      "16:00:00"
    ],
    "bookedSlots": [
      "10:00:00",
      "12:00:00",
      "13:00:00",
      "15:00:00"
    ],
    "totalAvailableSlots": 5,
    "totalBookedSlots": 4
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "status": "error",
  "message": "Cannot check availability for past dates"
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

**Use Case:**
- User memilih tanggal di calendar
- Frontend request slot yang available
- Frontend menampilkan time picker dengan slot yang tersedia
- Frontend disable slot yang sudah terbooked

---

### 2. Get Booked Dates Calendar

**Purpose:** Mendapatkan informasi tanggal-tanggal yang ada booking dalam 1 bulan untuk calendar view.

**Endpoint:**
```
GET /api/offline/balai/consultations/booked-dates
```

**Authentication:** Not required (Public)

**Query Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| balaiId | UUID | Yes | ID Balai yang ingin dilihat | `7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1` |
| month | Integer | Yes | Bulan (1-12) | `11` |
| year | Integer | Yes | Tahun | `2025` |

**Request Headers:**
```
Accept: application/json
```

**Request Example:**
```http
GET /api/offline/balai/consultations/booked-dates?balaiId=7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1&month=11&year=2025
```

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "balaiId": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
    "balaiName": "Balai Pelatihan Konstruksi Jakarta",
    "month": 11,
    "year": 2025,
    "dates": [
      {
        "date": "2025-11-15",
        "totalBookings": 5,
        "availableSlots": 4,
        "totalSlots": 9,
        "isFullyBooked": false,
        "bookingPercentage": 55.56
      },
      {
        "date": "2025-11-16",
        "totalBookings": 9,
        "availableSlots": 0,
        "totalSlots": 9,
        "isFullyBooked": true,
        "bookingPercentage": 100.0
      },
      {
        "date": "2025-11-20",
        "totalBookings": 3,
        "availableSlots": 6,
        "totalSlots": 9,
        "isFullyBooked": false,
        "bookingPercentage": 33.33
      }
    ],
    "totalDaysWithBookings": 8,
    "totalFullyBookedDays": 2
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

**Use Case:**
- User membuka calendar bulan November 2025
- Frontend highlight tanggal-tanggal yang ada booking
- Frontend tampilkan visual indicator (dot/badge) untuk booking density
- Frontend mark tanggal yang fully booked dengan warna berbeda

**UI Visualization Ideas:**
- Green dot: < 50% booked
- Yellow dot: 50-80% booked
- Red dot: > 80% booked atau fully booked
- Disable click pada fully booked dates

---

### 3. Get My Bookings

**Purpose:** User melihat riwayat booking mereka (upcoming & past consultations).

**Endpoint:**
```
GET /api/offline/balai/consultations/my-bookings
```

**Authentication:** Required (Bearer Token)

**Query Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| userId | Long | Yes | ID User yang login | `1` |

**Request Headers:**
```
Accept: application/json
Authorization: Bearer {token}
```

**Request Example:**
```http
GET /api/offline/balai/consultations/my-bookings?userId=1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "userId": 1,
    "userName": "John Doe",
    "upcomingConsultations": [
      {
        "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
        "user": {
          "id": 1,
          "name": "John Doe"
        },
        "balai": {
          "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
          "name": "Balai Pelatihan Konstruksi Jakarta"
        },
        "consultationDate": "2025-11-20",
        "consultationTime": "09:00:00",
        "status": "confirmed",
        "createdAt": "2025-11-10T10:00:00Z",
        "updatedAt": "2025-11-10T11:00:00Z"
      },
      {
        "id": "c7d13f9f-33b0-5e61-cbcd-ed0cbg3g2g3b",
        "user": {
          "id": 1,
          "name": "John Doe"
        },
        "balai": {
          "id": "8d6b4d6g-9b88-5e5f-0f1c-52c1c023d6b2",
          "name": "Balai Pelatihan Konstruksi Bandung"
        },
        "consultationDate": "2025-11-25",
        "consultationTime": "14:00:00",
        "status": "pending",
        "createdAt": "2025-11-11T08:00:00Z",
        "updatedAt": "2025-11-11T08:00:00Z"
      }
    ],
    "pastConsultations": [
      {
        "id": "d8e24g0g-44c1-6f72-dcde-fe1dch4h3h4c",
        "user": {
          "id": 1,
          "name": "John Doe"
        },
        "balai": {
          "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
          "name": "Balai Pelatihan Konstruksi Jakarta"
        },
        "consultationDate": "2025-11-05",
        "consultationTime": "10:00:00",
        "status": "completed",
        "createdAt": "2025-11-01T10:00:00Z",
        "updatedAt": "2025-11-05T11:00:00Z"
      }
    ],
    "totalUpcoming": 2,
    "totalPast": 1,
    "statusSummary": {
      "pending": 1,
      "confirmed": 1,
      "completed": 1,
      "cancelled": 0
    }
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "User not found."
}
```

**Use Case:**
- User membuka halaman "My Bookings"
- Tampilkan 2 section: "Upcoming Appointments" & "History"
- User bisa cancel booking yang status pending/confirmed
- User bisa lihat detail setiap consultation

**UI Components:**
```
┌─────────────────────────────────────┐
│         My Bookings                 │
├─────────────────────────────────────┤
│ 📊 Summary                          │
│  • Upcoming: 2                      │
│  • Pending: 1 | Confirmed: 1        │
│  • Completed: 1 | Cancelled: 0      │
├─────────────────────────────────────┤
│ 📅 Upcoming Appointments            │
│  ┌───────────────────────────────┐ │
│  │ Nov 20, 2025 - 09:00          │ │
│  │ Balai Jakarta                 │ │
│  │ Status: Confirmed ✓           │ │
│  │ [View Details] [Cancel]       │ │
│  └───────────────────────────────┘ │
├─────────────────────────────────────┤
│ 📜 History                          │
│  ┌───────────────────────────────┐ │
│  │ Nov 05, 2025 - 10:00          │ │
│  │ Balai Jakarta                 │ │
│  │ Status: Completed ✓           │ │
│  │ [View Details]                │ │
│  └───────────────────────────────┘ │
└─────────────────────────────────────┘
```

---

### 4. Get Daily Schedule

**Purpose:** Admin/Balai melihat jadwal konsultasi harian.

**Endpoint:**
```
GET /api/offline/balai/consultations/daily-schedule
```

**Authentication:** Required (Bearer Token)

**Query Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| balaiId | UUID | Yes | ID Balai | `7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1` |
| date | Date | Yes | Tanggal yang ingin dilihat | `2025-11-15` |

**Request Headers:**
```
Accept: application/json
Authorization: Bearer {token}
```

**Request Example:**
```http
GET /api/offline/balai/consultations/daily-schedule?balaiId=7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1&date=2025-11-15
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "balaiId": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
    "balaiName": "Balai Pelatihan Konstruksi Jakarta",
    "date": "2025-11-15",
    "consultations": [
      {
        "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
        "user": {
          "id": 1,
          "name": "John Doe"
        },
        "balai": {
          "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
          "name": "Balai Pelatihan Konstruksi Jakarta"
        },
        "consultationDate": "2025-11-15",
        "consultationTime": "08:00:00",
        "status": "confirmed",
        "createdAt": "2025-11-10T10:00:00Z",
        "updatedAt": "2025-11-10T11:00:00Z"
      },
      {
        "id": "c7d13f9f-33b0-5e61-cbcd-ed0cbg3g2g3b",
        "user": {
          "id": 2,
          "name": "Jane Smith"
        },
        "balai": {
          "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
          "name": "Balai Pelatihan Konstruksi Jakarta"
        },
        "consultationDate": "2025-11-15",
        "consultationTime": "10:00:00",
        "status": "pending",
        "createdAt": "2025-11-11T08:00:00Z",
        "updatedAt": "2025-11-11T08:00:00Z"
      }
    ],
    "totalConsultations": 2,
    "availableSlots": 7,
    "businessHours": {
      "start": "08:00:00",
      "end": "17:00:00",
      "slotIntervalMinutes": 60
    }
  }
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

**Use Case:**
- Admin Balai melihat jadwal hari ini
- Tampilkan timeline view dengan jam-jam yang ada booking
- Admin bisa confirm/cancel consultation
- Lihat occupancy rate (berapa % slot terisi)

---

### 5. Get Consultations by User

**Purpose:** Mendapatkan semua consultations milik user tertentu.

**Endpoint:**
```
GET /api/offline/balai/consultations/user/{userId}
```

**Authentication:** Required (Bearer Token)

**Path Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| userId | Long | Yes | ID User | `1` |

**Request Headers:**
```
Accept: application/json
Authorization: Bearer {token}
```

**Request Example:**
```http
GET /api/offline/balai/consultations/user/1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
      "user": {
        "id": 1,
        "name": "John Doe"
      },
      "balai": {
        "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
        "name": "Balai Pelatihan Konstruksi Jakarta"
      },
      "consultationDate": "2025-11-20",
      "consultationTime": "09:00:00",
      "status": "confirmed",
      "createdAt": "2025-11-10T10:00:00Z",
      "updatedAt": "2025-11-10T11:00:00Z"
    }
  ]
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "User not found."
}
```

---

### 6. Get Consultations by Balai

**Purpose:** Mendapatkan semua consultations di balai tertentu (Admin view).

**Endpoint:**
```
GET /api/offline/balai/consultations/balai/{balaiId}
```

**Authentication:** Required (Bearer Token)

**Path Parameters:**
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| balaiId | UUID | Yes | ID Balai | `7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1` |

**Request Headers:**
```
Accept: application/json
Authorization: Bearer {token}
```

**Request Example:**
```http
GET /api/offline/balai/consultations/balai/7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Success Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "id": "b6c02e8e-22a9-4d50-babc-dc9baf2f1f2a",
      "user": {
        "id": 1,
        "name": "John Doe"
      },
      "balai": {
        "id": "7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1",
        "name": "Balai Pelatihan Konstruksi Jakarta"
      },
      "consultationDate": "2025-11-20",
      "consultationTime": "09:00:00",
      "status": "confirmed",
      "createdAt": "2025-11-10T10:00:00Z",
      "updatedAt": "2025-11-10T11:00:00Z"
    }
  ]
}
```

**Error Response (401 Unauthorized):**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**Error Response (404 Not Found):**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

---

## Data Models

### AvailableSlotsResponse
| Field | Type | Description |
|-------|------|-------------|
| date | Date | Tanggal yang dicek |
| balaiId | UUID | ID Balai |
| balaiName | String | Nama Balai |
| businessHours | BusinessHours | Jam operasional |
| availableSlots | List\<String\> | List waktu yang available |
| bookedSlots | List\<String\> | List waktu yang sudah terbooked |
| totalAvailableSlots | Integer | Total slot available |
| totalBookedSlots | Integer | Total slot terbooked |

### BookedDatesCalendarResponse
| Field | Type | Description |
|-------|------|-------------|
| balaiId | UUID | ID Balai |
| balaiName | String | Nama Balai |
| month | Integer | Bulan (1-12) |
| year | Integer | Tahun |
| dates | List\<BookedDateResponse\> | List tanggal dengan info booking |
| totalDaysWithBookings | Integer | Total hari yang ada booking |
| totalFullyBookedDays | Integer | Total hari yang fully booked |

### BookedDateResponse
| Field | Type | Description |
|-------|------|-------------|
| date | Date | Tanggal |
| totalBookings | Integer | Jumlah booking di tanggal ini |
| availableSlots | Integer | Slot yang masih tersedia |
| totalSlots | Integer | Total slot per hari |
| isFullyBooked | Boolean | Apakah fully booked |
| bookingPercentage | Double | Persentase booking (0-100) |

### UserConsultationHistoryResponse
| Field | Type | Description |
|-------|------|-------------|
| userId | Long | ID User |
| userName | String | Nama User |
| upcomingConsultations | List\<OfflineConsultationResponse\> | Consultation yang akan datang |
| pastConsultations | List\<OfflineConsultationResponse\> | Riwayat consultation |
| totalUpcoming | Integer | Total upcoming |
| totalPast | Integer | Total past |
| statusSummary | ConsultationStatusSummary | Summary berdasarkan status |

### DailyScheduleResponse
| Field | Type | Description |
|-------|------|-------------|
| balaiId | UUID | ID Balai |
| balaiName | String | Nama Balai |
| date | Date | Tanggal |
| consultations | List\<OfflineConsultationResponse\> | List consultation hari ini |
| totalConsultations | Integer | Total consultation |
| availableSlots | Integer | Slot yang masih available |
| businessHours | BusinessHours | Jam operasional |

### BusinessHours
| Field | Type | Description |
|-------|------|-------------|
| start | String | Jam buka (default: "08:00:00") |
| end | String | Jam tutup (default: "17:00:00") |
| slotIntervalMinutes | Integer | Interval slot dalam menit (default: 60) |

### ConsultationStatusSummary
| Field | Type | Description |
|-------|------|-------------|
| pending | Long | Jumlah pending |
| confirmed | Long | Jumlah confirmed |
| completed | Long | Jumlah completed |
| cancelled | Long | Jumlah cancelled |

---

## Business Rules

### 1. Time Slot Generation
- **Business Hours:** 08:00 - 17:00 (9 jam)
- **Default Interval:** 60 menit (1 jam per slot)
- **Total Slots per Day:** 9 slots
- Slot times: `08:00, 09:00, 10:00, 11:00, 12:00, 13:00, 14:00, 15:00, 16:00`

### 2. Availability Rules
- Tanggal harus di masa depan (tidak bisa cek availability untuk tanggal yang sudah lewat)
- Slot yang sudah di-book dengan status `pending`, `confirmed`, atau `completed` dianggap tidak available
- Slot dengan status `cancelled` dianggap available lagi

### 3. Booking Status Flow
```
pending → confirmed → completed
   ↓          ↓
cancelled  cancelled
```

### 4. Fully Booked Definition
- Tanggal dianggap **fully booked** jika: `totalBookings >= totalSlots`
- Contoh: Jika ada 9 booking di 1 hari (dengan 9 total slots), maka tanggal tersebut fully booked

### 5. Upcoming vs Past Classification
- **Upcoming:** `consultationDate > currentDate` OR `(consultationDate == currentDate AND consultationTime >= currentTime)`
- **Past:** `consultationDate < currentDate` OR `(consultationDate == currentDate AND consultationTime < currentTime)`

---

## Error Handling

### Common Error Responses

**400 Bad Request - Invalid Date:**
```json
{
  "status": "error",
  "message": "Cannot check availability for past dates"
}
```

**401 Unauthorized:**
```json
{
  "status": "error",
  "message": "Authentication required."
}
```

**404 Not Found - Balai:**
```json
{
  "status": "error",
  "message": "Balai not found."
}
```

**404 Not Found - User:**
```json
{
  "status": "error",
  "message": "User not found."
}
```

**500 Internal Server Error:**
```json
{
  "status": "error",
  "message": "Failed to retrieve available slots."
}
```

---

## Frontend Integration Guide

### 1. Calendar Component Flow

```javascript
// Step 1: User opens calendar
// Fetch booked dates untuk highlight
const fetchBookedDates = async (balaiId, month, year) => {
  const response = await fetch(
    `/api/offline/balai/consultations/booked-dates?balaiId=${balaiId}&month=${month}&year=${year}`
  );
  const data = await response.json();
  
  // Highlight dates di calendar
  data.data.dates.forEach(date => {
    highlightDate(date.date, {
      color: date.isFullyBooked ? 'red' : 'green',
      tooltip: `${date.totalBookings}/${date.totalSlots} booked`
    });
  });
};

// Step 2: User clicks tanggal
// Fetch available slots
const fetchAvailableSlots = async (balaiId, date) => {
  const response = await fetch(
    `/api/offline/balai/consultations/available-slots?balaiId=${balaiId}&date=${date}`
  );
  const data = await response.json();
  
  // Show time picker dengan available slots
  renderTimePicker(data.data.availableSlots, data.data.bookedSlots);
};

// Step 3: User pilih waktu & submit booking
const createBooking = async (bookingData) => {
  const response = await fetch('/api/offline/balai/consultations', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(bookingData)
  });
  
  if (response.ok) {
    showSuccess('Booking berhasil dibuat!');
    navigateToMyBookings();
  }
};
```

### 2. My Bookings Page Flow

```javascript
const fetchMyBookings = async (userId) => {
  const response = await fetch(
    `/api/offline/balai/consultations/my-bookings?userId=${userId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  const data = await response.json();
  
  // Render upcoming section
  renderUpcomingConsultations(data.data.upcomingConsultations);
  
  // Render history section
  renderPastConsultations(data.data.pastConsultations);
  
  // Render summary
  renderSummary(data.data.statusSummary);
};
```

### 3. Admin Daily Schedule Flow

```javascript
const fetchDailySchedule = async (balaiId, date) => {
  const response = await fetch(
    `/api/offline/balai/consultations/daily-schedule?balaiId=${balaiId}&date=${date}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  const data = await response.json();
  
  // Render timeline view
  renderTimeline(data.data.consultations, data.data.businessHours);
  
  // Show statistics
  showStats({
    total: data.data.totalConsultations,
    available: data.data.availableSlots
  });
};
```

---

## Testing Examples

### cURL Examples

**1. Get Available Slots:**
```bash
curl -X GET "http://localhost:8080/api/offline/balai/consultations/available-slots?balaiId=7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1&date=2025-11-15&interval=60" \
  -H "Accept: application/json"
```

**2. Get Booked Dates:**
```bash
curl -X GET "http://localhost:8080/api/offline/balai/consultations/booked-dates?balaiId=7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1&month=11&year=2025" \
  -H "Accept: application/json"
```

**3. Get My Bookings:**
```bash
curl -X GET "http://localhost:8080/api/offline/balai/consultations/my-bookings?userId=1" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**4. Get Daily Schedule:**
```bash
curl -X GET "http://localhost:8080/api/offline/balai/consultations/daily-schedule?balaiId=7c5a3c5f-8a77-4d4e-9e0b-41b0b912c5a1&date=2025-11-15" \
  -H "Accept: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

---

## Performance Considerations

### 1. Caching Strategy
Untuk meningkatkan performa, pertimbangkan caching untuk:
- **Available Slots:** Cache per balai + date (TTL: 5 menit)
- **Booked Dates Calendar:** Cache per balai + month + year (TTL: 1 jam)
- Invalidate cache saat ada booking baru/update/delete

### 2. Database Indexing
Indexes yang sudah ada:
```sql
-- Sudah ada di migration
CREATE INDEX idx_offline_consultation_user_id ON offline_consultations(user_id);
CREATE INDEX idx_offline_consultation_balai_id ON offline_consultations(balai_id);
CREATE INDEX idx_offline_consultation_date ON offline_consultations(consultation_date);
CREATE INDEX idx_offline_consultation_status ON offline_consultations(status);
CREATE UNIQUE INDEX idx_offline_consultation_unique_booking 
  ON offline_consultations(user_id, balai_id, consultation_date, consultation_time)
  WHERE status != 'cancelled';
```

### 3. Query Optimization
- Query `findBookedTimeSlotsByBalaiAndDate` sangat efficient karena filter by indexed columns
- Query `findBookedDatesByBalaiAndDateRange` menggunakan `DISTINCT` dan indexed date range
- Semua query exclude cancelled consultations untuk akurasi availability

---

## Versioning & Changelog

### Version 1.0.0 (November 2025)
- Initial release
- 6 new endpoints untuk calendar & booking features
- Support untuk time slot management
- Calendar month view dengan booking statistics
- User booking history dengan upcoming/past separation
- Daily schedule view untuk admin

---

## Support & Contact

Untuk pertanyaan atau issues terkait API ini, silakan hubungi:
- **Team:** Hub Backend Team
- **Repository:** hub-backend
- **Branch:** feature/balai-offline-consultation

---

## Related Documentation

- [Offline Balai API Documentation](OFFLINE_BALAI_API_DOCUMENTATION.md)
- [Offline Consultation API Documentation](OFFLINE_CONSULTATION_API_DOCUMENTATION.md)
- [API Documentation](API_DOCUMENTATION.md)

---

**Last Updated:** November 12, 2025
