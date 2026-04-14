package com.vakilsahay.repository;

import com.vakilsahay.entity.Clause;
import com.vakilsahay.entity.Clause.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ClauseRepository extends JpaRepository<Clause, Long> {

    List<Clause> findByDocumentIdOrderByClauseNumber(Long documentId);

    List<Clause> findByDocumentIdAndSeverityOrderByClauseNumber(Long documentId, Severity severity);

    @Query("SELECT c FROM Clause c WHERE c.document.id = :documentId ORDER BY c.severityScore DESC")
    List<Clause> findByDocumentIdOrderBySeverityScoreDesc(Long documentId);

    void deleteAllByDocumentId(Long documentId);
}