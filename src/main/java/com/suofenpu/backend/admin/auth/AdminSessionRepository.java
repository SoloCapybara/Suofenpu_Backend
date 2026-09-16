package com.suofenpu.backend.admin.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminSessionRepository extends JpaRepository<AdminSession, Long> {
    @EntityGraph(attributePaths = "adminUser")
    Optional<AdminSession> findByToken(String token);
    void deleteByToken(String token);
    void deleteByAdminUserIdAndTokenNot(Long adminUserId, String token);
}
