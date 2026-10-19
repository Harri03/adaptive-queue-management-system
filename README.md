# Adaptive Queue Management System

## Project Overview

The Adaptive Queue Management System is a web-based application designed to simplify customer registration and queue handling. Customers can join a queue, receive a token number, view their queue position, and check their estimated waiting time. Office staff can manage customer queue statuses through a separate interface.

## Features

- Customer queue registration
- Automatic token number generation
- Queue position tracking
- Estimated waiting time calculation
- Queue status management
- Separate customer and office interfaces
- MySQL database integration
- Queue history management

## Technologies Used

**Backend**
- Java
- Spring Boot
- Spring Data JPA
- REST APIs
- Maven

**Frontend**
- React
- JavaScript
- HTML
- CSS
- Vite

**Database**
- MySQL

## Project Structure

```text
adaptive-queue-management-system/
├── adaptive-queue-management/
├── queue-frontend/
├── customer-frontend/
├── .gitignore
└── README.md
```

## Prerequisites

Install the following before running the project:

- Java Development Kit (JDK)
- Maven, or use the included Maven Wrapper if available
- Node.js and npm
- MySQL

## Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/Harri03/adaptive-queue-management-system.git
cd adaptive-queue-management-system
```

### 2. Configure the database

Create the MySQL database:

```sql
CREATE DATABASE adaptive_queue;
```

Configure the database connection using the environment variables expected by the backend:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`

The default database URL is `jdbc:mysql://localhost:3306/adaptive_queue`. Set your own database credentials locally. Do not commit passwords or other secrets to GitHub.

### 3. Run the Spring Boot backend

Open a terminal in the `adaptive-queue-management` folder and run:

```bash
mvn spring-boot:run
```

If Maven is not installed, use the project's Maven Wrapper if it is available.

### 4. Run the office frontend

Open a new terminal:

```bash
cd queue-frontend
npm install
npm run dev
```

### 5. Run the customer frontend

Open another terminal:

```bash
cd customer-frontend
npm install
npm run dev
```

Open the local URLs displayed by Vite in your terminal. Ensure the backend and MySQL database are running and that the frontend API configuration points to the correct backend address.

## Future Enhancements

- Automated customer notifications
- More responsive waiting-time updates
- Improved queue history and reporting
- Public deployment for access over the internet

## Author

Harri Ram
