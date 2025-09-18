// service/UserService.java
package com.yourorg.certificatesystem.service;

import com.yourorg.certificatesystem.entity.*;
import com.yourorg.certificatesystem.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String rollNumber) throws UsernameNotFoundException {
        User user = userRepository.findByRollNumber(rollNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + rollNumber));

        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(rollNumber);
        builder.password(user.getPassword());
        builder.roles(user.getRole());
        builder.disabled(!user.isEnabled());

        return builder.build();
    }

    @Transactional
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByRollNumber(String rollNumber) {
        return userRepository.findByRollNumber(rollNumber);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean existsByRollNumber(String rollNumber) {
        return userRepository.existsByRollNumber(rollNumber);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public User updateProfile(User user) {
        // Ensure the user exists and get the managed entity
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Update the managed entity with new values
        existingUser.setFullName(user.getFullName());
        existingUser.setStudyingYear(user.getStudyingYear());
        existingUser.setBranch(user.getBranch());
        existingUser.setSection(user.getSection());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        
        // Only update password if it's provided and different
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }
        
        // Save and return the updated entity
        User savedUser = userRepository.save(existingUser);
        
        // Force flush to ensure immediate persistence
        userRepository.flush();
        
        return savedUser;
    }

    @Transactional
    public User updateUserWithPassword(User user, String newPassword) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        existingUser.setFullName(user.getFullName());
        existingUser.setStudyingYear(user.getStudyingYear());
        existingUser.setBranch(user.getBranch());
        existingUser.setSection(user.getSection());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        
        User savedUser = userRepository.save(existingUser);
        userRepository.flush();
        
        return savedUser;
    }
}