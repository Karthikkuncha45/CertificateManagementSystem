package com.yourorg.certificatesystem.repository;

import com.yourorg.certificatesystem.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUserOrderByUploadedAtDesc(User user);
    
    @Query("SELECT c FROM Certificate c JOIN c.user u WHERE " +
           "(:section IS NULL OR u.section = :section) AND " +
           "(:year IS NULL OR u.studyingYear = :year) AND " +
           "(:branch IS NULL OR u.branch = :branch) AND " +
           "(:issueDate IS NULL OR c.issueDate = :issueDate) AND " +
           "(:fromDate IS NULL OR c.issueDate >= :fromDate) AND " +
           "(:toDate IS NULL OR c.issueDate <= :toDate) AND " +
           "(:title IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:minPercentage IS NULL OR c.percentage >= :minPercentage) AND " +
           "(:maxPercentage IS NULL OR c.percentage <= :maxPercentage) AND " +
           "(:rollNumber IS NULL OR LOWER(u.rollNumber) LIKE LOWER(CONCAT('%', :rollNumber, '%')))")
    List<Certificate> findCertificatesWithFilters(
        @Param("section") String section,
        @Param("year") Integer year,
        @Param("branch") String branch,
        @Param("issueDate") LocalDate issueDate,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate,
        @Param("title") String title,
        @Param("minPercentage") Double minPercentage,
        @Param("maxPercentage") Double maxPercentage,
        @Param("rollNumber") String rollNumber
    );
}
