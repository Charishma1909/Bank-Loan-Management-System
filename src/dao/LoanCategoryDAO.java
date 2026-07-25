package dao;

import model.LoanCategory;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data-access layer for the `loan_categories` table (bank policy configuration).
 */
public class LoanCategoryDAO {

    public List<LoanCategory> findAllActive() throws SQLException {
        List<LoanCategory> list = new ArrayList<>();
        String sql = "SELECT * FROM loan_categories WHERE is_active = TRUE ORDER BY category_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public LoanCategory findById(int categoryId) throws SQLException {
        String sql = "SELECT * FROM loan_categories WHERE category_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public boolean updatePolicy(LoanCategory lc) throws SQLException {
        String sql = "UPDATE loan_categories SET interest_rate=?, min_amount=?, max_amount=?, " +
                "min_tenure_months=?, max_tenure_months=?, min_credit_score=?, max_income_multiplier=? " +
                "WHERE category_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, lc.getInterestRate());
            ps.setBigDecimal(2, lc.getMinAmount());
            ps.setBigDecimal(3, lc.getMaxAmount());
            ps.setInt(4, lc.getMinTenureMonths());
            ps.setInt(5, lc.getMaxTenureMonths());
            ps.setInt(6, lc.getMinCreditScore());
            ps.setBigDecimal(7, lc.getMaxIncomeMultiplier());
            ps.setInt(8, lc.getCategoryId());
            return ps.executeUpdate() > 0;
        }
    }

    private LoanCategory mapRow(ResultSet rs) throws SQLException {
        LoanCategory lc = new LoanCategory();
        lc.setCategoryId(rs.getInt("category_id"));
        lc.setCategoryName(rs.getString("category_name"));
        lc.setInterestRate(rs.getBigDecimal("interest_rate"));
        lc.setMinAmount(rs.getBigDecimal("min_amount"));
        lc.setMaxAmount(rs.getBigDecimal("max_amount"));
        lc.setMinTenureMonths(rs.getInt("min_tenure_months"));
        lc.setMaxTenureMonths(rs.getInt("max_tenure_months"));
        lc.setMinCreditScore(rs.getInt("min_credit_score"));
        lc.setMaxIncomeMultiplier(rs.getBigDecimal("max_income_multiplier"));
        lc.setActive(rs.getBoolean("is_active"));
        return lc;
    }
}
