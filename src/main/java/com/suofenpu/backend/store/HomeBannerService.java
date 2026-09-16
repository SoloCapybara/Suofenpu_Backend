package com.suofenpu.backend.store;

import static com.suofenpu.backend.store.HomeBannerDtos.HomeBannerItem;
import static com.suofenpu.backend.store.HomeBannerDtos.HomeBannerPublicItem;
import static com.suofenpu.backend.store.HomeBannerDtos.ReplaceHomeBannersRequest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeBannerService {
    private final HomeBannerRepository repository;

    public HomeBannerService(HomeBannerRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<HomeBannerItem> adminList() {
        return repository.findAllByOrderBySortOrderAscIdAsc().stream()
            .map(HomeBannerItem::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<HomeBannerPublicItem> publicList() {
        return repository.findAllByEnabledTrueOrderBySortOrderAscIdAsc().stream()
            .map(HomeBannerPublicItem::from)
            .toList();
    }

    @Transactional
    public List<HomeBannerItem> replaceAll(ReplaceHomeBannersRequest request) {
        repository.deleteAll();
        List<HomeBanner> saved = new ArrayList<>();
        int sortOrder = 1;
        for (HomeBannerItem item : request.banners()) {
            saved.add(repository.save(new HomeBanner(
                item.imageUrl().trim(),
                item.title().trim(),
                item.highlight().trim(),
                item.badge().trim(),
                sortOrder++,
                Boolean.TRUE.equals(item.enabled())
            )));
        }
        return saved.stream().map(HomeBannerItem::from).toList();
    }
}
