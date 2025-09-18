// controller/ProfileController.java
package com.yourorg.certificatesystem.controller;

import com.yourorg.certificatesystem.entity.*;
import com.yourorg.certificatesystem.service.*;
import com.yourorg.certificatesystem.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        User user = userService.findByRollNumber(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute User userForm, BindingResult result,
                              @RequestParam String currentPassword, Authentication authentication,
                              Model model, RedirectAttributes redirectAttributes) {
        
        // Always fetch the existing user from database first
        User existingUser = userService.findByRollNumber(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (result.hasErrors()) {
            model.addAttribute("user", existingUser);
            return "profile";
        }

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
            model.addAttribute("error", "Current password is incorrect");
            model.addAttribute("user", existingUser);
            return "profile";
        }

        // Check if email is being changed and if it already exists for another user
        if (!existingUser.getEmail().equals(userForm.getEmail())) {
            if (userService.existsByEmail(userForm.getEmail())) {
                model.addAttribute("error", "Email already exists for another user");
                model.addAttribute("user", existingUser);
                return "profile";
            }
        }

        try {
            // Update user details (keeping the ID and other critical fields)
            existingUser.setFullName(userForm.getFullName());
            existingUser.setStudyingYear(userForm.getStudyingYear());
            existingUser.setBranch(userForm.getBranch());
            existingUser.setSection(userForm.getSection());
            existingUser.setEmail(userForm.getEmail());
            existingUser.setPhoneNumber(userForm.getPhoneNumber());

            // Save and flush to ensure immediate persistence
            User savedUser = userService.updateProfile(existingUser);
            
            // Add debug logging
            System.out.println("Updated user: " + savedUser.getFullName() + ", Email: " + savedUser.getEmail());
            
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
            return "redirect:/profile";
            
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
            model.addAttribute("user", existingUser);
            return "profile";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        User existingUser = userService.findByRollNumber(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, existingUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Current password is incorrect");
            return "redirect:/profile";
        }

        // Check if new passwords match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile";
        }

        // Password strength validation
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters long");
            return "redirect:/profile";
        }

        try {
            // Update password
            existingUser.setPassword(passwordEncoder.encode(newPassword));
            userService.updateProfile(existingUser);
            
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
            return "redirect:/profile";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to change password");
            return "redirect:/profile";
        }
    }
}