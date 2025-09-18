package com.yourorg.certificatesystem.dto;

import java.time.LocalDate;

public class CertificateDto {
    private Long id;
    private String title;
    private Double percentage;
    private LocalDate issueDate;
    private String cloudinaryUrl;
    private String studentName;
    private String rollNumber;
    private String branch;
    private Integer year;
    private String section;

    // Constructors, getters and setters
    public CertificateDto() {}

    public CertificateDto(Long id, String title, Double percentage, LocalDate issueDate, 
            String cloudinaryUrl, String studentName, String rollNumber, 
            String branch, Integer year, String section) {

        this.id = id;
        this.title = title;
        this.percentage = percentage;
        this.issueDate = issueDate;
        this.cloudinaryUrl = cloudinaryUrl;
        this.studentName = studentName;
        this.rollNumber = rollNumber;
        this.branch = branch;
        this.year = year;
        this.section = section;
    }

    public String getCloudinaryUrl() {
		return cloudinaryUrl;
	}

	public void setCloudinaryUrl(String cloudinaryUrl) {
		this.cloudinaryUrl = cloudinaryUrl;
	}

	// Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }


    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}