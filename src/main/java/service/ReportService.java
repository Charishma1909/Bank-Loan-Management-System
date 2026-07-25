package service;

import dao.LoanApplicationDAO;
import model.LoanApplication;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Generates summary reports for bank management:
 * loan portfolio overview, category-wise disbursement, approval/rejection stats.
 */
public class ReportService {

    private final LoanApplicationDAO applicationDAO = new LoanApplicationDAO();

    public String generateSummaryReport() throws SQLException {
        List<LoanApplication> all = applicationDAO.findAll();

        long pending = all.stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        long approved = all.stream().filter(a -> "APPROVED".equals(a.getStatus())).count();
        long rejected = all.stream().filter(a -> "REJECTED".equals(a.getStatus())).count();
        long closed = all.stream().filter(a -> "CLOSED".equals(a.getStatus())).count();

        BigDecimal totalDisbursed = all.stream()
                .filter(a -> "APPROVED".equals(a.getStatus()) || "CLOSED".equals(a.getStatus()))
                .map(LoanApplication::getRequestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder sb = new StringBuilder();
        sb.append("==================== LOAN PORTFOLIO SUMMARY ====================\n");
        sb.append(String.format("Total Applications : %d%n", all.size()));
        sb.append(String.format("  - Pending         : %d%n", pending));
        sb.append(String.format("  - Approved        : %d%n", approved));
        sb.append(String.format("  - Rejected        : %d%n", rejected));
        sb.append(String.format("  - Closed          : %d%n", closed));
        sb.append(String.format("Total Amount Disbursed (Approved+Closed): %s%n", totalDisbursed));
        sb.append("==================================================================\n");
        return sb.toString();
    }

    public String generateCategoryWiseReport() throws SQLException {
        List<LoanApplication> all = applicationDAO.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("================= CATEGORY-WISE LOAN REPORT =====================\n");

        all.stream()
            .map(LoanApplication::getCategoryName)
            .distinct()
            .sorted()
            .forEach(category -> {
                long count = all.stream().filter(a -> category.equals(a.getCategoryName())).count();
                BigDecimal total = all.stream()
                        .filter(a -> category.equals(a.getCategoryName()))
                        .map(LoanApplication::getRequestedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                sb.append(String.format("%-12s | Applications: %-5d | Total Requested: %s%n", category, count, total));
            });

        sb.append("==================================================================\n");
        return sb.toString();
    }

    public List<LoanApplication> getRejectedApplicationsReport() throws SQLException {
        return applicationDAO.findByStatus("REJECTED");
    }

    public List<LoanApplication> getApprovedApplicationsReport() throws SQLException {
        return applicationDAO.findByStatus("APPROVED");
    }
}
