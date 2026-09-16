package com.suofenpu.backend.store;

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
@Table(name = "home_banners")
public class HomeBanner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 80)
    private String highlight;

    @Column(nullable = false, length = 120)
    private String badge;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Instant updatedAt;

    protected HomeBanner() {
    }

    public HomeBanner(
        String imageUrl,
        String title,
        String highlight,
        String badge,
        int sortOrder,
        boolean enabled
    ) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.highlight = highlight;
        this.badge = badge;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public String getTitle() { return title; }
    public String getHighlight() { return highlight; }
    public String getBadge() { return badge; }
    public int getSortOrder() { return sortOrder; }
    public boolean isEnabled() { return enabled; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void update(
        String imageUrl,
        String title,
        String highlight,
        String badge,
        int sortOrder,
        boolean enabled
    ) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.highlight = highlight;
        this.badge = badge;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }
}
