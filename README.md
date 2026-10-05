# Smart Solar Microgrid Trading System

Smart Solar is an end-to-end client-server application for managing solar microgrid stations, energy reservations, prosumer accounts, and energy-transfer transactions.

The system consists of:

- ASP.NET Core REST Web API
- React.js web application
- Native Android mobile application
- MongoDB Atlas centralized database
- SQLite local Android persistence

The backend follows a FAT Service architecture where business rules and centralized data processing are handled by the Web API.

---

## Live Deployment

### Web Application

https://smart-solar-frontend.vercel.app

The React frontend is deployed using **Vercel**.

### Backend API

https://smartsolar.eastasia.cloudapp.azure.com

### Swagger API Documentation

https://smartsolar.eastasia.cloudapp.azure.com/swagger

The ASP.NET Core Web API is deployed on a **Microsoft Azure Windows Server VM using IIS** and secured using HTTPS.

---

## System Architecture

```text
                    MongoDB Atlas
                         ↑
                         |
                ASP.NET Core Web API
                  Azure Windows VM
                        IIS
                       HTTPS
                         |
              ┌──────────┴──────────┐
              │                     │
              ↓                     ↓
       React Web App          Native Android App
          Vercel                    Java
                                      |
                                    SQLite
```

The web and Android applications communicate with the centralized backend exclusively through REST API calls.

---

## Project Structure

```text
EAD_Project/
│
├── backend/
│   └── SmartSolar.Api/
│       ├── Controllers/
│       ├── DTOs/
│       ├── Models/
│       ├── Services/
│       ├── Data/
│       ├── Configuration/
│       └── Program.cs
│
├── web/
│   └── smart-solar-web/
│       ├── public/
│       ├── src/
│       ├── package.json
│       └── vite.config.js
│
├── mobile/
│   └── SmartSolarMobile/
│       └── app/
│           └── src/main/
│               ├── java/com/smartsolar/
│               └── res/
│
├── docs/
├── deployment/
└── README.md
```

---

## User Roles

### Backoffice

Backoffice users can:

- Log in using role-based authentication
- Manage Backoffice and Grid Operator accounts
- View and manage Prosumer accounts
- Approve pending Prosumer registrations
- Activate, deactivate and reactivate accounts
- Create and manage solar microgrid stations
- Manage booking slots
- Monitor reservations
- Approve energy reservations
- View system statistics and dashboards

### Grid Operator

Grid Operators can:

- Log in through the web or mobile application
- View operational dashboards
- Monitor reservations
- Scan Prosumer transaction QR codes
- Verify reservation information with the backend
- Confirm completion of energy transfers
- View completed transaction information

### Prosumer

Prosumers can:

- Register using the Android application
- Log in after account approval
- View and update their profile
- Request account deactivation
- Change or recover their password
- Discover nearby solar stations
- View stations using Google Maps
- Search and filter stations
- Create energy reservations
- Update eligible reservations
- Cancel eligible reservations
- View current, pending and historical reservations
- Generate transaction QR codes for approved reservations

---

## Main Features

### Authentication and Account Management

- JWT authentication
- Role-based authorization
- Backoffice and Grid Operator account management
- Prosumer registration
- Pending account approval
- Profile management
- Account activation and deactivation
- Password changing
- Email-based password recovery
- Role-specific dashboards

### Solar Station Management

- Create and update microgrid stations
- Store geographical coordinates
- Manage capacity and energy slots
- Manage opening and closing times
- Activate and deactivate stations
- Prevent invalid deactivation when active reservations exist
- Address search using Geoapify
- Nearby station visualization using Google Maps

### Reservation Management

- Create energy reservations
- Update reservations
- Cancel reservations
- Approve pending reservations
- Search and filter bookings
- View booking history
- View reservation status

Reservation business rules include:

- Reservations must be scheduled within 7 days
- Updates and cancellations require at least 12 hours' notice

### QR Verification Workflow

- Secure transaction QR generation
- HMAC-SHA256 signed QR payload
- QR scanning using the Android application
- Server-side QR verification
- Reservation ownership and status verification
- Energy-transfer completion
- Completed transaction history

---

## Technology Stack

| Technology | Purpose |
|---|---|
| ASP.NET Core Web API | Centralized REST backend |
| C# | Backend development |
| React.js | Web application development |
| Vite | Web development and build tool |
| Bootstrap 5 | Responsive web UI |
| Java | Native Android development |
| Android SDK | Mobile application development |
| MongoDB Atlas | Centralized NoSQL database |
| SQLite | Android local persistence |
| JWT | Authentication and authorization |
| Retrofit | Android REST API communication |
| Axios | React REST API communication |
| Swagger / OpenAPI | API documentation and testing |
| Google Maps SDK | Nearby station visualization |
| Geoapify | Address search and geocoding |
| ZXing | QR generation and scanning |
| Git / GitHub | Version control and collaboration |
| Windows IIS | ASP.NET Core Web API hosting and deployment |
| Microsoft Azure | Windows Server VM hosting |
| Vercel | React frontend deployment |
| Let's Encrypt / win-acme | HTTPS certificate management |

---

## Database

### MongoDB Atlas

MongoDB Atlas is used as the centralized server-side database.

Main collections include:

```text
Users
Prosumers
Stations
EnergyBookingSlots
EnergyReservations
PasswordResetTokens
```

All centralized business data is accessed through the Web API.

### SQLite

The Android application uses:

```text
smartsolar.db
```

The main local table is:

```text
user_session
```

It stores local session information including:

```text
id
token
user_id
name
email
role
reference_id
```

SQLite is used only for local persistence. Business data and business rules remain centralized in the Web API.

---

## API Configuration

The production API address is:

```text
https://smartsolar.eastasia.cloudapp.azure.com/api
```

### React

The web application uses:

```env
VITE_API_BASE_URL=https://smartsolar.eastasia.cloudapp.azure.com/api
```

Production environment variables are configured through Vercel.

### Android

The Retrofit production base URL is:

```text
https://smartsolar.eastasia.cloudapp.azure.com/api/
```

Because the API is publicly available through HTTPS, the Android application can connect using an emulator or physical device with an Internet connection.

---

## Backend Environment Variables

Sensitive configuration values are not committed to GitHub.

The backend requires configuration similar to:

```env
MONGODB_CONNECTION_STRING=
MONGODB_DATABASE_NAME=

JWT_KEY=
JWT_ISSUER=
JWT_AUDIENCE=
JWT_EXPIRY_MINUTES=

SEED_ADMIN_EMAIL=
SEED_ADMIN_PASSWORD=

QR_SECRET=

WEB_BASE_URL=

SMTP_HOST=
SMTP_PORT=
SMTP_USERNAME=
SMTP_PASSWORD=
SMTP_FROM=

GEOAPIFY_API_KEY=
```

Real secrets must be configured using secure server-side environment variables.

Do not commit production `.env` files or credentials.

---

## Running the Backend Locally

Navigate to:

```powershell
cd backend\SmartSolar.Api
```

Restore dependencies:

```powershell
dotnet restore
```

Run the API:

```powershell
dotnet run
```

The local Swagger URL depends on the active launch profile.

---

## Running the Web Application Locally

Navigate to:

```powershell
cd web\smart-solar-web
```

Install dependencies:

```powershell
npm install
```

Create `.env.local`:

```env
VITE_API_BASE_URL=https://smartsolar.eastasia.cloudapp.azure.com/api
```

Start the Vite development server:

```powershell
npm run dev
```

Open:

```text
http://localhost:5173
```

---

## Running the Android Application

Navigate to:

```powershell
cd mobile\SmartSolarMobile
```

Build the debug application:

```powershell
.\gradlew.bat assembleDebug
```

The APK will be generated at:

```text
app\build\outputs\apk\debug\app-debug.apk
```

The APK can be installed on an Android emulator or physical Android device.

For Google Maps functionality, configure the required Maps SDK API key using the local Android configuration.

API keys must not be committed to GitHub.

---

## Deployment

### Backend

The ASP.NET Core backend is deployed using:

```text
Microsoft Azure Windows Server VM
        ↓
Internet Information Services (IIS)
        ↓
ASP.NET Core Web API
```

The .NET ASP.NET Core Hosting Bundle is installed on the Windows Server.

The deployed backend application directory is:

```text
C:\SmartSolar\API
```

HTTPS is configured using Let's Encrypt and win-acme.

### Frontend

The React frontend is deployed using Vercel:

https://smart-solar-frontend.vercel.app

### Database

Centralized database hosting is provided using MongoDB Atlas.

---

## Git Repository

Main integrated repository:

https://github.com/Kehara04/EAD_Project.git

Frontend deployment repository:

https://github.com/MethmiHP/SmartSolar-Frontend.git

The `main` branch contains the final integrated version of the project.

Youtube Demo Video Link:

https://youtu.be/gsATAr4SM7o?si=mDLIdFBzjKkBXNbT

---

## Main Demonstration Flow

The final demonstration covers:

1. User authentication and role-based access
2. Backoffice account management
3. Prosumer registration and approval
4. Station and booking-slot management
5. Nearby-station map functionality
6. Reservation creation and approval
7. Reservation updating and cancellation
8. Transaction QR generation
9. Grid Operator QR scanning and verification
10. Energy-transfer completion
11. MongoDB and SQLite persistence
12. Azure IIS and Vercel deployment

---

## Security

The project includes:

- JWT authentication
- Role-based authorization
- Password hashing
- Server-side business-rule validation
- Secure environment-variable configuration
- HTTPS communication
- Signed QR transaction payloads
- Restricted account operations based on role
- Server-side QR verification

Sensitive values such as API keys, passwords, database connection strings, JWT keys and SMTP credentials are excluded from source control.

---

## Development and Version Control

Git and GitHub were used throughout development.

The team worked with separate development branches before integrating the completed components into the final `main` branch.

Meaningful commits were used to document development and integration work.

---

## Team Members and Individual Contributions

| IT Number | Component | Main Contribution |
|---|---|---|
| IT23227422 | Authentication and Account Management | Authentication, JWT authorization, account management, Prosumer registration and activation, profile management, password management, backend/web/mobile integration |
| IT23209916 | Station and Microgrid Management | Station management, energy booking slots, availability, address search, geolocation and maps integration |
| IT23193536 | Operator, QR and Dashboard | QR generation, QR scanning, server-side verification, transaction completion and operator dashboards |
| IT23202740 | Reservation and Booking Management | Reservation creation, updates, cancellations, approvals, booking views and reservation business rules |

---
