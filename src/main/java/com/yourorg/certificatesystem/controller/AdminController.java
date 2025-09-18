package com.yourorg.certificatesystem.controller;

import com.yourorg.certificatesystem.dto.*;
import com.yourorg.certificatesystem.entity.*;
import com.yourorg.certificatesystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CertificateService certificateService;

    private final String uploadDir = "uploads/certificates/";

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        List<Certificate> certificates = certificateService.getFilteredCertificates(
                null, null, null, null, null, null, null, null, null, null, null, null);
        
        List<CertificateDto> certificateDtos = certificates.stream()
                .map(cert -> new CertificateDto(
                        cert.getId(),
                        cert.getTitle(),
                        cert.getPercentage(),
                        cert.getIssueDate(),
                        cert.getFileName(),
                        cert.getUser().getFullName(),
                        cert.getUser().getRollNumber(),
                        cert.getUser().getBranch(),
                        cert.getUser().getStudyingYear(),
                        cert.getUser().getSection()
                ))
                .collect(Collectors.toList());
        
        model.addAttribute("certificates", certificateDtos);
        return "admin/dashboard";
    }

    @GetMapping("/filter-certificates")
    @ResponseBody
    public List<CertificateDto> filterCertificates(
            @RequestParam(required = false) String section,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate issueDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double minPercentage,
            @RequestParam(required = false) Double maxPercentage,
            @RequestParam(required = false) String rollNumber,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder) {

        List<Certificate> certificates = certificateService.getFilteredCertificates(
                section, year, branch, issueDate, fromDate, toDate, title, 
                minPercentage, maxPercentage, rollNumber, sortBy, sortOrder);

        return certificates.stream()
                .map(cert -> new CertificateDto(
                        cert.getId(),
                        cert.getTitle(),
                        cert.getPercentage(),
                        cert.getIssueDate(),
                        cert.getFileName(),
                        cert.getUser().getFullName(),
                        cert.getUser().getRollNumber(),
                        cert.getUser().getBranch(),
                        cert.getUser().getStudyingYear(),
                        cert.getUser().getSection()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadCertificate(@PathVariable Long id) {
        try {
            Certificate certificate = certificateService.getCertificateById(id)
                    .orElseThrow(() -> new RuntimeException("Certificate not found"));

            Path filePath = Paths.get(uploadDir).resolve(certificate.getFileName()).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                               "attachment; filename=\"" + certificate.getFileName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("File not found");
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}