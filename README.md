# 📝 Note Keeper Service

A modern REST API for managing notes with JWT authentication, built with Spring Boot 3.x and Java 21.

## 🏗️ Architecture

```
noteKeeper/
├── 📁 java-service/notekeeperService/src/main/java/org/balczukiewicz/notekeeperservice/
│   ├── 🔐 config/          # Security, Cache, OpenAPI configuration
│   ├── 🎮 controller/      # REST API endpoints
│   ├── 📋 dto/             # Data Transfer Objects
│   ├── 🏛️ entity/          # JPA entities (Note, User, Role)
│   ├── ❌ exception/       # Custom exceptions & handlers
│   ├── 🗄️ repository/      # Data access layer
│   └── ⚙️ service/         # Business logic
├── 📁 python-client/       # Python test client
│   ├── 🐍 fetch_notes.py   # Python API client
│   └── 📋 requirements.txt # Python dependencies
├── 🐳 Dockerfile          # Container configuration
├── 🐙 docker-compose.yml  # Docker orchestration
└── 📖 README.md           # This file
```

## 🚀 Quick Start with Docker

### Prerequisites
- Docker & Docker Compose installed

### Run the Application
```bash
# Clone and navigate to project
git clone 
cd notekeeperService

# Build the application
docker compose build

# Start the application
docker compose up -d

# View logs
docker compose logs -f notekeeper

# Stop the application
docker compose down
```

## 🌐 API Endpoints

Once running, access these endpoints:

| Service | URL | Description |
|---------|-----|-------------|
| **🏠 Application** | http://localhost:8080 | Main application |
| **📚 Swagger UI** | http://localhost:8080/swagger-ui.html | Interactive API documentation |
| **📋 OpenAPI Docs** | http://localhost:8080/v3/api-docs | API specification (JSON) |
| **🗄️ H2 Console** | http://localhost:8080/h2-console | Database console |

### H2 Database Console Access
- **JDBC URL**: `jdbc:h2:mem:notekeeper`
- **Username**: `sa`
- **Password**: *(leave empty)*

## 🔐 Authentication

The API uses JWT (JSON Web Tokens) for authentication.

### Default Users
- **Admin**: `admin` / `password`
- **User**: `user` / `password`

### Get JWT Token
```bash
curl -X POST http://localhost:8080/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

## 📝 API Usage Examples

### Create a Note
```bash
# Replace YOUR_JWT_TOKEN with the token from authentication
curl -X POST http://localhost:8080/api/v1/notes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Note",
    "content": "This is the content of my note!"
  }'
```

### Get All Notes
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/notes
```

### Get Specific Note
```bash
curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  http://localhost:8080/api/v1/notes/1
```

## 🐍 Python Test Client

### Prerequisites
- Python 3.7+ installed
- pip package manager

### Setup Python Client
```bash
# Navigate to python client directory
cd python-client

# Install dependencies
pip install -r requirements.txt

# Or install manually
pip install requests==2.31.0 urllib3==2.0.7
```

### Run Python Test Script
```bash
# Make sure your Note Keeper Service is running (docker-compose up -d)
# Then run the Python client
python fetch_notes.py
```

### What the Python Script Does
The `fetch_notes.py` script will:
1. **🔐 Authenticate** with the API using admin credentials
2. **📝 Create** 5 sample notes with different titles and content
3. **📥 Retrieve** all notes from the API
4. **📊 Generate** a detailed report showing:
   - Total number of notes
   - Notes with titles starting with vowels
   - Complete details of all notes

### Expected Output
```
Note Keeper API Client
==============================
Attempting to authenticate with username: admin
Authentication successful for user: admin

Creating sample notes...
Created note with ID: 1
Created note with ID: 2
Created note with ID: 3
Created note with ID: 4
Created note with ID: 5

Retrieving notes...
Successfully retrieved 5 notes

==================================================
 NOTES REPORT
==================================================
Total notes: 5
Titles starting with vowel:
 - An Important Note
 - Emergency Contact
 - Ideas for Weekend

==================================================
 ALL NOTES DETAILS
==================================================
1. ID: 1
   Title: An Important Note
   Content: This is a very important note about something.

2. ID: 2
   Title: Emergency Contact
   Content: Call John at 555-1234 in case of emergency.

[... more notes ...]

Client execution completed successfully!
```

## ⚡ Features

- **🔐 JWT Authentication** - Secure token-based authentication
- **📝 Note Management** - Create, read, and retrieve notes
- **⚡ Caching** - Performance optimization with Spring Cache
- **📚 API Documentation** - Interactive Swagger UI
- **🏥 Health Checks** - Built-in monitoring endpoints
- **🗄️ H2 Database** - In-memory database for development
- **🐳 Docker Ready** - Containerized deployment
- **🐍 Python Client** - Ready-to-use Python test client
- **🧪 Comprehensive Testing** - Unit and integration tests
- **🔒 Security Headers** - Protection against common web vulnerabilities


## 🚧 Further Improvements

> 💡 *If I had more time, this is what I would tackle next.*

| Priority | Improvement | Why it matters |
|----------|-------------|----------------|
| 🔄 **Switch to PostgreSQL** | Replace the in‑memory H2 database with PostgreSQL (already defined in `docker‑compose.yml`). | Real, durable storage suitable for production and more reliable integration tests. |
| 👤➡️📝 **User ↔ Note relationship** | Add a `OneToMany` mapping so each note is owned by a specific user. | Enables proper access control—users see only their own notes. |
| 🔒 **Hash user passwords** | Store passwords hashed with BCrypt/Argon2 instead of plaintext. | Aligns with OWASP recommendations and eliminates the risk of leaking raw passwords. |
| 📄 **Pagination & sorting** | Implement `GET /notes?page=&size=&sort=` using Spring’s `Pageable`. | Keeps response sizes manageable and scales gracefully to thousands of notes. |
| ✏️ **Update & delete endpoints** | Implement `POST /register-user`  `PUT /notes/{id}` and `DELETE /notes/{id}` (optionally soft‑delete). | Completes full authetntication support and CRUD support for notes and matches REST conventions. |
