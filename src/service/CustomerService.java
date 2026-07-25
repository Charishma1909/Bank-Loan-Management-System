package service;

import dao.CustomerDAO;
import exception.RecordNotFoundException;
import exception.ValidationException;
import model.Customer;
import util.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Business logic for customer registration, profile updates, and search.
 */
public class CustomerService {

    private final CustomerDAO customerDAO = new CustomerDAO();

    public int registerCustomer(Customer c) throws ValidationException, SQLException {
        validate(c);
        return customerDAO.insertCustomer(c);
    }

    public void updateCustomer(Customer c) throws ValidationException, RecordNotFoundException, SQLException {
        validate(c);
        Customer existing = customerDAO.findById(c.getCustomerId());
        if (existing == null) {
            throw new RecordNotFoundException("Customer with ID " + c.getCustomerId() + " does not exist.");
        }
        customerDAO.updateCustomer(c);
    }

    public void deleteCustomer(int customerId) throws RecordNotFoundException, SQLException {
        Customer existing = customerDAO.findById(customerId);
        if (existing == null) {
            throw new RecordNotFoundException("Customer with ID " + customerId + " does not exist.");
        }
        customerDAO.deleteCustomer(customerId);
    }

    public Customer getCustomer(int customerId) throws RecordNotFoundException, SQLException {
        Customer c = customerDAO.findById(customerId);
        if (c == null) {
            throw new RecordNotFoundException("Customer with ID " + customerId + " does not exist.");
        }
        return c;
    }

    public List<Customer> search(String keyword) throws SQLException {
        return customerDAO.searchByNameOrPhoneOrEmail(keyword);
    }

    public List<Customer> getAll() throws SQLException {
        return customerDAO.findAll();
    }

    private void validate(Customer c) throws ValidationException {
        if (!ValidationUtil.isNotEmpty(c.getFullName())) {
            throw new ValidationException("Full name cannot be empty.");
        }
        if (c.getDob() == null || !ValidationUtil.isAdult(c.getDob())) {
            throw new ValidationException("Customer must be at least 18 years old.");
        }
        if (!ValidationUtil.isNotEmpty(c.getGender())) {
            throw new ValidationException("Gender must be specified.");
        }
        if (!ValidationUtil.isValidEmail(c.getEmail())) {
            throw new ValidationException("Invalid email format.");
        }
        if (!ValidationUtil.isValidPhone(c.getPhone())) {
            throw new ValidationException("Invalid phone number. Must be a 10-digit number starting with 6-9.");
        }
        if (!ValidationUtil.isValidPAN(c.getPanNumber())) {
            throw new ValidationException("Invalid PAN number format (expected e.g. ABCDE1234F).");
        }
        if (!ValidationUtil.isValidAadhaar(c.getAadhaarNumber())) {
            throw new ValidationException("Invalid Aadhaar number. Must be exactly 12 digits.");
        }
        if (c.getMonthlyIncome() == null || c.getMonthlyIncome().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Monthly income must be greater than zero.");
        }
        if (!ValidationUtil.isNotEmpty(c.getEmploymentType())) {
            throw new ValidationException("Employment type must be specified.");
        }
        if (!ValidationUtil.isInRange(c.getCreditScore(), 300, 900)) {
            throw new ValidationException("Credit score must be between 300 and 900.");
        }
    }
}
