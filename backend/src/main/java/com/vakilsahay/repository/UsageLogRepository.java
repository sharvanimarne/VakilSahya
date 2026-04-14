package com.vakilsahay.repository;

import com.vakilsahay.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long> {
    long countByUserId(Long userId);
}