package com.vakilsahay.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "document_id")
    private Long documentId;

    @Column(nullable = false)
    private String action; // UPLOAD, ANALYZE, DOWNLOAD_REPORT, DELETE

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}