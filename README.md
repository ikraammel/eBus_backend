# 🚌 eBus — Backend API

**eBus Backend** is a RESTful API built with **Java 17 and Spring Boot**, designed to support a smart transportation management platform.

It handles business logic, user authentication, role-based access control, transportation management, subscriptions, payments, and administrative operations.

## 🛠️ Tech Stack

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.2.6-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![Stripe](https://img.shields.io/badge/Stripe-635BFF?style=for-the-badge&logo=stripe&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Render](https://img.shields.io/badge/Render-000000?style=for-the-badge&logo=render&logoColor=white)

## 🔗 Repositories & Deployment

| Component | GitHub Repository | Deployment |
|---|---|---|
| ⚙️ Backend API | [eBus Backend](https://github.com/ikraammel/eBus_backend) | [Render API](https://ebus-backend-ao2u.onrender.com) |
| 📱 Flutter Frontend | [eBus Frontend](https://github.com/ikraammel/eBus_frontend) | [Live Application](https://ebus-frontend-1an4.onrender.com/) |
| 🧪 QA Automation | [eBus QA Automation](https://github.com/FATIMA-BOUSMITI/eBus-qa-automation) | Local & Automated Testing |

## ⚙️ Backend Features

- **Authentication & Authorization:** JWT-based authentication and role-based access control.
- **Transportation Management:** Management of buses, routes, stations, and schedules.
- **Subscriptions & Payments:** Subscription management and Stripe payment integration.
- **Lost & Found:** Reporting, tracking, and managing lost items.
- **Complaints Management:** Submission and processing of user complaints.
- **Admin Dashboard:** Operational statistics, KPIs, and recent activity.
- **Email Services:** SMTP integration for application notifications.
- **REST APIs:** Structured endpoints for communication with the Flutter application.

## 🏗️ Architecture

The backend follows a layered Spring Boot architecture:

```text
REST Controllers
       ↓
Service Layer
       ↓
Repository Layer (Spring Data JPA)
       ↓
PostgreSQL Database
```

**Additional integrations:** JWT Security, Stripe API, and SMTP email services.

## 📁 Project Structure

```text
eBus_backend/
├── src/
│   ├── main/
│   │   ├── java/com/gestion/gestion_bus/
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
├── .github/workflows/
├── Dockerfile
├── pom.xml
└── README.md
```

## 🚀 Getting Started

**Prerequisites:** Java 17, Maven, and PostgreSQL.

**1. Clone the repository**

```bash
git clone https://github.com/ikraammel/eBus_backend.git
cd eBus_backend
```

**2. Configure environment variables**

Configure the required credentials using environment variables or a local, untracked `.env` file.

```env
DB_URL=jdbc:postgresql://localhost:5432/ebus
DB_USERNAME=your_username
DB_PASSWORD=your_password
JWT_SECRET=your_jwt_secret
MAIL_USERNAME=your_email
MAIL_PASSWORD=your_app_password
STRIPE_SECRET_KEY=your_stripe_secret
STRIPE_WEBHOOK_SECRET=your_webhook_secret
```

Never commit actual credentials to GitHub.

**3. Build and run**

```bash
mvn clean package -DskipTests
java -jar target/gestion-bus-0.0.1-SNAPSHOT.jar
```

**4. Check API health**

```text
GET http://localhost:8080/api/health
```

## 🐳 Docker

Build the backend image:

```bash
docker build -t ebus-backend .
```

The application can be deployed using Docker with the required environment variables and an accessible PostgreSQL database.

## 🧪 Backend Testing

The backend includes automated tests using **JUnit 5, Mockito, and Spring Boot Test**.

Run tests with:

```bash
mvn test
```

For end-to-end, mobile, API, and performance testing, visit the [QA Automation Repository](https://github.com/FATIMA-BOUSMITI/eBus-qa-automation).

## 🔐 Security

- JWT authentication and role-based authorization.
- Sensitive credentials managed through environment variables.
- Database access configured securely.
- Secrets excluded from version control.
- Separate configurations for development and production.

## 📄 Related Projects

- [eBus Flutter Application](https://github.com/ikraammel/eBus_frontend)
- [eBus QA Automation](https://github.com/FATIMA-BOUSMITI/eBus-qa-automation)

---

**eBus — Smart Transportation Management System**
