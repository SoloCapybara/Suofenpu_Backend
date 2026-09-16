package com.suofenpu.backend.store;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "store_settings")
public class StoreSettings {
    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id;

    @Column(nullable = false, length = 120)
    private String storeName;

    @Column(nullable = false, length = 80)
    private String businessHours;

    @Column(nullable = false, length = 120)
    private String deliveryRange;

    @Column(nullable = false, length = 500)
    private String notice;

    @Column(nullable = false)
    private boolean storeOpen;

    @Column(nullable = false)
    private Instant updatedAt;

    protected StoreSettings() {
    }

    public StoreSettings(
        String storeName,
        String businessHours,
        String deliveryRange,
        String notice,
        boolean storeOpen
    ) {
        this.id = SINGLETON_ID;
        this.storeName = storeName;
        this.businessHours = businessHours;
        this.deliveryRange = deliveryRange;
        this.notice = notice;
        this.storeOpen = storeOpen;
    }

    @PrePersist
    @PreUpdate
    void touchUpdatedAt() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getStoreName() { return storeName; }
    public String getBusinessHours() { return businessHours; }
    public String getDeliveryRange() { return deliveryRange; }
    public String getNotice() { return notice; }
    public boolean isStoreOpen() { return storeOpen; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void updateStoreOpen(boolean storeOpen) {
        this.storeOpen = storeOpen;
    }

    public void updateProfile(String storeName, String businessHours, String deliveryRange, String notice) {
        if (storeName != null && !storeName.isBlank()) this.storeName = storeName.trim();
        if (businessHours != null && !businessHours.isBlank()) this.businessHours = businessHours.trim();
        if (deliveryRange != null && !deliveryRange.isBlank()) this.deliveryRange = deliveryRange.trim();
        if (notice != null) this.notice = notice.trim();
    }
}
