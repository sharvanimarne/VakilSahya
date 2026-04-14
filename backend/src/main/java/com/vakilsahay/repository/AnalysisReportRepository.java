package com.vakilsahay.repository;

import com.vakilsahay.entity.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    Optional<AnalysisReport> findByDocumentId(Long documentId);
    void deleteByDocumentId(Long documentId);
}