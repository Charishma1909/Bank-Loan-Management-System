package service;

import model.Customer;
import model.LoanCategory;
import util.EMICalculator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core automated eligibility engine.
 *
 * Applies configurable bank-policy rules (drawn from LoanCategory) against
 * a customer's profile and the requested loan terms to decide whether the
 * loan should be auto-approved, and computes the EMI if it can proceed.
 *
 * Rules applied (in order):
 *  1. Category must be active.
 *  2. Requested amount must fall within [minAmount, maxAmount] for the category.
 *  3. Tenure must fall within [minTenureMonths, maxTenureMonths] for the category.
 *  4. Customer credit score must meet the category's minimum credit score.
 *  5. Customer must not be UNEMPLOYED.
 *  6. Requested amount must not exceed (monthlyIncome * 12 * maxIncomeMultiplier).
 *  7. EMI must not exceed 50% of the customer's monthly income (debt-to-income check).
 */
public class EligibilityEngine {

    private static final BigDecimal MAX_EMI_TO_INCOME_RATIO = new BigDecimal("0.50");

    public EligibilityResult evaluate(Customer customer, LoanCategory category,
                                       BigDecimal requestedAmount, int tenureMonths) {
        EligibilityResult result = new EligibilityResult();

        if (!category.isActive()) {
            result.addFailureReason("Loan category '" + category.getCategoryName() + "' is currently not offered.");
            return result;
        }

        if (requestedAmount.compareTo(category.getMinAmount()) < 0 ||
                requestedAmount.compareTo(category.getMaxAmount()) > 0) {
            result.addFailureReason(String.format(
                    "Requested amount must be between %s and %s for %s loans.",
                    category.getMinAmount(), category.getMaxAmount(), category.getCategoryName()));
        }

        if (tenureMonths < category.getMinTenureMonths() || tenureMonths > category.getMaxTenureMonths()) {
            result.addFailureReason(String.format(
                    "Tenure must be between %d and %d months for %s loans.",
                    category.getMinTenureMonths(), category.getMaxTenureMonths(), category.getCategoryName()));
        }

        if (customer.getCreditScore() < category.getMinCreditScore()) {
            result.addFailureReason(String.format(
                    "Credit score %d is below the required minimum of %d.",
                    customer.getCreditScore(), category.getMinCreditScore()));
        }

        if ("UNEMPLOYED".equalsIgnoreCase(customer.getEmploymentType())) {
            result.addFailureReason("Applicant has no declared source of income (employment type: UNEMPLOYED).");
        }

        BigDecimal annualIncome = customer.getMonthlyIncome().multiply(BigDecimal.valueOf(12));
        BigDecimal maxEligibleAmount = annualIncome.multiply(category.getMaxIncomeMultiplier())
                .setScale(2, RoundingMode.HALF_UP);
        result.setMaxEligibleAmount(maxEligibleAmount);

        if (requestedAmount.compareTo(maxEligibleAmount) > 0) {
            result.addFailureReason(String.format(
                    "Requested amount %s exceeds the maximum eligible amount of %s based on income.",
                    requestedAmount, maxEligibleAmount));
        }

        // Only compute EMI / debt-to-income check if basic parameters are sane enough to calculate
        if (tenureMonths > 0) {
            BigDecimal emi = EMICalculator.calculateEMI(requestedAmount, category.getInterestRate(), tenureMonths);
            result.setComputedEMI(emi);

            BigDecimal maxAllowedEMI = customer.getMonthlyIncome().multiply(MAX_EMI_TO_INCOME_RATIO)
                    .setScale(2, RoundingMode.HALF_UP);
            if (emi.compareTo(maxAllowedEMI) > 0) {
                result.addFailureReason(String.format(
                        "Calculated EMI %s exceeds 50%% of monthly income (max allowed EMI: %s).",
                        emi, maxAllowedEMI));
            }
        }

        return result;
    }
}
