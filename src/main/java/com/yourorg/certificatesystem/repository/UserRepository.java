// repository/UserRepository.java
package com.yourorg.certificatesystem.repository;

import com.yourorg.certificatesystem.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByRollNumber(String rollNumber);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByRollNumber(String rollNumber);
    
    boolean existsByEmail(String email);
    
    // Custom update method to ensure proper persistence
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.fullName = :fullName, u.studyingYear = :studyingYear, " +
           "u.branch = :branch, u.section = :section, u.email = :email, u.phoneNumber = :phoneNumber " +
           "WHERE u.id = :id")
    int updateUserProfile(@Param("id") Long id,
                         @Param("fullName") String fullName,
                         @Param("studyingYear") Integer studyingYear,
                         @Param("branch") String branch,
                         @Param("section") String section,
                         @Param("email") String email,
                         @Param("phoneNumber") String phoneNumber);
    
    // Method to update password separately
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    int updateUserPassword(@Param("id") Long id, @Param("password") String password);
    
    // Enable user account
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.enabled = :enabled WHERE u.email = :email")
    int updateUserStatus(@Param("email") String email, @Param("enabled") boolean enabled);
    
    // Check if email exists for another user (excluding current user)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);
}