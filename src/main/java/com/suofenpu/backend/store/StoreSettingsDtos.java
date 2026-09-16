package com.suofenpu.backend.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class StoreSettingsDtos {
    private StoreSettingsDtos() {}

    public record StorePublicResponse(
        boolean storeOpen,
        String storeName,
        String businessHours,
        String statusLabel
    ) {
        public static StorePublicResponse from(StoreSettings settings) {
            return new StorePublicResponse(
                settings.isStoreOpen(),
                settings.getStoreName(),
                settings.getBusinessHours(),
                settings.isStoreOpen() ? "营业中" : "已打烊"
            );
        }
    }

    public record StoreSettingsResponse(
        String storeName,
        String businessHours,
        String deliveryRange,
        String notice,
        boolean storeOpen
    ) {
        public static StoreSettingsResponse from(StoreSettings settings) {
            return new StoreSettingsResponse(
                settings.getStoreName(),
                settings.getBusinessHours(),
                settings.getDeliveryRange(),
                settings.getNotice(),
                settings.isStoreOpen()
            );
        }
    }

    public record UpdateStoreOpenRequest(@NotNull Boolean storeOpen) {}

    public record UpdateStoreSettingsRequest(
        @NotBlank @Size(max = 120) String storeName,
        @NotBlank @Size(max = 80) String businessHours,
        @NotBlank @Size(max = 120) String deliveryRange,
        @Size(max = 500) String notice
    ) {}
}
