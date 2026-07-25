package model;

import java.math.BigDecimal;

/**
 * Represents a configurable loan product / policy (e.g. HOME, PERSONAL, CAR).
 * All eligibility thresholds live here, so bank policy can change without
 * touching business logic code.
 */
public class LoanCategory {
    private int categoryId;
    private String categoryName;
    private BigDecimal interestRate;      // annual %
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private int minTenureMonths;
    private int maxTenureMonths;
    private int minCreditScore;
    private BigDecimal maxIncomeMultiplier; // max loan = monthlyIncome * 12 * multiplier
    private boolean active;

    public LoanCategory() {}

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }

    public int getMinTenureMonths() { return minTenureMonths; }
    public void setMinTenureMonths(int minTenureMonths) { this.minTenureMonths = minTenureMonths; }

    public int getMaxTenureMonths() { return maxTenureMonths; }
    public void setMaxTenureMonths(int maxTenureMonths) { this.maxTenureMonths = maxTenureMonths; }

    public int getMinCreditScore() { return minCreditScore; }
    public void setMinCreditScore(int minCreditScore) { this.minCreditScore = minCreditScore; }

    public BigDecimal getMaxIncomeMultiplier() { return maxIncomeMultiplier; }
    public void setMaxIncomeMultiplier(BigDecimal maxIncomeMultiplier) { this.maxIncomeMultiplier = maxIncomeMultiplier; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return String.format("[%d] %-10s | Rate: %5s%% | Amount: %s - %s | Tenure: %d-%d months | Min Credit: %d",
                categoryId, categoryName, interestRate, minAmount, maxAmount,
                minTenureMonths, maxTenureMonths, minCreditScore);
    }
}
