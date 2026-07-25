package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a loan application submitted by a customer for a specific loan category.
 */
public class LoanApplication {
    private int applicationId;
    private int customerId;
    private int categoryId;
    private BigDecimal requestedAmount;
    private int tenureMonths;
    private BigDecimal interestRate;   // snapshot of rate at time of application
    private BigDecimal emiAmount;
    private String status;             // PENDING, APPROVED, REJECTED, CLOSED
    private String rejectionReason;
    private LocalDateTime appliedOn;
    private Integer processedBy;
    private LocalDateTime processedOn;

    // Extra display fields (populated via joins, not persisted directly)
    private String customerName;
    private String categoryName;

    public LoanApplication() {}

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public BigDecimal getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(BigDecimal requestedAmount) { this.requestedAmount = requestedAmount; }

    public int getTenureMonths() { return tenureMonths; }
    public void setTenureMonths(int tenureMonths) { this.tenureMonths = tenureMonths; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getEmiAmount() { return emiAmount; }
    public void setEmiAmount(BigDecimal emiAmount) { this.emiAmount = emiAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getAppliedOn() { return appliedOn; }
    public void setAppliedOn(LocalDateTime appliedOn) { this.appliedOn = appliedOn; }

    public Integer getProcessedBy() { return processedBy; }
    public void setProcessedBy(Integer processedBy) { this.processedBy = processedBy; }

    public LocalDateTime getProcessedOn() { return processedOn; }
    public void setProcessedOn(LocalDateTime processedOn) { this.processedOn = processedOn; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    @Override
    public String toString() {
        return String.format("App#%d | Customer: %s | Loan: %s | Amount: %s | Tenure: %d mo | EMI: %s | Status: %s",
                applicationId, customerName, categoryName, requestedAmount, tenureMonths, emiAmount, status);
    }
}
