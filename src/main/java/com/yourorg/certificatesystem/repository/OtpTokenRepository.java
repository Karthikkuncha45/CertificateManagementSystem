package com.yourorg.certificatesystem.repository;


import com.yourorg.certificatesystem.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findByEmailAndOtpAndUsedFalse(String email, String otp);
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.email = :email")
    void deleteByEmail(@Param("email") String email);

}
