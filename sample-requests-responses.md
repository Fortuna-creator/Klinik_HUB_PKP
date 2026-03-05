# Hub API v2.1 - Sample Requests & Responses

## 🔐 Authentication Overview

All authenticated endpoints use **HttpOnly Cookie-based JWT authentication**.
Token is automatically set in response header after login/OTP verification.

**Important**: Frontend must use `credentials: 'include'` in fetch calls:
```javascript
fetch(url, {
  method: 'POST',
  credentials: 'include',  // ✅ Required for HttpOnly cookies
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(data)
})
```

---

## 📋 Portfolio Endpoints Overview

### 1. Create/Update Portfolio
**Endpoint:** `POST /api/users/portfolio`  
**Description:** Create or update a consultant's basic portfolio information

#### Sample Request
```bash
POST /api/users/portfolio
Cookie: auth_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "bio": "Experienced architect specializing in modern residential designs with sustainable materials",
  "specialization": "Modern Architecture & Sustainable Design",
  "experience": "7 years",
  "education": "Master of Architecture from Bandung Institute of Technology",
  "certifications": "Certified Green Architect, LEED Accredited Professional",
  "linkedinUrl": "https://linkedin.com/in/jane-architect",
  "websiteUrl": "https://jane-architect.com"
}
```

**Note**: Cookie `auth_token` is set automatically after login. No manual Authorization header needed.

#### Sample Response (201 Created)
```json
{
  "id": 1,
  "consultant": {
    "id": 123,
    "name": "Jane Architect",
    "email": "jane.architect@example.com"
  },
  "bio": "Experienced architect specializing in modern residential designs with sustainable materials",
  "specialization": "Modern Architecture & Sustainable Design",
  "experience": "7 years",
  "education": "Master of Architecture from Bandung Institute of Technology",
  "certifications": "Certified Green Architect, LEED Accredited Professional",
  "linkedinUrl": "https://linkedin.com/in/jane-architect",
  "websiteUrl": "https://jane-architect.com",
  "projects": [],
  "status": "INCOMPLETE",
  "createdAt": "2025-01-15T08:30:00.000Z",
  "updatedAt": "2025-01-15T08:30:00.000Z"
}
```

### 2. Update Portfolio
**Endpoint:** `PUT /api/users/portfolio`  
**Description:** Update an existing consultant's portfolio

#### Sample Request
```bash
PUT /api/users/portfolio
Cookie: auth_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "bio": "Experienced architect specializing in modern residential and commercial designs with sustainable materials",
  "specialization": "Modern Architecture & Sustainable Design",
  "experience": "8 years",
  "education": "Master of Architecture from Bandung Institute of Technology",
  "certifications": "Certified Green Architect, LEED Accredited Professional, Certified BIM Manager",
  "linkedinUrl": "https://linkedin.com/in/jane-architect",
  "websiteUrl": "https://jane-architect.com"
}
```

#### Sample Response (200 OK)
```json
{
  "id": 1,
  "consultant": {
    "id": 123,
    "name": "Jane Architect",
    "email": "jane.architect@example.com"
  },
  "bio": "Experienced architect specializing in modern residential and commercial designs with sustainable materials",
  "specialization": "Modern Architecture & Sustainable Design",
  "experience": "8 years",
  "education": "Master of Architecture from Bandung Institute of Technology",
  "certifications": "Certified Green Architect, LEED Accredited Professional, Certified BIM Manager",
  "linkedinUrl": "https://linkedin.com/in/jane-architect",
  "websiteUrl": "https://jane-architect.com",
  "projects": [
    {
      "id": 1,
      "projectName": "Modern Minimalist House - BSD City",
      "price": 75000.00,
      "detailDescription": "Complete architectural design for a 300m² modern minimalist house in BSD City...",
      "imageUrls": [
        "https://minio.example.com/modern-house-exterior.jpg",
        "https://minio.example.com/modern-house-interior.jpg"
      ],
      "createdAt": "2025-01-15T09:00:00.000Z",
      "updatedAt": "2025-01-15T09:00:00.000Z"
    }
  ],
  "status": "COMPLETED",
  "createdAt": "2025-01-15T08:30:00.000Z",
  "updatedAt": "2025-01-15T09:15:00.000Z"
}
```

### 3. Get Own Portfolio
**Endpoint:** `GET /api/users/portfolio`  
**Description:** Get the authenticated consultant's own portfolio

#### Sample Request
```bash
GET /api/users/portfolio
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Sample Response (200 OK)
```json
{
  "id": 1,
  "consultant": {
    "id": 123,
    "name": "Jane Architect",
    "email": "jane.architect@example.com"
  },
  "bio": "Experienced architect specializing in modern residential and commercial designs...",
  "specialization": "Modern Architecture & Sustainable Design",
  "experience": "8 years",
  "education": "Master of Architecture from Bandung Institute of Technology",
  "certifications": "Certified Green Architect, LEED Accredited Professional, Certified BIM Manager",
  "linkedinUrl": "https://linkedin.com/in/jane-architect",
  "websiteUrl": "https://jane-architect.com",
  "projects": [
    {
      "id": 1,
      "projectName": "Modern Minimalist House - BSD City",
      "price": 75000.00,
      "detailDescription": "Complete architectural design for a 300m² modern minimalist house in BSD City...",
      "imageUrls": [
        "https://minio.example.com/modern-house-exterior.jpg",
        "https://minio.example.com/modern-house-interior.jpg",
        "https://minio.example.com/modern-house-3d-render.jpg",
        "https://minio.example.com/modern-house-floor-plan.jpg"
      ],
      "createdAt": "2025-01-15T09:00:00.000Z",
      "updatedAt": "2025-01-15T09:00:00.000Z"
    },
    {
      "id": 2,
      "projectName": "Commercial Office Building - Jakarta CBD",
      "price": 150000.00,
      "detailDescription": "Comprehensive architectural design for a 10-story commercial office building...",
      "imageUrls": [
        "https://minio.example.com/office-building-facade.jpg",
        "https://minio.example.com/office-building-floor-plan.jpg",
        "https://minio.example.com/office-building-interior.jpg"
      ],
      "createdAt": "2025-01-15T10:30:00.000Z",
      "updatedAt": "2025-01-15T10:30:00.000Z"
    }
  ],
  "status": "COMPLETED",
  "createdAt": "2025-01-15T08:30:00.000Z",
  "updatedAt": "2025-01-15T10:30:00.000Z"
}
```

### 4. Add Portfolio Project
**Endpoint:** `POST /api/users/portfolio/projects`  
**Description:** Add a new project to the consultant's portfolio

#### Sample Request
```bash
POST /api/users/portfolio/projects
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "projectName": "Modern Minimalist House - BSD City",
  "price": 75000.00,
  "detailDescription": "Complete architectural design for a 300m² modern minimalist house in BSD City. Includes 3D visualization, working drawings, structural calculations, and MEP coordination. Features open-concept living spaces, sustainable materials, and smart home integration.",
  "imageUrls": [
    "https://minio.example.com/modern-house-exterior.jpg",
    "https://minio.example.com/modern-house-interior.jpg",
    "https://minio.example.com/modern-house-3d-render.jpg",
    "https://minio.example.com/modern-house-floor-plan.jpg"
  ]
}
```

#### Sample Response (200 OK)
```json
{
  "id": 1,
  "projectName": "Modern Minimalist House - BSD City",
  "price": 75000.00,
  "detailDescription": "Complete architectural design for a 300m² modern minimalist house in BSD City. Includes 3D visualization, working drawings, structural calculations, and MEP coordination. Features open-concept living spaces, sustainable materials, and smart home integration.",
  "imageUrls": [
    "https://minio.example.com/modern-house-exterior.jpg",
    "https://minio.example.com/modern-house-interior.jpg",
    "https://minio.example.com/modern-house-3d-render.jpg",
    "https://minio.example.com/modern-house-floor-plan.jpg"
  ],
  "createdAt": "2025-01-15T09:00:00.000Z",
  "updatedAt": "2025-01-15T09:00:00.000Z"
}
```

### 5. Get Consultant Portfolio by ID
**Endpoint:** `GET /api/users/consultants/{consultantId}/portfolio`
**Description:** Get a specific consultant's portfolio (for homeowners to view when selecting consultants)

#### Sample Request
```bash
GET /api/users/consultants/123/portfolio
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Sample Response (200 OK)
```json
{
  "consultant": {
    "id": 123,
    "name": "John Consultant",
    "email": "john@example.com",
    "specialization": "Architecture"
  },
  "portfolios": [
    {
      "id": 1,
      "projectName": "Modern House Design",
      "price": 50000,
      "detailDescription": "Complete modern house design...",
      "imageUrls": [
        "url1",
        "url2",
        "url3"
      ],
      "createdAt": "2025-01-15T10:30:00"
    }
  ]
}
```

### 6. Upload Portfolio Images
**Endpoint:** `POST /api/users/portfolio/upload-images`  
**Description:** Upload multiple images for portfolio projects

#### Sample Request
```bash
POST /api/users/portfolio/upload-images
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

# Form data with multiple files
files: [house-exterior.jpg, house-interior.jpg, floor-plan.jpg]
```

#### Sample Response (200 OK)
```json
{
  "imageUrls": [
    "https://minio.example.com/hub-files/portfolio/123/house-exterior.jpg?checksum=abc123",
    "https://minio.example.com/hub-files/portfolio/123/house-interior.jpg?checksum=def456",
    "https://minio.example.com/hub-files/portfolio/123/floor-plan.jpg?checksum=ghi789"
  ]
}
```

## Error Response Examples

### 400 Bad Request - Only consultants can create portfolios
```json
{
  "timestamp": "2025-01-15T08:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Only consultants can create portfolios",
  "path": "/api/users/portfolio"
}
```

### 401 Unauthorized - Invalid token
```json
{
  "timestamp": "2025-01-15T08:30:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token tidak valid atau tidak aktif",
  "path": "/api/users/portfolio"
}
```

### 404 Not Found - Portfolio not found
```json
{
  "timestamp": "2025-01-15T08:30:00.000Z",
  "status": 404,
  "error": "Not Found",
  "message": "Portfolio not found",
  "path": "/api/users/consultants/123/portfolio"
}
```

## Authentication Notes

- All portfolio endpoints require JWT authentication
- Include `Authorization: Bearer {jwt_token}` header in all requests
- Consultant-specific endpoints validate that the user is a consultant
- Homeowners can view consultant portfolios but cannot modify them

## Data Validation Rules

- **projectName**: Required, max 255 characters
- **price**: Optional, must be positive BigDecimal
- **detailDescription**: Optional, text field
- **imageUrls**: Optional array of valid URLs
- **bio**: Optional, max 500 characters
- **specialization**: Optional, max 100 characters
- **experience**: Optional, max 100 characters
- **education**: Optional, max 255 characters
- **certifications**: Optional, max 255 characters
- **linkedinUrl**: Optional, valid URL format
- **websiteUrl**: Optional, valid URL format

## Portfolio Status Logic

- **DRAFT**: Initial status when portfolio is created
- **INCOMPLETE**: Has basic info but no projects
- **COMPLETED**: Has basic info and at least one project

This completes the comprehensive sample requests and responses for all portfolio endpoints in the Hub API v2.1.