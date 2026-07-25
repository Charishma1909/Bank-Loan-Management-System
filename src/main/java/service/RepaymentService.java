package service;

import dao.LoanApplicationDAO;
import dao.RepaymentDAO;
import exception.LoanProcessingException;
import exception.RecordNotFoundException;
import model.LoanApplication;
import model.Repayment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Handles EMI repayment tracking: viewing schedules and recording payments.
 */
public class RepaymentService {

    private final RepaymentDAO repaymentDAO = new RepaymentDAO();
    private final LoanApplicationDAO applicationDAO = new LoanApplicationDAO();

    public List<Repayment> getSchedule(int applicationId) throws SQLException {
        repaymentDAO.refreshOverdueStatus(applicationId);
        return repaymentDAO.findByApplication(applicationId);
    }

    /**
     * Pays the next pending/overdue installment in full.
     */
    public Repayment payNextInstallment(int applicationId) throws SQLException, RecordNotFoundException, LoanProcessingException {
        LoanApplication app = applicationDAO.findById(applicationId);
        if (app == null) {
            throw new RecordNotFoundException("Loan application #" + applicationId + " not found.");
        }
        if (!"APPROVED".equals(app.getStatus()) && !"CLOSED".equals(app.getStatus())) {
            throw new LoanProcessingException("Cannot record repayment for a loan that is not approved.");
        }

        repaymentDAO.refreshOverdueStatus(applicationId);
        Repayment next = repaymentDAO.findNextPending(applicationId);
        if (next == null) {
            throw new LoanProcessingException("All installments for loan #" + applicationId + " are already paid.");
        }

        repaymentDAO.markPaid(next.getRepaymentId(), next.getDueAmount());
        next.setPaidAmount(next.getDueAmount());
        next.setStatus("PAID");

        // If this was the last installment, close the loan
        int paid = repaymentDAO.countPaidInstallments(applicationId);
        int total = repaymentDAO.countTotalInstallments(applicationId);
        if (paid == total) {
            applicationDAO.updateStatus(applicationId, "CLOSED", null, app.getProcessedBy() != null ? app.getProcessedBy() : 0);
        }

        return next;
    }

    public BigDecimal getOutstandingBalance(int applicationId) throws SQLException {
        List<Repayment> schedule = repaymentDAO.findByApplication(applicationId);
        BigDecimal outstanding = BigDecimal.ZERO;
        for (Repayment r : schedule) {
            if (!"PAID".equals(r.getStatus())) {
                outstanding = outstanding.add(r.getDueAmount());
            }
        }
        return outstanding;
    }
}
