package com.suofenpu.backend.store;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class HomeBannerDtos {
    private HomeBannerDtos() {}

    public record HomeBannerItem(
        Long id,
        @NotBlank @Size(max = 500) String imageUrl,
        @NotBlank @Size(max = 80) String title,
        @NotBlank @Size(max = 80) String highlight,
        @NotBlank @Size(max = 120) String badge,
        @NotNull Boolean enabled
    ) {
        public static HomeBannerItem from(HomeBanner banner) {
            return new HomeBannerItem(
                banner.getId(),
                banner.getImageUrl(),
                banner.getTitle(),
                banner.getHighlight(),
                banner.getBadge(),
                banner.isEnabled()
            );
        }
    }

    public record HomeBannerPublicItem(
        String image,
        String title,
        String highlight,
        String badge
    ) {
        public static HomeBannerPublicItem from(HomeBanner banner) {
            return new HomeBannerPublicItem(
                banner.getImageUrl(),
                banner.getTitle(),
                banner.getHighlight(),
                banner.getBadge()
            );
        }
    }

    public record ReplaceHomeBannersRequest(
        @Valid @NotNull List<HomeBannerItem> banners
    ) {}
}
