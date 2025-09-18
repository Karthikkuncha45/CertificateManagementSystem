package com.yourorg.certificatesystem.service;

import com.yourorg.certificatesystem.entity.*;
import com.yourorg.certificatesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.transaction.annotation.Transactional;

@Service
public class OtpService {

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private EmailService emailService;

    @Transactional  // ADD THIS
    public String generateAndSendOtp(String email) {
        // Delete existing OTPs for this email
        otpTokenRepository.deleteByEmail(email);

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Save OTP token with 10-minute expiry
        OtpToken otpToken = new OtpToken(email, otp, LocalDateTime.now().plusMinutes(10));
        otpTokenRepository.save(otpToken);

        // Send email
        emailService.sendOtpEmail(email, otp);

        return otp;
    }

    @Transactional  // You can also mark this so 'save' is bound in same persistence context
    public boolean validateOtp(String email, String otp) {
        return otpTokenRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .map(token -> {
                    if (token.getExpiryTime().isAfter(LocalDateTime.now())) {
                        token.setUsed(true);
                        otpTokenRepository.save(token);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }
}
