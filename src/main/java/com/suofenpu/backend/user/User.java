package com.suofenpu.backend.user;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 128)
    private String openid;

    @Column(length = 128)
    private String unionid;

    @Column(unique = true, length = 30)
    private String phone;

    @Column(length = 80)
    private String nickname;

    @Column(length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected User() {
    }

    public User(String openid, String unionid, String phone, String nickname, String avatarUrl) {
        this.openid = openid;
        this.unionid = unionid;
        this.phone = phone;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.status = UserStatus.ACTIVE;
    }

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getOpenid() {
        return openid;
    }

    public String getUnionid() {
        return unionid;
    }

    public String getPhone() {
        return phone;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void mergeProfile(String phone, String nickname, String unionid) {
        if (phone != null && !phone.isBlank()) {
            this.phone = phone;
        }
        if (nickname != null && !nickname.isBlank()) {
            this.nickname = nickname;
        }
        if (unionid != null && !unionid.isBlank()) {
            this.unionid = unionid;
        }
    }

    public void updateStatus(UserStatus status) {
        this.status = status;
    }
}
