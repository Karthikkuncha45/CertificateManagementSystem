package com.yourorg.certificatesystem.controller;

import com.yourorg.certificatesystem.entity.*;
import com.yourorg.certificatesystem.service.*;
import com.yourorg.certificatesystem.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private UserService userService;

    private final String uploadDir = "uploads/certificates/";

    @GetMapping("/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        // Get currently authenticated student
        User user = userService.findByRollNumber(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Fetch certificates for this student
        List<Certificate> certificates = certificateService.getCertificatesByUser(user);

        // Calculate stats
        int totalCertificates = certificates.size();
        double avgScore = 0.0;
        double maxScore = 0.0;

        if (!certificates.isEmpty()) {
            avgScore = certificates.stream()
                    .mapToDouble(Certificate::getPercentage)
                    .average()
                    .orElse(0.0);

            maxScore = certificates.stream()
                    .mapToDouble(Certificate::getPercentage)
                    .max()
                    .orElse(0.0);
        }

        // Put data into the model
        model.addAttribute("certificates", certificates);
        model.addAttribute("user", user);
        model.addAttribute("totalCertificates", totalCertificates);
        model.addAttribute("avgScore", avgScore);
        model.addAttribute("maxScore", maxScore);

        return "student/dashboard";
    }

    @GetMapping("/upload")
    public String showUploadForm(Model model) {
        model.addAttribute("certificate", new Certificate());
        return "student/upload";
    }

 // Remove FileUploadUtil imports and usage

    @PostMapping("/upload")
    public String uploadCertificate(@ModelAttribute Certificate certificate,
                                  @RequestParam("file") MultipartFile file,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        // ... validation code ...
        try {
            User user = userService.findByRollNumber(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            certificate.setUser(user);
            certificateService.saveCertificate(certificate, file);
            redirectAttributes.addFlashAttribute("success", "Certificate uploaded successfully!");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/student/dashboard";
    }

    // Update download method to redirect to Cloudinary URL
    @GetMapping("/download/{id}")
    public String downloadCertificate(@PathVariable Long id, Authentication authentication) {
        Certificate certificate = certificateService.getCertificateById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
        if (!certificate.getUser().getRollNumber().equals(authentication.getName())) {
            return "redirect:/student/dashboard?error=access-denied";
        }
        return "redirect:" + certificate.getCloudinaryUrl();
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Authentication authentication, Model model,
                             RedirectAttributes redirectAttributes) {
        
        Certificate certificate = certificateService.getCertificateById(id)
                .orElse(null);
        
        if (certificate == null || !certificate.getUser().getRollNumber().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "Certificate not found or access denied");
            return "redirect:/student/dashboard";
        }
        
        model.addAttribute("certificate", certificate);
        return "student/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateCertificate(@PathVariable Long id, @ModelAttribute Certificate certificateForm,
                                  @RequestParam(value = "file", required = false) MultipartFile file,
                                  Authentication authentication, RedirectAttributes redirectAttributes) {
        
        Certificate existingCertificate = certificateService.getCertificateById(id)
                .orElse(null);
        
        if (existingCertificate == null || !existingCertificate.getUser().getRollNumber().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "Certificate not found or access denied");
            return "redirect:/student/dashboard";
        }
        
        try {
            existingCertificate.setTitle(certificateForm.getTitle());
            existingCertificate.setPercentage(certificateForm.getPercentage());
            existingCertificate.setIssueDate(certificateForm.getIssueDate());
            
            if (file != null && !file.isEmpty()) {
                // Validate file
                if (!isValidFileType(file.getContentType()) || file.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("error", "Invalid file type or size");
                    return "redirect:/student/edit/" + id;
                }
                
                // Delete old file and save new one
                FileUploadUtil.deleteFile(uploadDir, existingCertificate.getFileName());
                String newFileName = FileUploadUtil.saveFile(uploadDir, file);
                existingCertificate.setFileName(newFileName);
                existingCertificate.setFilePath(uploadDir + newFileName);
            }
            
            certificateService.saveCertificate(existingCertificate);
            redirectAttributes.addFlashAttribute("success", "Certificate updated successfully!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update certificate: " + e.getMessage());
        }
        
        return "redirect:/student/dashboard";
    }

    @PostMapping("/delete/{id}")
    public String deleteCertificate(@PathVariable Long id, Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        
        Certificate certificate = certificateService.getCertificateById(id)
                .orElse(null);
        
        if (certificate == null || !certificate.getUser().getRollNumber().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("error", "Certificate not found or access denied");
            return "redirect:/student/dashboard";
        }
        
        try {
            FileUploadUtil.deleteFile(uploadDir, certificate.getFileName());
            certificateService.deleteCertificate(id);
            redirectAttributes.addFlashAttribute("success", "Certificate deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete certificate");
        }
        
        return "redirect:/student/dashboard";
    }

    

    private boolean isValidFileType(String contentType) {
        return contentType != null && (
                contentType.equals("application/pdf") ||
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png")
        );
    }
}
