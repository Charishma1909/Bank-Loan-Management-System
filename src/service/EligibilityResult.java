package service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the outcome of an automated eligibility check:
 * whether the applicant is eligible, and if not, the specific reasons why.
 */
public class EligibilityResult {

    private boolean eligible = true;
    private final List<String> reasons = new ArrayList<>();
    private BigDecimal maxEligibleAmount;
    private BigDecimal computedEMI;

    public void addFailureReason(String reason) {
        this.eligible = false;
        this.reasons.add(reason);
    }

    public boolean isEligible() {
        return eligible;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public String getReasonsAsString() {
        return String.join("; ", reasons);
    }

    public BigDecimal getMaxEligibleAmount() {
        return maxEligibleAmount;
    }

    public void setMaxEligibleAmount(BigDecimal maxEligibleAmount) {
        this.maxEligibleAmount = maxEligibleAmount;
    }

    public BigDecimal getComputedEMI() {
        return computedEMI;
    }

    public void setComputedEMI(BigDecimal computedEMI) {
        this.computedEMI = computedEMI;
    }
}
