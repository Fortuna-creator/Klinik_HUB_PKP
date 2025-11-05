# Payment Lifecycle Documentation

## Overview
This document outlines the payment lifecycle flow starting from contract signing by both homeowner and consultant, including all API endpoints and their request/response payloads for frontend team integration.

## Payment Lifecycle Flow

### 1. Contract Signing Process
After contract approval, both parties need to sign the contract:

**Contract Status Flow:**
- `APPROVED` → `HOMEOWNER_SIGNED` → `CONSULTANT_SIGNED` → `SIGNED`

**API Endpoint: Sign Contract**
```
PATCH /api/contracts/{contractId}/sign
Authorization: Bearer {token}
```

**Request:**
```json
// No request body required
```

**Response:**
```json
{
  "id": "uuid",
  "consultationId": "uuid",
  "fileUrl": "string",
  "digitalSignature": true,
  "status": "SIGNED",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### 2. Payment Request by Consultant
After contract is fully signed (`SIGNED`), consultant can request payment:

**API Endpoint: Request Payment**
```
POST /api/contracts/{contractId}/request-payment
Authorization: Bearer {consultant-token}
```

**Request:**
```json
// No request body required
```

**Response:**
```json
{
  "id": "uuid",
  "consultationId": "uuid",
  "fileUrl": "string",
  "digitalSignature": true,
  "status": "SIGNED",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

**What happens internally:**
- **With Installments**: Creates payment installments based on contract installments (1-3 installments with custom percentages)
- **Without Installments**: Creates single payment installment due immediately after signing
- Creates escrow account for the consultation
- Updates consultation status to `AKTIF`
- Creates payment records with status `MENUNGGU` (waiting for homeowner payment)

### 2.1 Single Payment Case (No Installments)
If no installments are specified during contract creation, the system automatically creates a single payment:

**Single Payment Behavior:**
- Payment amount = Full contract value
- Due date = Same day as contract signing
- Termin = 1
- Status = `MENUNGGU`

### 3. Payment Approval by Homeowner
Homeowner approves the payment to hold funds in escrow:

**API Endpoint: Approve Payment**
```
POST /api/payments/{paymentId}/approve
Authorization: Bearer {homeowner-token}
```

**Request:**
```json
// No request body required
```

**Response:**
```json
{
  "id": "uuid",
  "consultationId": "uuid",
  "type": "KONSULTASI",
  "termin": 1,
  "amount": 5000000.00,
  "method": "BANK_TRANSFER",
  "description": "Pembayaran konsultasi desain termin 1",
  "status": "ESCROW_HOLD",
  "paymentGatewayRef": null,
  "dueDate": "2024-01-15T00:00:00",
  "referenceId": "contract-uuid",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### 4. Payment Release by Consultant
Consultant releases funds from escrow after work completion:

**API Endpoint: Release Payment**
```
POST /api/payments/{paymentId}/release
Authorization: Bearer {consultant-token}
```

**Request:**
```json
// No request body required
```

**Response:**
```json
{
  "id": "uuid",
  "consultationId": "uuid",
  "type": "KONSULTASI",
  "termin": 1,
  "amount": 5000000.00,
  "method": "BANK_TRANSFER",
  "description": "Pembayaran konsultasi desain termin 1",
  "status": "ESCROW_RELEASED",
  "paymentGatewayRef": null,
  "dueDate": "2024-01-15T00:00:00",
  "referenceId": "contract-uuid",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

## Payment Status Flow

```
MENUNGGU (Waiting for homeowner payment)
    ↓
ESCROW_PENDING (Payment created, waiting for approval)
    ↓
ESCROW_HOLD (Funds held in escrow)
    ↓
ESCROW_RELEASED (Funds released to consultant)
```

## Additional API Endpoints

### Get Payments by Consultation
```
GET /api/payments?consultationId={consultationId}
Authorization: Bearer {token}
```

**Response:**
```json
[
  {
    "id": "uuid",
    "consultationId": "uuid",
    "type": "KONSULTASI",
    "termin": 1,
    "amount": 5000000.00,
    "method": "BANK_TRANSFER",
    "description": "Pembayaran konsultasi desain termin 1",
    "status": "ESCROW_HOLD",
    "paymentGatewayRef": null,
    "dueDate": "2024-01-15T00:00:00",
    "referenceId": "contract-uuid",
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
]
```

### Request Payment Notification (Consultant)
```
POST /api/payments/request?consultationId={consultationId}
Authorization: Bearer {consultant-token}
```

**Request:**
```json
// No request body required
```

**Response:**
```json
"Payment request sent to homeowner"
```

### Get Wallet Balance
```
GET /api/wallet/balance
Authorization: Bearer {token}
```

**Request:**
```json
// No request body required
```

**Response:**
```json
{
  "id": 1,
  "userId": 123,
  "balance": 1000000.00,
  "currency": "IDR",
  "isActive": true,
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

## Payment Types

- `KONSULTASI`: Consultation payment (default)
- `PERIZINAN`: Permit payment
- `KONTRAKTOR`: Contractor payment
- `SURVEY`: Survey payment
- `MATERIAL`: Material payment
- `LAINNYA`: Other payments

## Payment Methods

- `BANK_TRANSFER`: Bank transfer
- `CREDIT_CARD`: Credit card
- `DEBIT_CARD`: Debit card
- `EWALLET`: E-wallet

## Installment Scenarios

### Scenario 1: Multiple Installments (1-3 installments)
- **Input**: List of `PaymentInstallmentRequest` with custom percentages and due dates
- **Validation**: Total percentages must equal 100%
- **Example**: 30% due in 7 days, 40% due in 30 days, 30% due in 60 days

### Scenario 2: Single Payment (No installments specified)
- **Input**: `null` or empty installment list
- **Behavior**: Automatically creates single payment for full contract value
- **Due Date**: 7 days after contract signing
- **Termin**: 1

### Scenario 2.1: Manual Single Payment Creation (Non-Consultation)
- **Input**: Homeowner manually creates payment without specifying termin
- **Behavior**: Allows creation of single payments outside the automatic installment flow
- **Termin**: null (not set)
- **Use Case**: Ad-hoc payments for permits, contractors, materials, or other services

### Scenario 3: Legacy Fixed Distribution (Deprecated)
- **Input**: Integer number of installments (1-3)
- **Distribution**:
  - 1 installment: 100% due in 7 days
  - 2 installments: 50% - 50% (7 days, 30 days)
  - 3 installments: 30% - 40% - 30% (7 days, 30 days, 60 days)

## Error Handling

All endpoints return standard error responses:

```json
{
  "timestamp": "2024-01-01T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Error message",
  "path": "/api/payments"
}
```

## Frontend Integration Notes

1. **Contract Signing**: Call sign endpoint for both homeowner and consultant
2. **Payment Request**: Consultant calls request-payment after contract is signed
3. **Payment Approval**: Homeowner approves to hold funds in escrow
5. **Payment Release**: Consultant releases funds after work completion
6. **Status Monitoring**: Poll payment status or use websockets for real-time updates

## Security Notes

- All endpoints require authentication
- Homeowner can only approve their own payments
- Consultant can only release payments for their consultations
- Role-based access control is enforced