package com.yourorg.certificatesystem.controller;

import com.yourorg.certificatesystem.dto.*;
import com.yourorg.certificatesystem.entity.*;
import com.yourorg.certificatesystem.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {

 @Autowired
 private UserService userService;

 @Autowired
 private OtpService otpService;

 @GetMapping("/")
 public String home(Authentication authentication) {
     if (authentication != null && authentication.isAuthenticated()) {
         return "redirect:/dashboard";
     }
     return "redirect:/login";
 }

 @GetMapping("/login")
 public String login() {
     return "login";
 }

 @GetMapping("/register")
 public String showRegistrationForm(Model model) {
     model.addAttribute("user", new UserRegistrationDto());
     return "register";
 }

 @PostMapping("/register")
 public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                          BindingResult result, Model model, HttpSession session,
                          RedirectAttributes redirectAttributes) {
     
     if (result.hasErrors()) {
         return "register";
     }

     if (!userDto.getPassword().equals(userDto.getRetypePassword())) {
         model.addAttribute("error", "Passwords do not match");
         return "register";
     }

     if (userService.existsByRollNumber(userDto.getRollNumber())) {
         model.addAttribute("error", "Roll number already exists");
         return "register";
     }

     if (userService.existsByEmail(userDto.getEmail())) {
         model.addAttribute("error", "Email already exists");
         return "register";
     }

     // Store user data in session and send OTP
     session.setAttribute("registrationData", userDto);
     otpService.generateAndSendOtp(userDto.getEmail());
     
     redirectAttributes.addAttribute("email", userDto.getEmail());
     redirectAttributes.addAttribute("type", "registration");
     return "redirect:/otp-verification";
 }

 @GetMapping("/otp-verification")
 public String showOtpVerification(@RequestParam String email, 
                                 @RequestParam(required = false, defaultValue = "registration") String type,
                                 Model model) {
     model.addAttribute("email", email);
     model.addAttribute("type", type);
     return "otp-verification";
 }

 @PostMapping("/verify-otp")
 public String verifyOtp(@RequestParam String email, 
                        @RequestParam String otp,
                        @RequestParam(required = false, defaultValue = "registration") String type,
                        HttpSession session, Model model, RedirectAttributes redirectAttributes) {
     
     if (otpService.validateOtp(email, otp)) {
         
         if ("reset".equals(type)) {
             // For password reset flow
             session.setAttribute("otpVerified", true);
             session.setAttribute("resetEmail", email);
             return "redirect:/reset-password?email=" + email;
             
         } else {
             // For registration flow
             UserRegistrationDto userDto = (UserRegistrationDto) session.getAttribute("registrationData");
             
             if (userDto != null) {
                 User user = new User();
                 user.setFullName(userDto.getFullName());
                 user.setRollNumber(userDto.getRollNumber());
                 user.setStudyingYear(userDto.getStudyingYear());
                 user.setBranch(userDto.getBranch());
                 user.setSection(userDto.getSection());
                 user.setEmail(userDto.getEmail());
                 user.setPhoneNumber(userDto.getPhoneNumber());
                 user.setPassword(userDto.getPassword());
                 user.setRole("STUDENT");
                 user.setEnabled(true);
                 
                 userService.saveUser(user);
                 session.removeAttribute("registrationData");
                 
                 redirectAttributes.addFlashAttribute("success", "Registration successful! Please login.");
                 return "redirect:/login";
             }
         }
     }
     
     model.addAttribute("email", email);
     model.addAttribute("type", type);
     model.addAttribute("error", "Invalid or expired OTP");
     return "otp-verification";
 }

 @PostMapping("/resend-otp")
 @ResponseBody
 public String resendOtp(@RequestParam String email) {
     try {
         otpService.generateAndSendOtp(email);
         return "success";
     } catch (Exception e) {
         return "error";
     }
 }

 @GetMapping("/forgot-password")
 public String showForgotPassword() {
     return "forgot-password";
 }

 @PostMapping("/forgot-password")
 public String processForgotPassword(@RequestParam String email, HttpSession session,
                                   Model model, RedirectAttributes redirectAttributes) {
     
     if (userService.findByEmail(email).isPresent()) {
         session.setAttribute("resetEmail", email);
         otpService.generateAndSendOtp(email);
         redirectAttributes.addAttribute("email", email);
         redirectAttributes.addAttribute("type", "reset");
         return "redirect:/otp-verification";
     }
     
     model.addAttribute("error", "Email not found");
     return "forgot-password";
 }

 @GetMapping("/reset-password")
 public String showResetPassword(@RequestParam String email, HttpSession session, Model model) {
     // Check if OTP was verified
     if (!Boolean.TRUE.equals(session.getAttribute("otpVerified")) || 
         !email.equals(session.getAttribute("resetEmail"))) {
         return "redirect:/forgot-password";
     }
     
     model.addAttribute("email", email);
     return "reset-password";
 }

 @PostMapping("/reset-password")
 public String resetPassword(@RequestParam String email, 
                           @RequestParam String password,
                           @RequestParam String retypePassword, 
                           HttpSession session,
                           Model model, RedirectAttributes redirectAttributes) {
     
     // Verify session state
     if (!Boolean.TRUE.equals(session.getAttribute("otpVerified")) || 
         !email.equals(session.getAttribute("resetEmail"))) {
         return "redirect:/forgot-password";
     }

     if (password.length() < 6) {
         model.addAttribute("email", email);
         model.addAttribute("error", "Password must be at least 6 characters long");
         return "reset-password";
     }

     if (!password.equals(retypePassword)) {
         model.addAttribute("email", email);
         model.addAttribute("error", "Passwords do not match");
         return "reset-password";
     }

     try {
         userService.updatePassword(email, password);
         
         // Clean up session
         session.removeAttribute("otpVerified");
         session.removeAttribute("resetEmail");
         
         redirectAttributes.addFlashAttribute("success", "Password reset successful! Please login with your new password.");
         return "redirect:/login";
         
     } catch (Exception e) {
         model.addAttribute("email", email);
         model.addAttribute("error", "Failed to reset password. Please try again.");
         return "reset-password";
     }
 }

 @GetMapping("/dashboard")
 public String dashboard(Authentication authentication) {
     if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
         return "redirect:/admin/dashboard";
     } else {
         return "redirect:/student/dashboard";
     }
 }
}