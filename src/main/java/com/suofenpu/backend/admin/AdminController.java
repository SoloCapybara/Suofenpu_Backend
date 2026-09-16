package com.suofenpu.backend.admin;

import static com.suofenpu.backend.dish.DishDtos.AdminDishCategoryCreateRequest;
import static com.suofenpu.backend.dish.DishDtos.AdminDishCategoryUpdateRequest;
import static com.suofenpu.backend.dish.DishDtos.AdminDishCreateRequest;
import static com.suofenpu.backend.dish.DishDtos.AdminDishUpdateRequest;
import static com.suofenpu.backend.dish.DishDtos.DishCategoryResponse;
import static com.suofenpu.backend.dish.DishDtos.DishResponse;
import static com.suofenpu.backend.media.GalleryDtos.CreateGalleryFolderRequest;
import static com.suofenpu.backend.media.GalleryDtos.GalleryFolderResponse;
import static com.suofenpu.backend.media.GalleryDtos.GalleryImageResponse;
import static com.suofenpu.backend.media.GalleryDtos.GalleryResponse;
import static com.suofenpu.backend.order.OrderDtos.OrderResponse;
import static com.suofenpu.backend.order.OrderDtos.UpdateOrderStatusRequest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.suofenpu.backend.common.ApiException;
import com.suofenpu.backend.dish.Dish;
import com.suofenpu.backend.dish.DishCategory;
import com.suofenpu.backend.dish.DishCategoryRepository;
import com.suofenpu.backend.dish.DishRepository;
import com.suofenpu.backend.media.GalleryStorageService;
import com.suofenpu.backend.order.OrderService;
import com.suofenpu.backend.order.OrderStatus;
import com.suofenpu.backend.store.HomeBannerDtos.HomeBannerItem;
import com.suofenpu.backend.store.HomeBannerDtos.ReplaceHomeBannersRequest;
import com.suofenpu.backend.store.HomeBannerService;
import com.suofenpu.backend.store.StoreSettingsDtos.StoreSettingsResponse;
import com.suofenpu.backend.store.StoreSettingsDtos.UpdateStoreOpenRequest;
import com.suofenpu.backend.store.StoreSettingsDtos.UpdateStoreSettingsRequest;
import com.suofenpu.backend.store.StoreSettingsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "商家后台")
public class AdminController {
    private final DishRepository dishRepository;
    private final DishCategoryRepository dishCategoryRepository;
    private final GalleryStorageService galleryStorageService;
    private final OrderService orderService;
    private final StoreSettingsService storeSettingsService;
    private final HomeBannerService homeBannerService;

    public AdminController(
        DishRepository dishRepository,
        DishCategoryRepository dishCategoryRepository,
        GalleryStorageService galleryStorageService,
        OrderService orderService,
        StoreSettingsService storeSettingsService,
        HomeBannerService homeBannerService
    ) {
        this.dishRepository = dishRepository;
        this.dishCategoryRepository = dishCategoryRepository;
        this.galleryStorageService = galleryStorageService;
        this.orderService = orderService;
        this.storeSettingsService = storeSettingsService;
        this.homeBannerService = homeBannerService;
    }

    @GetMapping("/orders")
    @Operation(
        summary = "后台订单列表",
        description = "商家后台使用。默认返回最近100条订单；可按状态筛选，例如 status=PAID 查看待接单订单。"
    )
    public List<OrderResponse> listOrders(
        @Parameter(
            description = "可选订单状态筛选。不传返回最近100条；传 PAID、PREPARING 等只返回对应状态订单。状态含义见 OrderStatus 枚举说明",
            example = "PAID"
        )
        @RequestParam(required = false)
        OrderStatus status
    ) {
        return orderService.listOrders(status);
    }

    @PatchMapping("/orders/{orderNo}/status")
    @Operation(
        summary = "更新订单状态",
        description = "商家后台推进订单流程，例如 PAID -> ACCEPTED -> PREPARING -> DELIVERING -> COMPLETED。当前未限制状态流转顺序，后续可加状态机校验。"
    )
    public OrderResponse updateOrderStatus(
        @Parameter(description = "订单号，来自下单响应 orderNo 或后台订单列表", example = "SF20260701113926919958")
        @PathVariable String orderNo,
        @Valid @RequestBody UpdateOrderStatusRequest request
    ) {
        return orderService.updateStatus(orderNo, request.status());
    }

    @GetMapping("/dishes")
    @Operation(summary = "后台菜品列表", description = "商家后台使用。返回全部菜品，包括下架菜品。")
    public List<DishResponse> listDishes() {
        return dishRepository.findAllWithCategory().stream().map(DishResponse::from).toList();
    }

    @GetMapping("/dish-categories")
    @Operation(summary = "后台菜品分类列表", description = "商家后台新增/编辑商品时使用。返回全部分类，包括停用分类。")
    public List<DishCategoryResponse> listDishCategories() {
        return dishCategoryRepository.findAllByOrderBySortOrderAscIdAsc().stream()
            .map(DishCategoryResponse::from)
            .toList();
    }

    @PostMapping("/dish-categories")
    @Transactional
    @Operation(summary = "新增菜品分类", description = "商家后台维护菜单分类。code 必须唯一，用于前台分组。")
    public DishCategoryResponse createDishCategory(@Valid @RequestBody AdminDishCategoryCreateRequest request) {
        String code = request.code().trim();
        if (dishCategoryRepository.existsByCode(code)) {
            throw new ApiException(HttpStatus.CONFLICT, "分类编码已存在：" + code);
        }
        DishCategory category = new DishCategory(
            code,
            request.name().trim(),
            request.iconUrl().trim(),
            request.sortOrder(),
            request.enabled() == null || request.enabled()
        );
        return DishCategoryResponse.from(dishCategoryRepository.save(category));
    }

    @PatchMapping("/dish-categories/{id}")
    @Transactional
    @Operation(summary = "更新菜品分类", description = "商家后台维护菜单分类。可修改名称、编码、排序和启停状态。")
    public DishCategoryResponse updateDishCategory(
        @Parameter(description = "分类ID，来自后台分类列表 id", example = "2")
        @PathVariable Long id,
        @Valid @RequestBody AdminDishCategoryUpdateRequest request
    ) {
        DishCategory category = dishCategoryRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "菜品分类不存在：" + id));
        if (request.code() != null && dishCategoryRepository.existsByCodeAndIdNot(request.code().trim(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "分类编码已存在：" + request.code().trim());
        }
        category.updateForAdmin(
            request.code() == null ? null : request.code().trim(),
            request.name() == null ? null : request.name().trim(),
            request.iconUrl() == null ? null : request.iconUrl().trim(),
            request.sortOrder(),
            request.enabled()
        );
        return DishCategoryResponse.from(category);
    }

    @DeleteMapping("/dish-categories/{id}")
    @Transactional
    @Operation(summary = "删除菜品分类", description = "只能删除没有商品引用的分类。已有商品的分类请先迁移商品或停用分类。")
    public void deleteDishCategory(
        @Parameter(description = "分类ID，来自后台分类列表 id", example = "2")
        @PathVariable Long id
    ) {
        DishCategory category = dishCategoryRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "菜品分类不存在：" + id));
        long usedCount = dishRepository.countByCategoryId(id);
        if (usedCount > 0) {
            throw new ApiException(HttpStatus.CONFLICT, "该分类下还有 " + usedCount + " 个商品，不能删除，可先停用");
        }
        dishCategoryRepository.delete(category);
    }

    @GetMapping("/image-assets")
    @Operation(summary = "后台图库列表", description = "扫描本地图库根目录。目录即图库分类，图片文件即图库素材。商品编辑时可从图库选择 imageUrl。")
    public GalleryResponse listImageAssets() {
        return galleryStorageService.list();
    }

    @PostMapping("/image-folders")
    @Operation(summary = "创建图库目录", description = "在图库根目录下创建文件夹。支持多级目录，例如 products/noodles。目录名可作为图库分类。")
    public GalleryFolderResponse createImageFolder(@Valid @RequestBody CreateGalleryFolderRequest request) {
        return galleryStorageService.createFolder(request.path().trim());
    }

    @PostMapping(value = "/image-assets", consumes = "multipart/form-data")
    @Operation(summary = "上传图库图片", description = "上传图片到图库根目录或指定目录。后端保存到本地文件夹，并返回可访问 imageUrl。")
    public GalleryImageResponse uploadImageAsset(
        @Parameter(description = "图库目录。空字符串表示根目录；支持 products/noodles 这种多级目录", example = "products/noodles")
        @RequestParam(defaultValue = "") String folder,
        @Parameter(description = "图片文件。支持 jpg、jpeg、png、webp、gif")
        @RequestParam MultipartFile file
    ) {
        return galleryStorageService.upload(folder, file);
    }

    @DeleteMapping("/image-assets")
    @Operation(summary = "删除图库图片", description = "删除图库根目录下的真实图片文件。已保存到商品里的 imageUrl 不会自动清空。")
    public void deleteImageAsset(
        @Parameter(description = "图片ID，等同于图库内相对路径，来自图库列表 id", example = "products/noodles/luosifen.jpg")
        @RequestParam String id
    ) {
        galleryStorageService.deleteImage(id);
    }

    @PostMapping("/dishes")
    @Transactional
    @Operation(
        summary = "新增菜品",
        description = "商家后台新增菜品。新增后会写入数据库，前台菜单接口会按 available=true 展示上架菜品。"
    )
    public DishResponse createDish(@Valid @RequestBody AdminDishCreateRequest request) {
        DishCategory category = findEnabledCategory(request.categoryId());
        Dish dish = new Dish(
            request.name(),
            request.description(),
            request.shortDescription(),
            request.price(),
            request.imageUrl(),
            request.stock(),
            request.available() == null || request.available(),
            category
        );
        return DishResponse.from(dishRepository.save(dish));
    }

    @PatchMapping("/dishes/{id}")
    @Transactional
    @Operation(
        summary = "更新菜品价格、库存和上下架",
        description = "商家后台维护菜品。请求字段均可选；只传 price 只改价格，只传 available=false 可下架。"
    )
    public DishResponse updateDish(
        @Parameter(description = "菜品ID，来自后台菜品列表 id", example = "1")
        @PathVariable Long id,
        @Valid @RequestBody AdminDishUpdateRequest request
    ) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "菜品不存在：" + id));
        dish.updateTextForAdmin(request.name(), request.description(), request.shortDescription(), request.imageUrl());
        if (request.categoryId() != null) {
            dish.updateCategoryForAdmin(findEnabledCategory(request.categoryId()));
        }
        dish.updateForAdmin(request.price(), request.stock(), request.available());
        return DishResponse.from(dish);
    }

    @DeleteMapping("/dishes/{id}")
    @Transactional
    @Operation(summary = "删除菜品", description = "永久删除后台商品；历史订单保留当时的商品名称、价格和数量，不受影响。")
    public void deleteDish(
        @Parameter(description = "菜品ID，来自后台菜品列表 id", example = "1")
        @PathVariable Long id
    ) {
        Dish dish = dishRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "菜品不存在：" + id));
        dishRepository.delete(dish);
    }

    @GetMapping("/store-settings")
    @Operation(summary = "获取门店设置")
    public StoreSettingsResponse storeSettings() {
        return storeSettingsService.adminSettings();
    }

    @PatchMapping("/store-settings/store-open")
    @Operation(summary = "更新营业状态")
    public StoreSettingsResponse updateStoreOpen(@Valid @RequestBody UpdateStoreOpenRequest request) {
        return storeSettingsService.updateStoreOpen(request);
    }

    @PatchMapping("/store-settings")
    @Operation(summary = "更新门店资料")
    public StoreSettingsResponse updateStoreSettings(@Valid @RequestBody UpdateStoreSettingsRequest request) {
        return storeSettingsService.updateSettings(request);
    }

    @GetMapping("/home-banners")
    @Operation(summary = "获取首页轮播图")
    public List<HomeBannerItem> homeBanners() {
        return homeBannerService.adminList();
    }

    @PostMapping("/home-banners")
    @Operation(summary = "保存首页轮播图")
    public List<HomeBannerItem> replaceHomeBanners(@Valid @RequestBody ReplaceHomeBannersRequest request) {
        return homeBannerService.replaceAll(request);
    }

    private DishCategory findEnabledCategory(Long categoryId) {
        DishCategory category = dishCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "菜品分类不存在：" + categoryId));
        if (!category.isEnabled()) {
            throw new ApiException(HttpStatus.CONFLICT, "菜品分类已停用：" + category.getName());
        }
        return category;
    }
}
