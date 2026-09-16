package com.suofenpu.backend.store;

import static com.suofenpu.backend.store.HomeBannerDtos.HomeBannerPublicItem;
import static com.suofenpu.backend.store.StoreSettingsDtos.StorePublicResponse;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/store")
@Tag(name = "门店信息")
public class StoreController {
    private final StoreSettingsService storeSettingsService;
    private final HomeBannerService homeBannerService;

    public StoreController(StoreSettingsService storeSettingsService, HomeBannerService homeBannerService) {
        this.storeSettingsService = storeSettingsService;
        this.homeBannerService = homeBannerService;
    }

    @GetMapping
    @Operation(summary = "获取门店公开信息", description = "小程序使用，返回营业状态与门店名称等公开信息。")
    public StorePublicResponse info() {
        return storeSettingsService.publicInfo();
    }

    @GetMapping("/banners")
    @Operation(summary = "获取首页轮播图", description = "小程序使用，返回已启用的首页轮播图。")
    public List<HomeBannerPublicItem> banners() {
        return homeBannerService.publicList();
    }
}
