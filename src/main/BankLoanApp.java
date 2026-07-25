package main;

import exception.*;
import model.*;
import service.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based, menu-driven entry point for the Bank Loan Management System.
 *
 * Run with:  java -cp bin:lib/mysql-connector-j-x.x.x.jar main.BankLoanApp
 */
public class BankLoanApp {

    private static final Scanner sc = new Scanner(System.in);
    private static final AuthService authService = new AuthService();
    private static final CustomerService customerService = new CustomerService();
    private static final LoanService loanService = new LoanService();
    private static final RepaymentService repaymentService = new RepaymentService();
    private static final ReportService reportService = new ReportService();

    private static User currentUser;

    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("   BANK LOAN MANAGEMENT SYSTEM");
        System.out.println("=============================================");

        while (true) {
            if (currentUser == null) {
                loginMenu();
            } else {
                mainMenu();
            }
        }
    }

    // ------------------------------------------------------------------
    // LOGIN
    // ------------------------------------------------------------------
    private static void loginMenu() {
        System.out.println("\n1. Login");
        System.out.println("2. Exit");
        int choice = readInt("Select an option: ");
        switch (choice) {
            case 1:
                doLogin();
                break;
            case 2:
                System.out.println("Goodbye!");
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private static void doLogin() {
        String username = readString("Username: ");
        String password = readString("Password: ");
        try {
            currentUser = authService.login(username, password);
            System.out.println("Login successful. Welcome, " + currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        } catch (AuthenticationException e) {
            System.out.println("Login failed: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // MAIN MENU
    // ------------------------------------------------------------------
    private static void mainMenu() {
        System.out.println("\n===== MAIN MENU (" + currentUser.getUsername() + " | " + currentUser.getRole() + ") =====");
        System.out.println("1. Customer Management");
        System.out.println("2. Loan Application Processing");
        System.out.println("3. Repayment Tracking");
        System.out.println("4. Reports");
        if (currentUser.isAdmin()) {
            System.out.println("5. Staff Management (Admin)");
        }
        System.out.println("0. Logout");

        int choice = readInt("Select an option: ");
        switch (choice) {
            case 1: customerMenu(); break;
            case 2: loanMenu(); break;
            case 3: repaymentMenu(); break;
            case 4: reportMenu(); break;
            case 5:
                if (currentUser.isAdmin()) staffMenu();
                else System.out.println("Invalid option.");
                break;
            case 0:
                currentUser = null;
                System.out.println("Logged out.");
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    // ------------------------------------------------------------------
    // CUSTOMER MANAGEMENT
    // ------------------------------------------------------------------
    private static void customerMenu() {
        System.out.println("\n--- Customer Management ---");
        System.out.println("1. Register New Customer");
        System.out.println("2. Update Customer");
        System.out.println("3. Search Customer");
        System.out.println("4. View All Customers");
        System.out.println("5. Delete Customer");
        System.out.println("0. Back");

        int choice = readInt("Select an option: ");
        try {
            switch (choice) {
                case 1: registerCustomer(); break;
                case 2: updateCustomer(); break;
                case 3: searchCustomer(); break;
                case 4: viewAllCustomers(); break;
                case 5: deleteCustomer(); break;
                case 0: return;
                default: System.out.println("Invalid option.");
            }
        } catch (ValidationException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (RecordNotFoundException e) {
            System.out.println("Not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void registerCustomer() throws ValidationException, SQLException {
        System.out.println("\n-- Register New Customer --");
        String name = readString("Full Name: ");
        LocalDate dob = readDate("Date of Birth (yyyy-MM-dd): ");
        String gender = readString("Gender (MALE/FEMALE/OTHER): ").toUpperCase();
        String email = readString("Email: ");
        String phone = readString("Phone (10 digits): ");
        String address = readString("Address: ");
        String pan = readString("PAN Number (e.g. ABCDE1234F): ").toUpperCase();
        String aadhaar = readString("Aadhaar Number (12 digits): ");
        BigDecimal income = readBigDecimal("Monthly Income: ");
        String employmentType = readString("Employment Type (SALARIED/SELF_EMPLOYED/BUSINESS/UNEMPLOYED): ").toUpperCase();
        int creditScore = readInt("Credit Score (300-900): ");

        Customer c = new Customer(name, dob, gender, email, phone, address, pan, aadhaar, income, employmentType, creditScore);
        c.setCreatedBy(currentUser.getUserId());

        int id = customerService.registerCustomer(c);
        System.out.println("Customer registered successfully with ID: " + id);
    }

    private static void updateCustomer() throws ValidationException, RecordNotFoundException, SQLException {
        int id = readInt("Enter Customer ID to update: ");
        Customer existing = customerService.getCustomer(id);
        System.out.println("Current details: " + existing);

        System.out.println("Enter new details (press Enter to keep current value):");
        String name = readOptional("Full Name [" + existing.getFullName() + "]: ", existing.getFullName());
        String email = readOptional("Email [" + existing.getEmail() + "]: ", existing.getEmail());
        String phone = readOptional("Phone [" + existing.getPhone() + "]: ", existing.getPhone());
        String address = readOptional("Address [" + existing.getAddress() + "]: ", existing.getAddress());
        BigDecimal income = readBigDecimalOptional("Monthly Income [" + existing.getMonthlyIncome() + "]: ", existing.getMonthlyIncome());
        int creditScore = readIntOptional("Credit Score [" + existing.getCreditScore() + "]: ", existing.getCreditScore());

        existing.setFullName(name);
        existing.setEmail(email);
        existing.setPhone(phone);
        existing.setAddress(address);
        existing.setMonthlyIncome(income);
        existing.setCreditScore(creditScore);

        customerService.updateCustomer(existing);
        System.out.println("Customer updated successfully.");
    }

    private static void searchCustomer() throws SQLException {
        String keyword = readString("Enter name/phone/email/PAN to search: ");
        List<Customer> results = customerService.search(keyword);
                if (results.isEmpty()) {
            System.out.println("No customers found matching that keyword.");
        } else {
            results.forEach(System.out::println);
        }
    }

    private static void viewAllCustomers() throws SQLException {
        // Change line 199 to:
        List<Customer> customers = customerService.search("");
        if (customers.isEmpty()) {
            System.out.println("No customers registered yet.");
        } else {
            customers.forEach(System.out::println);
        }
    }

    private static void deleteCustomer() throws RecordNotFoundException, SQLException {
        int id = readInt("Enter Customer ID to delete: ");
        System.out.print("Are you sure? (Y/N): ");
        String confirm = sc.nextLine().trim();
        if ("Y".equalsIgnoreCase(confirm)) {
            customerService.deleteCustomer(id);
            System.out.println("Customer deleted successfully.");
        } else {
            System.out.println("Operation canceled.");
        }
    }

    // ------------------------------------------------------------------
    // LOAN APPLICATION PROCESSING
    // ------------------------------------------------------------------
    private static void loanMenu() {
        System.out.println("\n--- Loan Application Processing ---");
        System.out.println("1. Submit New Loan Application");
        System.out.println("2. View Applications by Status");
        System.out.println("3. View Customer's Loan Applications");
        System.out.println("4. View All Loan Applications");
        System.out.println("0. Back");

        int choice = readInt("Select an option: ");
        try {
            switch (choice) {
                case 1: submitLoanApplication(); break;
                case 2: viewApplicationsByStatus(); break;
                case 3: viewCustomerApplications(); break;
                case 4: viewAllApplications(); break;
                case 0: return;
                default: System.out.println("Invalid option.");
            }
        } catch (ValidationException e) {
            System.out.println("Validation error: " + e.getMessage());
        } catch (RecordNotFoundException e) {
            System.out.println("Not found: " + e.getMessage());
        } catch (LoanProcessingException e) {
            System.out.println("Loan processing failed: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private static void submitLoanApplication() throws ValidationException, RecordNotFoundException, SQLException, LoanProcessingException {
        System.out.println("\n-- Submit New Loan Application --");
        int customerId = readInt("Customer ID: ");
        
        System.out.println("\nAvailable Loan Categories:");
        try {
            List<LoanCategory> categories = loanService.getAllLoanCategories();
            categories.forEach(cat -> System.out.println(cat.getCategoryId() + ". " + cat.getCategoryName() + " (" + cat.getInterestRate() + "%)"));
        } catch (SQLException e) {
            System.out.println("Could not load categories: " + e.getMessage());
        }

        int categoryId = readInt("Select Category ID: ");
        BigDecimal amount = readBigDecimal("Requested Loan Amount: ");
        int tenure = readInt("Tenure (in months): ");

        // Fixed Try-Catch Handling logic wrapper mapping 
        try {
            LoanApplication app = loanService.applyForLoan(customerId, categoryId, amount, tenure, currentUser.getUserId());
            System.out.println("\nApplication processed successfully!");
            System.out.println("Application ID: " + app.getApplicationId());
            System.out.println("Status: " + app.getStatus());
            if ("APPROVED".equals(app.getStatus())) {
                System.out.println("Monthly EMI: " + app.getEmiAmount());
            } else {
                System.out.println("Reason: " + app.getRejectionReason());
            }
        } catch (LoanProcessingException e) {
            System.out.println("Application evaluation aborted: " + e.getMessage());
        }
    }

    private static void viewApplicationsByStatus() throws SQLException {
        String status = readString("Enter status (PENDING/APPROVED/REJECTED): ").toUpperCase();
        List<LoanApplication> apps = loanService.getApplicationsByStatus(status);
        if (apps.isEmpty()) {
            System.out.println("No applications found with status: " + status);
        } else {
            apps.forEach(System.out::println);
        }
    }

    private static void viewCustomerApplications() throws SQLException {
        int customerId = readInt("Enter Customer ID: ");
        List<LoanApplication> apps = loanService.getApplicationsForCustomer(customerId);
        if (apps.isEmpty()) {
            System.out.println("No applications found for customer ID: " + customerId);
        } else {
            apps.forEach(System.out::println);
        }
    }

    private static void viewAllApplications() throws SQLException {
        List<LoanApplication> apps = loanService.getAllApplications();
        if (apps.isEmpty()) {
            System.out.println("No applications found.");
        } else {
            apps.forEach(System.out::println);
        }
    }

    // ------------------------------------------------------------------
    // REPAYMENT TRACKING
    // ------------------------------------------------------------------
    private static void repaymentMenu() {
        System.out.println("\n--- Repayment Tracking ---");
        System.out.println("0. Back");
        readInt("Select an option: ");
    }

    // ------------------------------------------------------------------
    // REPORTS & STAFF MANAGEMENT PLACEHOLDERS
    // ------------------------------------------------------------------
    private static void reportMenu() {
        System.out.println("\n--- Reports ---");
        System.out.println("0. Back");
        readInt("Select an option: ");
    }

    private static void staffMenu() {
        System.out.println("\n--- Staff Management (Admin) ---");
        System.out.println("0. Back");
        readInt("Select an option: ");
    }

    // ------------------------------------------------------------------
    // INPUT UTILITIES
    // ------------------------------------------------------------------
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = sc.nextInt();
                sc.nextLine(); // clear buffer
                return val;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer.");
                sc.nextLine();
            }
        }
    }

    private static int readIntOptional(String prompt, int defaultVal) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return defaultVal;
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Keeping old value.");
            return defaultVal;
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private static String readOptional(String prompt, String defaultVal) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        return input.isEmpty() ? defaultVal : input;
    }

    private static BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal val = sc.nextBigDecimal();
                sc.nextLine();
                return val;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
                sc.nextLine();
            }
        }
    }

    private static BigDecimal readBigDecimalOptional(String prompt, BigDecimal defaultVal) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        if (input.isEmpty()) return defaultVal;
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid decimal format. Keeping old value.");
            return defaultVal;
        }
    }

    private static LocalDate readDate(String prompt) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            try {
                return LocalDate.parse(input, formatter);
            } catch (Exception e) {
                System.out.println("Invalid date format. Use yyyy-MM-dd.");
            }
        }
    }
}
