package com.suofenpu.backend.admin.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_sessions")
public class AdminSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_user_id")
    private AdminUser adminUser;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    protected AdminSession() {
    }

    public AdminSession(String token, AdminUser adminUser, Instant expiresAt) {
        this.token = token;
        this.adminUser = adminUser;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void prePersist() { createdAt = Instant.now(); }

    public String getToken() { return token; }
    public AdminUser getAdminUser() { return adminUser; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isExpired() { return expiresAt.isBefore(Instant.now()); }
}
