# Notification Microservices Project

A lightweight microservices-based system built with **Java Spring Boot**, demonstrating key production-grade concepts such as service communication, API gateway, service discovery, configuration management, rate limiting, fault tolerance, Redis OTP management, PostgreSQL persistence, unit testing with Mockito, and email notifications using JavaMail.

---

## 🏗️ **Microservices Included**

### 1. **User Service**

* Handles user registration and login.
* Generates OTP using Redis.
* Calls Notification Service using **OpenFeign**.
* Connected to PostgreSQL.
* Includes unit tests using **Mockito**.

### 2. **Notification Service**

* Sends OTP emails using **JavaMailSender**.
* Uses Feign client to accept OTP send requests.
* Has fault-tolerant endpoints protected with:

  * **Circuit Breaker**
  * **Rate Limiter**
  * **Retry** (optional)
* Unit tests written using **Mockito**.

### 3. **API Gateway**

* Built using **Spring Cloud Gateway**.
* Routes incoming requests to User Service and Notification Service.
* Provides centralized entry point.
* Supports path-based routing.

### 4. **Config Server**

* Central configuration management for all services.
* Stores config files (application.yml) in a separate Git repository.

### 5. **Service Registry (Eureka Server)**

* Enables service discovery.
* All microservices register themselves here.
* Gateway uses Eureka to route dynamically.

---

## 🧰 **Technologies Used**

### **Backend**

* Java 17
* Spring Boot
* Spring Cloud Netflix Eureka
* Spring Cloud Gateway
* Spring Cloud Config
* Spring Data JPA
* Spring Retry / Resilience4J (fault tolerance)
* Spring Mail (JavaMailSender)

### **Databases**

* **Redis** (OTP Storage)
* **PostgreSQL** (User Data)

### **Testing**

* JUnit 5
* Mockito

### **Communication**

* OpenFeign (User Service → Notification Service)

---

## 📌 **Architecture Overview**

```
Client → API Gateway → [ User Service → Notification Service ]
                 ↓                 ↑
            Service Registry ←→ Config Server

User Service → Redis (OTP)
User Service → PostgreSQL (User Data)
```

---

## ⚙️ **Key Features**

### ✔️ **OTP Registration Flow**

1. User hits `/send-otp` endpoint.
2. User Service generates an OTP using Redis.
3. User Service calls Notification Service via Feign.
4. Notification Service sends OTP email using JavaMail.
5. User verifies OTP.

### ✔️ **Fault Tolerance**

* Circuit breaker around NotificationService calls.
* Rate limiter prevents spamming OTP requests.
* Retry mechanism handles temporary failures.

### ✔️ **Scalable Communication**

* Feign client replaces manual RestTemplate.
* Eureka-based discovery removes hardcoded URLs.

### ✔️ **Central Config Management**

* All microservices load properties from Config Server.

### ✔️ **Mockito-Based Unit Tests**

* User Service: OTP generation, validation.
* Notification Service: Email send mocking.

---

## 🚀 **How to Run the Project**

### 1. **Start Config Server**

```
cd configserver
mvn spring-boot:run
```

### 2. **Start Eureka Server (Service Registry)**

```
cd serviceregistry
mvn spring-boot:run
```

### 3. **Start API Gateway**

```
cd apigateway
mvn spring-boot:run
```

### 4. **Start User Service**

```
cd userservice
mvn spring-boot:run
```

### 5. **Start Notification Service**

```
cd notificationservice
mvn spring-boot:run
```

---

## 🗄️ **Environment Variables**

```
POSTGRES_DB=yourdb
POSTGRES_USER=postgres
POSTGRES_PASSWORD=yourpassword
REDIS_HOST=localhost
REDIS_PORT=6379
MAIL_USERNAME=your-email
MAIL_PASSWORD=yourpassword
```

---

## 🧪 **Running Tests**

```
mvn test
```

Tests include:

* OTPService unit tests
* Feign call mocking tests
* Email send mock tests

---

## 📖 **Future Improvements**

* Add JWT authentication
* Add Docker Compose
* Add Prometheus + Grafana monitoring
* Add SMS-based OTP support

---

## 🐳 **Docker & Deployment**

### **Individual Dockerfiles**

Each service contains its own `Dockerfile` to build independent images:

* User Service
* Notification Service
* API Gateway
* Config Server
* Service Registry

This allows containerizing and deploying every microservice separately.

### **Docker Compose Setup**

A root-level `docker-compose.yml` is included to run the entire system with a single command. It orchestrates:

* All microservices
* PostgreSQL database
* Redis server
* Networks & environment variables

#### Run everything:

```
docker-compose up --build
```

#### Stop everything:

```
docker-compose down
```

---

## 🎯 **Conclusion**

This project may be small, but it includes **all essential microservice concepts** used in real enterprise projects—perfect for learning, portfolio, and interview preparation.
