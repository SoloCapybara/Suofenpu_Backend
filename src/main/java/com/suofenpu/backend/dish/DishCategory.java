package com.suofenpu.backend.dish;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "dish_categories")
public class DishCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 40)
    private String name;

    @Column(nullable = false, length = 500)
    private String iconUrl;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean enabled;

    @Version
    private Long version;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected DishCategory() {
    }

    public DishCategory(String code, String name, String iconUrl, int sortOrder, boolean enabled) {
        this.code = code;
        this.name = name;
        this.iconUrl = iconUrl;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void updateForAdmin(String code, String name, String iconUrl, Integer sortOrder, Boolean enabled) {
        if (code != null && !code.isBlank()) {
            this.code = code;
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (iconUrl != null && !iconUrl.isBlank()) {
            this.iconUrl = iconUrl;
        }
        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }
        if (enabled != null) {
            this.enabled = enabled;
        }
    }
}
