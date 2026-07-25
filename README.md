# Bank Loan Management System

A modular, console-based (menu-driven) Java application for managing bank
customers and loans, built with **Java, JDBC, and MySQL**. Designed as a
final-year Computer Science project demonstrating layered architecture,
OOP design, and real-world business rule automation.

***LIVE LINK : https://bank-loan-management-production.up.railway.app/***

## Features

- **Authentication** — Admin/Employee login with SHA-256 hashed passwords, role-based access.
- **Customer Management** — Register, update, search, list, and delete customer profiles.
- **Configurable Loan Categories** — HOME, PERSONAL, CAR, EDUCATION, BUSINESS, each with
  its own interest rate, amount/tenure limits, and minimum credit score (stored in DB,
  not hardcoded — true "bank policy" configuration).
- **Automated Eligibility Engine** — Checks requested amount, tenure, credit score,
  employment status, income-based cap, and debt-to-income (EMI ≤ 50% of income) rules,
  producing a clear list of rejection reasons when a loan doesn't qualify.
- **EMI Calculation** — Standard reducing-balance EMI formula implemented with `BigDecimal`
  for financial precision.
- **Loan Approval / Rejection** — Automatic decisioning with audit trail (who processed it, when).
- **Repayment Tracking** — Auto-generated monthly repayment schedule, installment payment
  recording, overdue detection, and outstanding balance calculation.
- **Reports** — Loan portfolio summary, category-wise disbursement, approved/rejected lists.
- **Exception Handling** — Custom checked exceptions (`ValidationException`,
  `RecordNotFoundException`, `AuthenticationException`, `LoanProcessingException`) instead
  of leaking raw SQL/runtime errors to the user.
- **Input Validation** — Regex-based validation for email, phone, PAN, Aadhaar, age, ranges.

## Architecture (Layered / MVC-ish)

```
src/
├── model/       -> Plain Java objects (User, Customer, LoanCategory, LoanApplication, Repayment)
├── dao/         -> JDBC data-access classes (one per table), all using PreparedStatements
├── service/     -> Business logic (AuthService, CustomerService, LoanService,
│                    EligibilityEngine, RepaymentService, ReportService)
├── util/        -> Cross-cutting helpers (DBConnection, PasswordUtil, ValidationUtil, EMICalculator)
├── exception/   -> Custom checked exceptions
└── main/        -> BankLoanApp.java (console menu-driven UI / entry point)
```

This separation means the UI layer (`main`) never touches JDBC directly, and the
`EligibilityEngine` can be unit-tested independently of the database.

## Database Schema

See [`sql/schema.sql`](sql/schema.sql). Tables: `users`, `customers`, `loan_categories`,
`loan_applications`, `repayments`. Includes seed data for two staff accounts and five loan
categories.

## Setup Instructions

### 1. Prerequisites
- JDK 11 or later
- MySQL 8.x Server
- [MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/) (JDBC driver JAR) — place it in `lib/`

### 2. Create the database
```bash
mysql -u root -p < sql/schema.sql
```

### 3. Configure the connection
Edit `src/util/DBConnection.java` and set your MySQL username/password:
```java
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "your_password_here";
```

### 4. Compile
```bash
mkdir -p bin
javac -d bin -cp "lib\mysql-connector-j-9.7.0.jar" src\*.java

```

### 5. Run
```bash
java -cp "bin;lib/mysql-connector-j-9.7.0.jar" main.BankLoanApp
```
(On Windows, replace `:` with `;` in the classpath.)

## Default Login Credentials

| Username   | Password  | Role     |
|------------|-----------|----------|
| admin      | admin123  | ADMIN    |
| employee1  | emp123    | EMPLOYEE |

*(Change these immediately in a real deployment — passwords are stored as SHA-256 hashes.)*

## Eligibility Rules Implemented

A loan is **auto-approved** only if **all** of the following hold:
1. The loan category is active.
2. Requested amount is within the category's `[minAmount, maxAmount]`.
3. Tenure is within the category's `[minTenureMonths, maxTenureMonths]`.
4. Customer's credit score ≥ category's `minCreditScore`.
5. Customer's employment type is not `UNEMPLOYED`.
6. Requested amount ≤ `monthlyIncome × 12 × maxIncomeMultiplier` (category-specific cap).
7. Calculated EMI ≤ 50% of the customer's monthly income (debt-to-income safety check).

Any failed rule is collected and returned as a rejection reason — multiple reasons can
be shown at once so the employee understands exactly why an application failed.

## Extending the Project

- Add a Swing GUI layer on top of the existing `service` classes (they contain zero
  console-specific code, so they're reusable as-is).
- Add JUnit tests against `EligibilityEngine` and `EMICalculator` (pure logic, no DB needed).
- Add PDF report export (e.g. using iText) built on top of `ReportService`.
- Add multi-branch support by adding a `branch_id` column and filtering queries.

## Notes for Evaluators

- All SQL uses `PreparedStatement` — no string concatenation, so it is safe from SQL injection.
- Passwords are never stored or compared in plain text.
- `BigDecimal` is used throughout for all monetary values to avoid floating-point rounding errors.
- The DAO layer only knows about SQL; the service layer only knows about business rules;
  the `main` package only knows about console I/O — a clean separation of concerns.
