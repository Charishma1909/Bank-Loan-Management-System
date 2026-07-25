-- ============================================================
-- Bank Loan Management System - Database Schema
-- Database: MySQL 8.x
-- ============================================================

DROP DATABASE IF EXISTS bank_loan_system;
CREATE DATABASE bank_loan_system;
USE bank_loan_system;

-- ------------------------------------------------------------
-- Table: users  (Bank staff: ADMIN / EMPLOYEE)
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id      INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,      -- stored as SHA-256 hash
    full_name    VARCHAR(100) NOT NULL,
    role         ENUM('ADMIN','EMPLOYEE') NOT NULL DEFAULT 'EMPLOYEE',
    is_active    BOOLEAN NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- Table: customers
-- ------------------------------------------------------------
CREATE TABLE customers (
    customer_id     INT AUTO_INCREMENT PRIMARY KEY,
    full_name       VARCHAR(100) NOT NULL,
    dob              DATE NOT NULL,
    gender          ENUM('MALE','FEMALE','OTHER') NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(15) NOT NULL UNIQUE,
    address         VARCHAR(255),
    pan_number      VARCHAR(20) NOT NULL UNIQUE,
    aadhaar_number  VARCHAR(20) NOT NULL UNIQUE,
    monthly_income  DECIMAL(12,2) NOT NULL,
    employment_type ENUM('SALARIED','SELF_EMPLOYED','BUSINESS','UNEMPLOYED') NOT NULL,
    credit_score    INT NOT NULL DEFAULT 700,   -- 300 - 900
    created_by      INT,                        -- employee who registered
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_creator FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- ------------------------------------------------------------
-- Table: loan_categories (configurable bank policies)
-- ------------------------------------------------------------
CREATE TABLE loan_categories (
    category_id         INT AUTO_INCREMENT PRIMARY KEY,
    category_name       VARCHAR(50) NOT NULL UNIQUE,   -- e.g. HOME, PERSONAL, CAR, EDUCATION
    interest_rate        DECIMAL(5,2) NOT NULL,          -- annual %, e.g. 8.50
    min_amount           DECIMAL(12,2) NOT NULL,
    max_amount           DECIMAL(12,2) NOT NULL,
    min_tenure_months    INT NOT NULL,
    max_tenure_months    INT NOT NULL,
    min_credit_score     INT NOT NULL DEFAULT 650,
    max_income_multiplier DECIMAL(5,2) NOT NULL DEFAULT 20.00, -- max loan = income*12*multiplier
    is_active            BOOLEAN NOT NULL DEFAULT TRUE
);

-- ------------------------------------------------------------
-- Table: loan_applications
-- ------------------------------------------------------------
CREATE TABLE loan_applications (
    application_id   INT AUTO_INCREMENT PRIMARY KEY,
    customer_id      INT NOT NULL,
    category_id      INT NOT NULL,
    requested_amount DECIMAL(12,2) NOT NULL,
    tenure_months    INT NOT NULL,
    interest_rate    DECIMAL(5,2) NOT NULL,       -- snapshot at application time
    emi_amount       DECIMAL(12,2),
    status           ENUM('PENDING','APPROVED','REJECTED','CLOSED') NOT NULL DEFAULT 'PENDING',
    rejection_reason VARCHAR(255),
    applied_on       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_by     INT,
    processed_on     TIMESTAMP NULL,
    CONSTRAINT fk_loan_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    CONSTRAINT fk_loan_category FOREIGN KEY (category_id) REFERENCES loan_categories(category_id),
    CONSTRAINT fk_loan_processor FOREIGN KEY (processed_by) REFERENCES users(user_id)
);

-- ------------------------------------------------------------
-- Table: repayments
-- ------------------------------------------------------------
CREATE TABLE repayments (
    repayment_id     INT AUTO_INCREMENT PRIMARY KEY,
    application_id   INT NOT NULL,
    installment_no   INT NOT NULL,
    due_amount       DECIMAL(12,2) NOT NULL,
    paid_amount      DECIMAL(12,2) NOT NULL DEFAULT 0,
    due_date         DATE NOT NULL,
    paid_date        DATE NULL,
    status           ENUM('PENDING','PAID','OVERDUE') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_repay_loan FOREIGN KEY (application_id) REFERENCES loan_applications(application_id)
);

-- ------------------------------------------------------------
-- Seed data
-- ------------------------------------------------------------

-- Default admin (password: admin123 -> SHA-256 hashed by application on first run/insert)
-- Hash below corresponds to "admin123"
INSERT INTO users (username, password, full_name, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a', 'System Administrator', 'ADMIN');

-- Sample employee (password: emp123)
INSERT INTO users (username, password, full_name, role) VALUES
('employee1', '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d', 'John Employee', 'EMPLOYEE');

-- Loan categories (bank policy configuration)
INSERT INTO loan_categories (category_name, interest_rate, min_amount, max_amount, min_tenure_months, max_tenure_months, min_credit_score, max_income_multiplier) VALUES
('HOME',      8.50,  500000, 10000000, 60, 360, 700, 60.00),
('PERSONAL',  12.00, 50000,  1500000,  12, 60,  650, 15.00),
('CAR',       9.50,  100000, 3000000,  12, 84,  675, 20.00),
('EDUCATION', 7.75,  50000,  2000000,  12, 120, 650, 25.00),
('BUSINESS',  11.50, 200000, 5000000,  12, 96,  700, 18.00);
