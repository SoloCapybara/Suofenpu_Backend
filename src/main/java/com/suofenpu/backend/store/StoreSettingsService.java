package com.suofenpu.backend.store;

import static com.suofenpu.backend.store.StoreSettingsDtos.StorePublicResponse;
import static com.suofenpu.backend.store.StoreSettingsDtos.StoreSettingsResponse;
import static com.suofenpu.backend.store.StoreSettingsDtos.UpdateStoreOpenRequest;
import static com.suofenpu.backend.store.StoreSettingsDtos.UpdateStoreSettingsRequest;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreSettingsService {
    private final StoreSettingsRepository repository;

    public StoreSettingsService(StoreSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public StorePublicResponse publicInfo() {
        return StorePublicResponse.from(requireSettings());
    }

    @Transactional(readOnly = true)
    public StoreSettingsResponse adminSettings() {
        return StoreSettingsResponse.from(requireSettings());
    }

    @Transactional
    public StoreSettingsResponse updateStoreOpen(UpdateStoreOpenRequest request) {
        StoreSettings settings = requireSettings();
        settings.updateStoreOpen(request.storeOpen());
        return StoreSettingsResponse.from(repository.save(settings));
    }

    @Transactional
    public StoreSettingsResponse updateSettings(UpdateStoreSettingsRequest request) {
        StoreSettings settings = requireSettings();
        settings.updateProfile(
            request.storeName(),
            request.businessHours(),
            request.deliveryRange(),
            request.notice() == null ? "" : request.notice()
        );
        return StoreSettingsResponse.from(repository.save(settings));
    }

    private StoreSettings requireSettings() {
        return repository.findById(StoreSettings.SINGLETON_ID)
            .orElseGet(this::createDefaultSettings);
    }

    private StoreSettings createDefaultSettings() {
        return repository.save(new StoreSettings(
            "嗦粉铺桂林七星店",
            "10:00 - 22:00",
            "门店 3 公里内",
            "满 38 减 5，预计 25 分钟送达。",
            true
        ));
    }
}
