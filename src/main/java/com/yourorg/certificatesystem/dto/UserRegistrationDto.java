package com.yourorg.certificatesystem.dto;


import jakarta.validation.constraints.*;

public class UserRegistrationDto {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Roll number is required")
    private String rollNumber;

    @Min(value = 1, message = "Year must be between 1 and 4")
    @Max(value = 4, message = "Year must be between 1 and 4")
    private Integer studyingYear;

    @NotBlank(message = "Branch is required")
    private String branch;

    @NotBlank(message = "Section is required")
    private String section;

    @Email(message = "Please provide a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please retype password")
    private String retypePassword;

    // Constructors, getters and setters
    public UserRegistrationDto() {}

    // Getters and Setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public Integer getStudyingYear() { return studyingYear; }
    public void setStudyingYear(Integer studyingYear) { this.studyingYear = studyingYear; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRetypePassword() { return retypePassword; }
    public void setRetypePassword(String retypePassword) { this.retypePassword = retypePassword; }
}