package com.yourorg.certificatesystem.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    @Value("${mail.smtp.host:smtp.gmail.com}")
    private String smtpHost;

    @Value("${mail.smtp.port:587}")
    private String smtpPort;

    @Value("${mail.smtp.username:kunchakarthik0@gmail.com}")
    private String username;

    @Value("${mail.smtp.password:your-app-password}")
    private String password;

    @Value("${mail.smtp.auth:true}")
    private String auth;

    @Value("${mail.smtp.starttls:true}")
    private String starttls;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            // Create properties for SMTP configuration
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", auth);
            props.put("mail.smtp.starttls.enable", starttls);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            // Create message
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Certificate Management System - OTP Verification");
            
            // Create email body
            String emailBody = "Dear User,\n\n" +
                             "Your OTP for verification is: " + otp + "\n\n" +
                             "This OTP is valid for 10 minutes only.\n" +
                             "Please do not share this OTP with anyone.\n\n" +
                             "Best regards,\n" +
                             "Certificate Management System";
            
            message.setText(emailBody);

            // Send message
            Transport.send(message);
            
            System.out.println("OTP email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    // Method to send HTML emails (optional)
//    public void sendOtpEmailHtml(String toEmail, String otp) {
//        try {
//            Properties props = new Properties();
//            props.put("mail.smtp.host", smtpHost);
//            props.put("mail.smtp.port", smtpPort);
//            props.put("mail.smtp.auth", auth);
//            props.put("mail.smtp.starttls.enable", starttls);
//            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
//
//            Session session = Session.getInstance(props, new Authenticator() {
//                @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                    return new PasswordAuthentication(username, password);
//                }
//            });
//
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(username));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
//            message.setSubject("Certificate Management System - OTP Verification");
//
//            // HTML content
//            String htmlContent = """
//                <html>
//                <body style="font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f4f4f4;">
//                    <div style="max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
//                        <h2 style="color: #333; text-align: center; margin-bottom: 30px;">Certificate Management System</h2>
//                        <div style="background-color: #f8f9fa; padding: 20px; border-radius: 5px; text-align: center; margin: 20px 0;">
//                            <h3 style="color: #007bff; margin: 0;">Your OTP Code</h3>
//                            <div style="font-size: 32px; font-weight: bold; color: #007bff; margin: 15px 0; letter-spacing: 5px;">%s</div>
//                            <p style="color: #666; margin: 10px 0;">This OTP is valid for 10 minutes</p>
//                        </div>
//                        <p style="color: #333; line-height: 1.6;">Please use this OTP to complete your verification. Do not share this code with anyone.</p>
//                        <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; color: #666; font-size: 12px; text-align: center;">
//                            This is an automated message. Please do not reply to this email.
//                        </div>
//                    </div>
//                </body>
//                </html>
//                """.formatted(otp);
//
//            message.setContent(htmlContent, "text/html; charset=utf-8");
//            Transport.send(message);
//            
//            System.out.println("HTML OTP email sent successfully to: " + toEmail);
//
//        } catch (MessagingException e) {
//            System.err.println("Failed to send HTML OTP email: " + e.getMessage());
//            throw new RuntimeException("Failed to send OTP email", e);
//        }
//    }
}