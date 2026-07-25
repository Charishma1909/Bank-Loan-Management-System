package dao;

import model.LoanApplication;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the `loan_applications` table.
 */
public class LoanApplicationDAO {

    private static final String SELECT_JOIN =
            "SELECT la.*, c.full_name AS customer_name, lc.category_name AS category_name " +
            "FROM loan_applications la " +
            "JOIN customers c ON la.customer_id = c.customer_id " +
            "JOIN loan_categories lc ON la.category_id = lc.category_id ";

    public int insertApplication(LoanApplication app) throws SQLException {
        String sql = "INSERT INTO loan_applications (customer_id, category_id, requested_amount, tenure_months, " +
                "interest_rate, emi_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, app.getCustomerId());
            ps.setInt(2, app.getCategoryId());
            ps.setBigDecimal(3, app.getRequestedAmount());
            ps.setInt(4, app.getTenureMonths());
            ps.setBigDecimal(5, app.getInterestRate());
            ps.setBigDecimal(6, app.getEmiAmount());
            ps.setString(7, app.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateStatus(int applicationId, String status, String rejectionReason, int processedBy) throws SQLException {
        String sql = "UPDATE loan_applications SET status=?, rejection_reason=?, processed_by=?, processed_on=NOW() " +
                "WHERE application_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, rejectionReason);
            ps.setInt(3, processedBy);
            ps.setInt(4, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    public LoanApplication findById(int applicationId) throws SQLException {
        String sql = SELECT_JOIN + " WHERE la.application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<LoanApplication> findByCustomer(int customerId) throws SQLException {
        List<LoanApplication> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE la.customer_id = ? ORDER BY la.application_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<LoanApplication> findByStatus(String status) throws SQLException {
        List<LoanApplication> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE la.status = ? ORDER BY la.application_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<LoanApplication> findAll() throws SQLException {
        List<LoanApplication> list = new ArrayList<>();
        String sql = SELECT_JOIN + " ORDER BY la.application_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    private LoanApplication mapRow(ResultSet rs) throws SQLException {
        LoanApplication a = new LoanApplication();
        a.setApplicationId(rs.getInt("application_id"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setCategoryId(rs.getInt("category_id"));
        a.setRequestedAmount(rs.getBigDecimal("requested_amount"));
        a.setTenureMonths(rs.getInt("tenure_months"));
        a.setInterestRate(rs.getBigDecimal("interest_rate"));
        a.setEmiAmount(rs.getBigDecimal("emi_amount"));
        a.setStatus(rs.getString("status"));
        a.setRejectionReason(rs.getString("rejection_reason"));
        Timestamp appliedOn = rs.getTimestamp("applied_on");
        if (appliedOn != null) a.setAppliedOn(appliedOn.toLocalDateTime());
        int processedBy = rs.getInt("processed_by");
        a.setProcessedBy(rs.wasNull() ? null : processedBy);
        Timestamp processedOn = rs.getTimestamp("processed_on");
        if (processedOn != null) a.setProcessedOn(processedOn.toLocalDateTime());
        a.setCustomerName(rs.getString("customer_name"));
        a.setCategoryName(rs.getString("category_name"));
        return a;
    }
}
