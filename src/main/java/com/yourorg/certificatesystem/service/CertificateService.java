package com.yourorg.certificatesystem.service;

import com.yourorg.certificatesystem.entity.*;
import com.yourorg.certificatesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class CertificateService {

    @Autowired
    private CertificateRepository certificateRepository;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    public Certificate saveCertificate(Certificate certificate, MultipartFile file) throws IOException {
        String cloudinaryUrl = cloudinaryService.uploadFile(file);
        certificate.setCloudinaryUrl(cloudinaryUrl);
        return certificateRepository.save(certificate);
    }

    // Update other methods to remove local file handling

    public Certificate saveCertificate(Certificate certificate) {
        return certificateRepository.save(certificate);
    }

    public List<Certificate> getCertificatesByUser(User user) {
        return certificateRepository.findByUserOrderByUploadedAtDesc(user);
    }

    public Optional<Certificate> getCertificateById(Long id) {
        return certificateRepository.findById(id);
    }

    public void deleteCertificate(Long id) {
        certificateRepository.deleteById(id);
    }

    public List<Certificate> getFilteredCertificates(String section, Integer year, String branch,
                                                   LocalDate issueDate, LocalDate fromDate, LocalDate toDate,
                                                   String title, Double minPercentage, Double maxPercentage,
                                                   String rollNumber, String sortBy, String sortOrder) {
        
        List<Certificate> certificates = certificateRepository.findCertificatesWithFilters(
                section, year, branch, issueDate, fromDate, toDate, title, minPercentage, maxPercentage, rollNumber);

        // Apply sorting
        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Certificate> comparator = null;
            
            switch (sortBy.toLowerCase()) {
                case "rollnumber":
                    comparator = Comparator.comparing(cert -> cert.getUser().getRollNumber());
                    break;
                case "percentage":
                    comparator = Comparator.comparing(Certificate::getPercentage);
                    break;
                default:
                    comparator = Comparator.comparing(Certificate::getUploadedAt);
            }
            
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            
            certificates = certificates.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        }

        return certificates;
    }
}