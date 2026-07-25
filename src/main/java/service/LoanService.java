package service;

import dao.LoanApplicationDAO;
import dao.LoanCategoryDAO;
import dao.CustomerDAO;
import dao.RepaymentDAO;
import exception.LoanProcessingException;
import exception.RecordNotFoundException;
import exception.ValidationException;
import model.Customer;
import model.LoanApplication;
import model.LoanCategory;
import model.Repayment;
import util.EMICalculator;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full loan application lifecycle:
 * submission -> automated eligibility check -> approval/rejection -> repayment schedule generation.
 */
public class LoanService {

    private final LoanApplicationDAO applicationDAO = new LoanApplicationDAO();
    private final LoanCategoryDAO categoryDAO = new LoanCategoryDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final RepaymentDAO repaymentDAO = new RepaymentDAO();
    private final EligibilityEngine eligibilityEngine = new EligibilityEngine();

    /**
     * Submits a new loan application, runs the automated eligibility engine, and
     * immediately marks the application APPROVED or REJECTED accordingly.
     *
     * @return the persisted LoanApplication with final status set
     */
    public LoanApplication applyForLoan(int customerId, int categoryId, BigDecimal requestedAmount,
                                         int tenureMonths, int processedByUserId)
            throws ValidationException, RecordNotFoundException, SQLException, LoanProcessingException {

        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Requested loan amount must be greater than zero.");
        }
        if (tenureMonths <= 0) {
            throw new ValidationException("Tenure must be a positive number of months.");
        }

        Customer customer = customerDAO.findById(customerId);
        if (customer == null) {
            throw new RecordNotFoundException("Customer with ID " + customerId + " not found.");
        }

        LoanCategory category = categoryDAO.findById(categoryId);
        if (category == null) {
            throw new RecordNotFoundException("Loan category with ID " + categoryId + " not found.");
        }

        EligibilityResult eligibility = eligibilityEngine.evaluate(customer, category, requestedAmount, tenureMonths);

        LoanApplication app = new LoanApplication();
        app.setCustomerId(customerId);
        app.setCategoryId(categoryId);
        app.setRequestedAmount(requestedAmount);
        app.setTenureMonths(tenureMonths);
        app.setInterestRate(category.getInterestRate());

        if (eligibility.isEligible()) {
            app.setEmiAmount(eligibility.getComputedEMI());
            app.setStatus("PENDING"); // Saved as pending first, then auto-approved below
        } else {
            app.setEmiAmount(eligibility.getComputedEMI()); // may be null if amount/tenure invalid
            app.setStatus("PENDING");
        }

        int appId = applicationDAO.insertApplication(app);
        app.setApplicationId(appId);

        if (eligibility.isEligible()) {
            approveLoan(appId, processedByUserId);
            app.setStatus("APPROVED");
        } else {
            rejectLoan(appId, eligibility.getReasonsAsString(), processedByUserId);
            app.setStatus("REJECTED");
            app.setRejectionReason(eligibility.getReasonsAsString());
        }

        app.setCustomerName(customer.getFullName());
        app.setCategoryName(category.getCategoryName());
        return app;
    }

    /**
     * Approves a loan and generates its monthly repayment schedule.
     */
    public void approveLoan(int applicationId, int processedByUserId) throws SQLException, RecordNotFoundException, LoanProcessingException {
        LoanApplication app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new RecordNotFoundException("Loan application #" + applicationId + " not found.");
        }
        if ("APPROVED".equals(app.getStatus())) {
            throw new LoanProcessingException("Loan application #" + applicationId + " is already approved.");
        }
        applicationDAO.updateStatus(applicationId, "APPROVED", null, processedByUserId);
        generateRepaymentSchedule(app);
    }

    public void rejectLoan(int applicationId, String reason, int processedByUserId) throws SQLException, RecordNotFoundException {
        LoanApplication app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new RecordNotFoundException("Loan application #" + applicationId + " not found.");
        }
        applicationDAO.updateStatus(applicationId, "REJECTED", reason, processedByUserId);
    }

    private void generateRepaymentSchedule(LoanApplication app) throws SQLException {
        BigDecimal emi = app.getEmiAmount() != null ? app.getEmiAmount() :
                EMICalculator.calculateEMI(app.getRequestedAmount(), app.getInterestRate(), app.getTenureMonths());

        List<Repayment> schedule = new ArrayList<>();
        LocalDate firstDueDate = LocalDate.now().plusMonths(1);
        for (int i = 1; i <= app.getTenureMonths(); i++) {
            Repayment r = new Repayment();
            r.setApplicationId(app.getApplicationId());
            r.setInstallmentNo(i);
            r.setDueAmount(emi);
            r.setDueDate(firstDueDate.plusMonths(i - 1));
            schedule.add(r);
        }
        repaymentDAO.insertSchedule(schedule);
    }

    public LoanApplication getApplication(int applicationId) throws SQLException, RecordNotFoundException {
        LoanApplication app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new RecordNotFoundException("Loan application #" + applicationId + " not found.");
        }
        return app;
    }

    public List<LoanApplication> getApplicationsForCustomer(int customerId) throws SQLException {
        return applicationDAO.findByCustomer(customerId);
    }

    public List<LoanApplication> getApplicationsByStatus(String status) throws SQLException {
        return applicationDAO.findByStatus(status);
    }

    public List<LoanApplication> getAllApplications() throws SQLException {
        return applicationDAO.findAll();
    }

    public List<LoanCategory> getAllLoanCategories() throws SQLException {
        return categoryDAO.findAllActive();
    }
}
