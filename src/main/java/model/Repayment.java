package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a single EMI installment for a loan application.
 */
public class Repayment {
    private int repaymentId;
    private int applicationId;
    private int installmentNo;
    private BigDecimal dueAmount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String status; // PENDING, PAID, OVERDUE

    public Repayment() {}

    public int getRepaymentId() { return repaymentId; }
    public void setRepaymentId(int repaymentId) { this.repaymentId = repaymentId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getInstallmentNo() { return installmentNo; }
    public void setInstallmentNo(int installmentNo) { this.installmentNo = installmentNo; }

    public BigDecimal getDueAmount() { return dueAmount; }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount = dueAmount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getPaidDate() { return paidDate; }
    public void setPaidDate(LocalDate paidDate) { this.paidDate = paidDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("Installment #%-3d | Due: %-10s | Amount: %-10s | Paid: %-10s | Status: %s",
                installmentNo, dueDate, dueAmount, paidAmount, status);
    }
}
