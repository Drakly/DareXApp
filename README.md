# DareXApp 🚀

**DareXApp** is a next-generation financial platform built with Java 17 and Spring Boot. It combines powerful features like user management, digital wallets, card generation, subscription services, and a referral system into one sleek, modular application.

> **Note:** The **ADMIN** role is exclusively assigned via the database and cannot be set during user registration.

---

## Table of Contents

- [Features](#features)
- [Technologies](#technologies)
- [Installation & Running](#installation--running)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## Features

- **User Management & Registration**  
  - Register with a unique email and username.
  - Manage profiles and secure user data.
  - **ADMIN Role:** Only assigned through the database.

- **Wallets & Transactions**  
  - Create and manage digital wallets.
  - Seamlessly perform deposits, withdrawals, and transfers.

- **Cards**  
  - Generate both **virtual** and **physical** cards.
  - Automatically generate MasterCard-compliant card numbers (using the Luhn algorithm) and CVV codes.

- **Subscriptions**  
  - Start with a basic subscription and upgrade to premium tiers.
  - Manage subscription statuses: Active, Completed, or Terminated.

- **Referral System**  
  - Generate unique referral codes and track click counts.
  - Boost user engagement with an intuitive referral mechanism.

- **Bank Statement Generation**  
  - Generate PDF bank statements using Thymeleaf and ITextRenderer.
  - Customize and share financial summaries with ease.

---

## Technologies

- **Java 17** – Cutting-edge language features and performance improvements.
- **Spring Boot** – Rapid application development with a modular architecture.
- **Spring Data JPA** – Streamlined database interactions.
- **Thymeleaf** – Dynamic and maintainable HTML templating.
- **ITextRenderer** – Convert HTML to high-quality PDFs.
- **JUnit 5 & Mockito** – Comprehensive testing suite for unit and integration tests.
- **Lombok** – Reduces boilerplate code for a cleaner codebase.

---

## Installation & Running

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/yourusername/darexapp.git
   cd darexapp
   ```
   
Configuration
The application uses the standard application.properties (or application.yml) file for configuration. It defaults to an in-memory H2 database but can be easily configured for MySQL, PostgreSQL, etc.

Testing
Run all tests with:
  ```bash
    mvn test
```
PROJECT STRUCTURE
```
├── src/main/java
│   ├── org/darexapp/user         # User management and profile handling
│   ├── org/darexapp/wallet       # Wallet and transaction services
│   ├── org/darexapp/card         # Card generation and management
│   ├── org/darexapp/subscription # Subscription and upgrade services
│   ├── org/darexapp/transaction  # Transaction processing services
│   ├── org/referralsvc/referral  # Referral system and code management
│   └── org/darexapp/statement    # Bank statement PDF generation
└── src/test/java                # Unit and integration tests
```

License
This project is licensed under the MIT License.
