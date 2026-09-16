package com.suofenpu.backend.admin.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_users")
public class AdminUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String displayName;

    @Column(length = 500)
    private String avatarUrl;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected AdminUser() {
    }

    public AdminUser(String username, String passwordHash, String displayName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.displayName = displayName;
        this.role = "OWNER";
        this.enabled = true;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void updateAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void clearAvatarUrl() {
        this.avatarUrl = null;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void updateForManagement(String displayName, String role, Boolean enabled) {
        if (displayName != null && !displayName.isBlank()) this.displayName = displayName.trim();
        if (role != null && !role.isBlank()) this.role = role;
        if (enabled != null) this.enabled = enabled;
    }
}
