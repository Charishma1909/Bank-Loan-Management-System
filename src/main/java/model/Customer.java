package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a bank customer eligible to apply for loans.
 */
public class Customer {
    private int customerId;
    private String fullName;
    private LocalDate dob;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private String panNumber;
    private String aadhaarNumber;
    private BigDecimal monthlyIncome;
    private String employmentType;
    private int creditScore;
    private Integer createdBy;

    public Customer() {}

    public Customer(String fullName, LocalDate dob, String gender, String email, String phone,
                     String address, String panNumber, String aadhaarNumber,
                     BigDecimal monthlyIncome, String employmentType, int creditScore) {
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.panNumber = panNumber;
        this.aadhaarNumber = aadhaarNumber;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
        this.creditScore = creditScore;
    }

    public int getAge() {
        return LocalDate.now().getYear() - dob.getYear() -
                (LocalDate.now().getDayOfYear() < dob.getDayOfYear() ? 1 : 0);
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPanNumber() { return panNumber; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }

    public String getAadhaarNumber() { return aadhaarNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }

    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }

    public Integer getCreatedBy() { return createdBy; }
    public void setCreatedBy(Integer createdBy) { this.createdBy = createdBy; }

    @Override
    public String toString() {
        return String.format("[%d] %-20s | %-12s | %-25s | Income: %-10s | Credit: %d",
                customerId, fullName, phone, email, monthlyIncome, creditScore);
    }
}
