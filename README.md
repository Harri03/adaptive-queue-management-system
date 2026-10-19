# Adaptive Queue Management System

## Project Overview

This project is a queue management system designed to simplify customer registration and queue handling. It allows customers to join a queue, receive a token number, view their queue position, and check their estimated waiting time. The system uses a Spring Boot backend, a MySQL database, and React-based interfaces for customers and office staff.

## Features

- Customer queue registration
- Token number generation
- Queue position tracking
- Estimated waiting time calculation
- Queue status management
- Daily queue records
- Customer and office interfaces
- QR-based customer registration *(once verified in the running application)*

## Technologies Used

- Java
- Spring Boot
- Spring Data JPA
- MySQL
- React
- JavaScript
- HTML
- CSS
- REST APIs
- Vite

## Installation

1. Download or clone the project repository.
2. Install Java, Maven, Node.js, and MySQL.
3. Configure the database connection in the backend application configuration.
4. Run the Spring Boot backend.
5. Open the office frontend folder and install dependencies using `npm install`.
6. Start the office frontend using `npm run dev`.
7. Open the customer frontend folder and install dependencies using `npm install`.
8. Start the customer frontend using `npm run dev`.
9. Open the frontend URLs provided by Vite in your browser.

## Example Workflow

- Customer joins the queue.
- The system generates a token number.
- The customer checks their queue position.
- The system estimates the waiting time.
- Office staff manage customer queue statuses.
- Completed customer records remain stored in the database.

## Project Structure

```text
adaptive-queue-management-system/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
│
├── office-frontend/
│   ├── src/
│   ├── package.json
│   └── ...
│
├── customer-frontend/
│   ├── src/
│   ├── package.json
│   └── ...
│
└── README.md
```

## Future Enhancements

- Public deployment for access from any network
- Automated customer notifications
- Improved waiting-time updates
- Enhanced queue history and reporting
- Additional accessibility improvements

## Author

Harri Ram
