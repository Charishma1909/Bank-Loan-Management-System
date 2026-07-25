package dao;

import model.Repayment;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the `repayments` table (EMI schedule + payment tracking).
 */
public class RepaymentDAO {

    public void insertSchedule(List<Repayment> schedule) throws SQLException {
        String sql = "INSERT INTO repayments (application_id, installment_no, due_amount, paid_amount, due_date, status) " +
                "VALUES (?, ?, ?, 0, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Repayment r : schedule) {
                ps.setInt(1, r.getApplicationId());
                ps.setInt(2, r.getInstallmentNo());
                ps.setBigDecimal(3, r.getDueAmount());
                ps.setDate(4, Date.valueOf(r.getDueDate()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public List<Repayment> findByApplication(int applicationId) throws SQLException {
        List<Repayment> list = new ArrayList<>();
        String sql = "SELECT * FROM repayments WHERE application_id = ? ORDER BY installment_no";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Repayment findNextPending(int applicationId) throws SQLException {
        String sql = "SELECT * FROM repayments WHERE application_id = ? AND status <> 'PAID' " +
                "ORDER BY installment_no LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean markPaid(int repaymentId, java.math.BigDecimal paidAmount) throws SQLException {
        String sql = "UPDATE repayments SET paid_amount = ?, paid_date = ?, status = 'PAID' WHERE repayment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, paidAmount);
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            ps.setInt(3, repaymentId);
            return ps.executeUpdate() > 0;
        }
    }

    public void refreshOverdueStatus(int applicationId) throws SQLException {
        String sql = "UPDATE repayments SET status = 'OVERDUE' " +
                "WHERE application_id = ? AND status = 'PENDING' AND due_date < CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.executeUpdate();
        }
    }

    public int countPaidInstallments(int applicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM repayments WHERE application_id = ? AND status = 'PAID'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countTotalInstallments(int applicationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM repayments WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private Repayment mapRow(ResultSet rs) throws SQLException {
        Repayment r = new Repayment();
        r.setRepaymentId(rs.getInt("repayment_id"));
        r.setApplicationId(rs.getInt("application_id"));
        r.setInstallmentNo(rs.getInt("installment_no"));
        r.setDueAmount(rs.getBigDecimal("due_amount"));
        r.setPaidAmount(rs.getBigDecimal("paid_amount"));
        r.setDueDate(rs.getDate("due_date").toLocalDate());
        Date paidDate = rs.getDate("paid_date");
        if (paidDate != null) r.setPaidDate(paidDate.toLocalDate());
        r.setStatus(rs.getString("status"));
        return r;
    }
}
