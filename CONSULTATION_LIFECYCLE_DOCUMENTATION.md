Berikut adalah **CONSULTATION LIFECYCLE** lengkap dengan status, step, dan state terbaru berdasarkan semua perubahan yang telah diimplementasikan:

## **SECURITY & AUTHENTICATION**

### HttpOnly Cookie Authentication
- **Token Storage**: JWT tokens stored in secure HttpOnly cookies (not localStorage)
- **Cookie Attributes**: Secure, HttpOnly, SameSite=Strict, Path=/api
- **Token Expiration**: 7 days (604800 seconds)
- **OTP Rate Limiting**: 3 failed attempts → 15-minute lockout (HTTP 429)
- **CORS Enabled**: Credentials support for cross-origin requests

### Frontend Integration Requirements
```javascript
// When making API requests, use:
fetch(`${apiUrl}/endpoint`, {
  method: 'POST',
  credentials: 'include',  // ✅ REQUIRED for cookie transmission
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data)
})
```

### Testing with Postman
- Set `base_url` to `http://localhost:8080`
- Use `/api/auth/login` or `/api/auth/verify-otp` for initial authentication
- Postman automatically handles cookies for subsequent requests
- Token returned in `Set-Cookie` header with `auth_token` name

## **CONSULTATION STATUS LIFECYCLE (FINAL VERSION)**

### **1. MENUNGGU_KONFIRMASI_KONSULTAN**
- **Step**: CONSULTATION
- **State**: CREATED
- **Consultation Status**: MENUNGGU_KONFIRMASI_KONSULTAN
- **Deskripsi**: Home Owner mengajukan konsultasi, menunggu konsultan terima

### **2. MENYIAPKAN_KONTRAK**
- **Step**: CONTRACT
- **State**: PREPARING
- **Consultation Status**: MENYIAPKAN_KONTRAK
- **Deskripsi**: Konsultan sudah terima, sedang menyiapkan kontrak/survey

### **3. MENUNGGU_JADWAL_SURVEY**
- **Step**: SURVEY
- **State**: SCHEDULE_REQUESTED
- **Consultation Status**: MENUNGGU_JADWAL_SURVEY
- **Deskripsi**: Konsultan mengajukan jadwal survey

### **4. MENUNGGU_APPROVAL_JADWAL**
- **Step**: SURVEY
- **State**: SCHEDULE_PENDING_APPROVAL
- **Consultation Status**: MENUNGGU_APPROVAL_JADWAL
- **Deskripsi**: Home Owner dapat tolak atau propose jadwal baru

### **5. SURVEY_DIJADWALKAN**
- **Step**: SURVEY
- **State**: SCHEDULED
- **Consultation Status**: SURVEY_DIJADWALKAN
- **Deskripsi**: Kedua pihak sudah setuju jadwal

### **6. SURVEY_SELESAI**
- **Step**: SURVEY
- **State**: COMPLETED
- **Consultation Status**: SURVEY_SELESAI
- **Deskripsi**: Survey sudah dilakukan

### **7. MENYIAPKAN_KONTRAK** (kedua kali)
- **Step**: CONTRACT
- **State**: PREPARING
- **Consultation Status**: MENYIAPKAN_KONTRAK
- **Deskripsi**: Konsultan sedang menyiapkan kontrak setelah survey

### **8. MENUNGGU_APPROVAL_KONTRAK**
- **Step**: CONTRACT
- **State**: PENDING_APPROVAL
- **Consultation Status**: MENUNGGU_APPROVAL_KONTRAK
- **Deskripsi**: Kontrak diajukan untuk approval homeowner

### **9. KONTRAK_DISETUJUI**
- **Step**: CONTRACT
- **State**: APPROVED
- **Consultation Status**: KONTRAK_DISETUJUI
- **Deskripsi**: Kontrak disetujui homeowner, menunggu tanda tangan

### **10. MENUNGGU_TANDA_TANGAN_KONTRAK**
- **Step**: CONTRACT
- **State**: HOMEOWNER_SIGNED / CONSULTANT_SIGNED
- **Consultation Status**: MENUNGGU_TANDA_TANGAN_KONTRAK
- **Deskripsi**: Salah satu pihak sudah sign, menunggu pihak lainnya

### **11. MENUNGGU_REQUEST_PAYMENT**
- **Step**: CONTRACT
- **State**: FULLY_SIGNED
- **Consultation Status**: MENUNGGU_REQUEST_PAYMENT
- **Deskripsi**: Kontrak sudah ditandatangani, menunggu konsultan request payment

### **12. MENUNGGU_PEMBAYARAN**
- **Step**: PAYMENT
- **State**: PAYMENT_REQUESTED
- **Consultation Status**: MENUNGGU_PEMBAYARAN
- **Deskripsi**: Payment sudah direquest konsultan, menunggu homeowner bayar

### **13. AKTIF**
- **Step**: CONSULTATION
- **State**: STARTED
- **Consultation Status**: AKTIF
- **Deskripsi**: Konsultasi aktif (chat), payment sudah dibuat

### **14. REVISI** (tetap AKTIF)
- **Step**: DESIGN
- **State**: REVISION_REQUESTED
- **Consultation Status**: AKTIF (tidak berubah)
- **Deskripsi**: Dalam proses revisi design

### **15. FINAL** (tetap AKTIF)
- **Step**: DESIGN
- **State**: FINALIZED
- **Consultation Status**: AKTIF (tidak berubah)
- **Deskripsi**: Konsultasi selesai, menunggu final document

### **16. SELESAI**
- **Step**: CONSULTATION
- **State**: COMPLETED
- **Consultation Status**: SELESAI
- **Deskripsi**: Semua proses selesai

## **CONTRACT STATUS LIFECYCLE**

### **Contract Status:**
1. **DRAFT** → Contract sedang dibuat konsultan
2. **REQUEST_FOR_APPROVAL** → Diajukan untuk approval homeowner
3. **REVISION_REQUESTED** → Homeowner meminta revisi
4. **APPROVED** → Disetujui homeowner
5. **HOMEOWNER_SIGNED** → Homeowner sudah tanda tangan, menunggu konsultan
6. **CONSULTANT_SIGNED** → Konsultan sudah tanda tangan, menunggu homeowner
7. **SIGNED** → Kedua pihak sudah ditandatangani
8. **REJECTED** → Ditolak homeowner

## **PAYMENT FLOW (untuk kasus tanpa installment)**

```
Kontrak SIGNED → MENUNGGU_REQUEST_PAYMENT
                    ↓ (Konsultan klik "Request Payment")
               Payment dibuat (single payment, due today)
                    ↓
               Consultation → MENUNGGU_PEMBAYARAN
                    ↓ (Homeowner bayar)
               Consultation → AKTIF
```

## **EVENT-DRIVEN HISTORY LOGGING**

Semua history sekarang menggunakan **publish event only** pattern:
- `CONTRACT_REQUEST_FOR_APPROVAL`
- `CONTRACT_APPROVED`
- `CONTRACT_REJECTED`
- `CONTRACT_SIGNED` (partial signing)
- `CONTRACT_FULLY_SIGNED`
- `PAYMENT_REQUESTED`
- `CONSULTATION_STARTED`

## **KEY FEATURES IMPLEMENTED:**

1. ✅ **Tanpa Installment Support**: FE bisa kirim request tanpa installments
2. ✅ **Partial Signing**: Track siapa yang sudah sign duluan
3. ✅ **Manual Payment Request**: Konsultan trigger payment creation
4. ✅ **Event-Driven History**: Centralized history logging
5. ✅ **Proper Status Flow**: Consultation status mengikuti business logic