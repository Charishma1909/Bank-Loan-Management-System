package dao;

import model.Customer;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the `customers` table.
 */
public class CustomerDAO {

    public int insertCustomer(Customer c) throws SQLException {
        String sql = "INSERT INTO customers (full_name, dob, gender, email, phone, address, pan_number, " +
                "aadhaar_number, monthly_income, employment_type, credit_score, created_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getFullName());
            ps.setDate(2, Date.valueOf(c.getDob()));
            ps.setString(3, c.getGender());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getPhone());
            ps.setString(6, c.getAddress());
            ps.setString(7, c.getPanNumber());
            ps.setString(8, c.getAadhaarNumber());
            ps.setBigDecimal(9, c.getMonthlyIncome());
            ps.setString(10, c.getEmploymentType());
            ps.setInt(11, c.getCreditScore());
            if (c.getCreatedBy() != null) {
                ps.setInt(12, c.getCreatedBy());
            } else {
                ps.setNull(12, Types.INTEGER);
            }
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean updateCustomer(Customer c) throws SQLException {
        String sql = "UPDATE customers SET full_name=?, dob=?, gender=?, email=?, phone=?, address=?, " +
                "pan_number=?, aadhaar_number=?, monthly_income=?, employment_type=?, credit_score=? " +
                "WHERE customer_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getFullName());
            ps.setDate(2, Date.valueOf(c.getDob()));
            ps.setString(3, c.getGender());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getPhone());
            ps.setString(6, c.getAddress());
            ps.setString(7, c.getPanNumber());
            ps.setString(8, c.getAadhaarNumber());
            ps.setBigDecimal(9, c.getMonthlyIncome());
            ps.setString(10, c.getEmploymentType());
            ps.setInt(11, c.getCreditScore());
            ps.setInt(12, c.getCustomerId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteCustomer(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Customer> searchByNameOrPhoneOrEmail(String keyword) throws SQLException {
        List<Customer> results = new ArrayList<>();
        String sql = "SELECT * FROM customers WHERE full_name LIKE ? OR phone LIKE ? OR email LIKE ? " +
                "OR pan_number LIKE ? ORDER BY customer_id";
        String like = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapRow(rs));
            }
        }
        return results;
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> results = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY customer_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) results.add(mapRow(rs));
        }
        return results;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setCustomerId(rs.getInt("customer_id"));
        c.setFullName(rs.getString("full_name"));
        c.setDob(rs.getDate("dob").toLocalDate());
        c.setGender(rs.getString("gender"));
        c.setEmail(rs.getString("email"));
        c.setPhone(rs.getString("phone"));
        c.setAddress(rs.getString("address"));
        c.setPanNumber(rs.getString("pan_number"));
        c.setAadhaarNumber(rs.getString("aadhaar_number"));
        c.setMonthlyIncome(rs.getBigDecimal("monthly_income"));
        c.setEmploymentType(rs.getString("employment_type"));
        c.setCreditScore(rs.getInt("credit_score"));
        int createdBy = rs.getInt("created_by");
        c.setCreatedBy(rs.wasNull() ? null : createdBy);
        return c;
    }
}
