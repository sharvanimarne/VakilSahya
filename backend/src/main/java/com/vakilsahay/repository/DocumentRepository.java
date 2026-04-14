package com.vakilsahay.repository;

import com.vakilsahay.entity.Document;
import com.vakilsahay.entity.Document.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    Page<Document> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Document> findByIdAndUserId(Long id, Long userId);

    List<Document> findByUserIdAndStatus(Long userId, DocumentStatus status);

    @Query("SELECT COUNT(d) FROM Document d WHERE d.user.id = :userId")
    long countByUserId(Long userId);

    @Query("SELECT COUNT(d) FROM Document d")
    long countTotal();
}