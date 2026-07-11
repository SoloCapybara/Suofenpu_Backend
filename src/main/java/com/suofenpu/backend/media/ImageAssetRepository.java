package com.suofenpu.backend.media;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {
    List<ImageAsset> findAllByOrderBySortOrderAscIdAsc();

    List<ImageAsset> findByEnabledTrueOrderBySortOrderAscIdAsc();
}
