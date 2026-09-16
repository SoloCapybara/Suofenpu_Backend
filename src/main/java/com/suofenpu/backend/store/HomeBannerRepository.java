package com.suofenpu.backend.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {
    List<HomeBanner> findAllByOrderBySortOrderAscIdAsc();

    List<HomeBanner> findAllByEnabledTrueOrderBySortOrderAscIdAsc();
}
