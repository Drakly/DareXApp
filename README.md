DareXApp
DareXApp is a modern application that manages users, wallets, cards, transactions, subscriptions, and a referral system. Built using Java 17 and Spring Boot, the project features a modular architecture that cleanly separates different business concerns while ensuring scalability and ease of maintenance.

Note: The ADMIN role is exclusively assigned through the database and cannot be set during user registration.

Features
User Registration & Management

Register users with a unique email and username.
Manage user profiles with dedicated services.
Security: ADMIN roles are managed solely in the database.
Wallets & Transactions

Create and manage wallets through the WalletService.
Perform deposits, withdrawals, and transfers via the TransactionService.
Cards

Generate virtual and physical cards using the CardService.
Automatically generate MasterCard-compliant card numbers using the Luhn algorithm.
Generate CVV codes for card security.
Subscriptions

Create a basic subscription and upgrade it using the SubscriptionService.
Manage subscription statuses (Active, Completed, Terminated).
Referral System

Generate unique referral codes and track click counts using the ReferralService.
Bank Statement Generation

Generate bank statement PDFs using Thymeleaf templates and ITextRenderer.
Technologies
Java 17
Spring Boot – for building robust and scalable applications.
Spring Data JPA – for database interactions.
Thymeleaf – for dynamic HTML template generation.
ITextRenderer – for converting HTML to PDF.
JUnit 5 and Mockito – for unit and integration testing.
Lombok – to reduce boilerplate code.
Installation & Running the Application
Clone the repository:

bash
Копиране
git clone https://github.com/yourusername/darexapp.git
cd darexapp
Build and run with Maven:

bash
Копиране
mvn clean install
mvn spring-boot:run
Alternatively, if using Gradle:

bash
Копиране
./gradlew clean build
./gradlew bootRun
Configuration:
The application uses the standard application.properties (or application.yml) for configuration. By default, it can run with an in-memory H2 database, but you may also configure it to use MySQL, PostgreSQL, or another database by adjusting the settings accordingly.

Testing
Run the tests using:

bash
Копиране
mvn test
or with Gradle:

bash
Копиране
./gradlew test
Integration tests load the Spring context and use an in-memory database to verify that components interact correctly.

Project Structure
org.darexapp.user – User management and profile handling.
org.darexapp.wallet – Wallet management and transaction processing.
org.darexapp.card – Card generation and management functionalities.
org.darexapp.subscription – Subscription creation, upgrades, and status management.
org.darexapp.transaction – Transaction processing services.
org.referralsvc.referral – Referral system and code management.
org.darexapp.statement – PDF bank statement generation.
Contributing
Contributions are welcome! If you wish to contribute:

Fork the repository.
Create a branch for your feature or bug fix.
Submit a pull request with detailed information about your changes.
License
This project is licensed under the MIT License.
