package com.datn.backend.repository;

import com.datn.backend.entity.UserBehaviorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserBehaviorLogRepository extends JpaRepository<UserBehaviorLog, Long> {
    List<UserBehaviorLog> findTop200ByUser_UserIdOrderByCreatedAtDesc(Long userId);
    List<UserBehaviorLog> findTop200BySessionIdOrderByCreatedAtDesc(String sessionId);
}
