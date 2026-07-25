package util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Handles EMI (Equated Monthly Installment) and total-interest calculations
 * using the standard reducing-balance formula:
 *
 *      EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 *
 * where:
 *      P = principal loan amount
 *      r = monthly interest rate (annual rate / 12 / 100)
 *      n = tenure in months
 */
public class EMICalculator {

    private EMICalculator() {}

    public static BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualInterestRate, int tenureMonths) {
        if (principal == null || annualInterestRate == null || tenureMonths <= 0) {
            throw new IllegalArgumentException("Invalid EMI calculation parameters");
        }

        MathContext mc = new MathContext(10);
        BigDecimal monthlyRate = annualInterestRate
                .divide(BigDecimal.valueOf(12), mc)
                .divide(BigDecimal.valueOf(100), mc);

        // Handle 0% interest edge case (straight division)
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(tenureMonths), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, mc);

        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRPowN, mc);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotalPayment(BigDecimal emi, int tenureMonths) {
        return emi.multiply(BigDecimal.valueOf(tenureMonths)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTotalInterest(BigDecimal principal, BigDecimal emi, int tenureMonths) {
        return calculateTotalPayment(emi, tenureMonths).subtract(principal).setScale(2, RoundingMode.HALF_UP);
    }
}
