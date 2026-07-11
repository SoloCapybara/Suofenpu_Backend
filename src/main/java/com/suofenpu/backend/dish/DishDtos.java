package com.suofenpu.backend.dish;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public final class DishDtos {
    private DishDtos() {
    }

    @Schema(description = "菜品信息")
    public record DishResponse(
        @Schema(description = "菜品ID，下单时传给 items[].dishId", example = "1")
        Long id,
        @Schema(description = "菜品名称", example = "经典螺蛳粉")
        String name,
        @Schema(description = "菜品详情描述，用于菜单列表详情", example = "卤水、锅烧、酸豆角、花生、葱花，默认微辣。")
        String description,
        @Schema(description = "短描述，用于首页推荐卡片", example = "传统配方，经典美味")
        String shortDescription,
        @Schema(description = "基础价格。规格加价由后端根据 spec 重新计算", example = "16.80")
        BigDecimal price,
        @Schema(description = "菜品图片地址，当前可直接给小程序展示", example = "/assets/banner-1.jpg")
        String imageUrl,
        @Schema(description = "剩余库存。下单成功后后端事务扣减", example = "100")
        int stock,
        @Schema(description = "是否上架。true=前台可购买，false=后台保留但前台不展示", example = "true")
        boolean available,
        @Schema(description = "菜品分类ID，后台新增/编辑商品时选择分类", example = "2")
        Long categoryId,
        @Schema(description = "菜品分类编码，前端可用来分组。枚举：noodles=螺蛳粉，snacks=特色小吃，drinks=饮品甜品，toppings=加料配菜，combo=套餐组合", example = "noodles")
        String categoryCode,
        @Schema(description = "菜品分类名称", example = "螺蛳粉")
        String categoryName,
        @Schema(description = "分类排序值，越小越靠前", example = "2")
        int categorySortOrder
    ) {
        public static DishResponse from(Dish dish) {
            DishCategory category = dish.getCategory();
            return new DishResponse(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getShortDescription(),
                dish.getPrice(),
                dish.getImageUrl(),
                dish.getStock(),
                dish.isAvailable(),
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getSortOrder()
            );
        }
    }

    @Schema(description = "菜品分类信息")
    public record DishCategoryResponse(
        @Schema(description = "分类ID，商品新增/编辑时传 categoryId", example = "2")
        Long id,
        @Schema(description = "分类编码。枚举：noodles=螺蛳粉，snacks=特色小吃，drinks=饮品甜品，toppings=加料配菜，combo=套餐组合", example = "noodles")
        String code,
        @Schema(description = "分类名称", example = "螺蛳粉")
        String name,
        @Schema(description = "分类图标地址，供小程序菜单左侧分类展示", example = "/assets/icons/lucide/bowl.svg")
        String iconUrl,
        @Schema(description = "排序值，越小越靠前", example = "2")
        int sortOrder,
        @Schema(description = "是否启用。true=前台展示该分类；false=后台保留但前台不展示", example = "true")
        boolean enabled
    ) {
        public static DishCategoryResponse from(DishCategory category) {
            return new DishCategoryResponse(
                category.getId(),
                category.getCode(),
                category.getName(),
                category.getIconUrl(),
                category.getSortOrder(),
                category.isEnabled()
            );
        }
    }

    @Schema(description = "后台更新菜品请求。字段均可选，只传需要修改的字段")
    public record AdminDishUpdateRequest(
        @Schema(description = "菜品名称。不传表示不修改", example = "经典螺蛳粉")
        String name,
        @Schema(description = "菜品详情描述。不传表示不修改", example = "卤水、锅烧、酸豆角、花生、葱花。")
        String description,
        @Schema(description = "短描述。不传表示不修改", example = "传统配方，经典美味")
        String shortDescription,
        @Schema(description = "菜品图片地址。不传表示不修改", example = "/assets/banner-1.jpg")
        String imageUrl,
        @Schema(description = "菜品分类ID。不传表示不修改；来自 GET /api/admin/dish-categories", example = "2")
        Long categoryId,
        @Schema(description = "新价格。不传表示不修改；必须大于0", example = "18.80")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        BigDecimal price,
        @Schema(description = "新库存。不传表示不修改；0 表示售罄", example = "50")
        @Min(value = 0, message = "库存不能小于0")
        Integer stock,
        @Schema(description = "是否上架。不传表示不修改；true=上架，false=下架", example = "true")
        Boolean available
    ) {
    }

    @Schema(description = "后台新增菜品分类请求")
    public record AdminDishCategoryCreateRequest(
        @Schema(description = "分类编码。只能包含小写字母、数字和中划线；小程序可用来分组", example = "noodles", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "分类编码不能为空")
        @Pattern(regexp = "^[a-z0-9-]{2,40}$", message = "分类编码只能包含小写字母、数字和中划线，长度2-40")
        String code,
        @Schema(description = "分类名称", example = "螺蛳粉", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "分类名称不能为空")
        String name,
        @Schema(description = "分类图标地址", example = "/assets/icons/lucide/bowl.svg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "分类图标不能为空")
        String iconUrl,
        @Schema(description = "排序值，越小越靠前", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "排序值不能为空")
        @Min(value = 0, message = "排序值不能小于0")
        Integer sortOrder,
        @Schema(description = "是否启用。true=前台展示该分类；false=后台保留但前台不展示", example = "true")
        Boolean enabled
    ) {
    }

    @Schema(description = "后台更新菜品分类请求。字段均可选，只传需要修改的字段")
    public record AdminDishCategoryUpdateRequest(
        @Schema(description = "分类编码。不传表示不修改；只能包含小写字母、数字和中划线", example = "snacks")
        @Pattern(regexp = "^[a-z0-9-]{2,40}$", message = "分类编码只能包含小写字母、数字和中划线，长度2-40")
        String code,
        @Schema(description = "分类名称。不传表示不修改", example = "特色小吃")
        String name,
        @Schema(description = "分类图标地址。不传表示不修改", example = "/assets/icons/lucide/sparkle.svg")
        String iconUrl,
        @Schema(description = "排序值。不传表示不修改；越小越靠前", example = "2")
        @Min(value = 0, message = "排序值不能小于0")
        Integer sortOrder,
        @Schema(description = "是否启用。不传表示不修改；true=启用，false=停用", example = "true")
        Boolean enabled
    ) {
    }

    @Schema(description = "后台新增菜品请求")
    public record AdminDishCreateRequest(
        @Schema(description = "菜品名称", example = "酸辣螺蛳粉", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "菜品名称不能为空")
        String name,
        @Schema(description = "菜品详情描述", example = "酸笋、花生、木耳、腐竹，酸辣开胃", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "菜品详情不能为空")
        String description,
        @Schema(description = "短描述，用于卡片展示", example = "酸辣开胃，人气单品", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "短描述不能为空")
        String shortDescription,
        @Schema(description = "菜品图片地址", example = "/assets/banner-1.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "商品图片不能为空")
        String imageUrl,
        @Schema(description = "菜品分类ID，来自 GET /api/admin/dish-categories", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "菜品分类不能为空")
        Long categoryId,
        @Schema(description = "价格，必须大于0", example = "18.80", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "价格不能为空")
        @DecimalMin(value = "0.01", message = "价格必须大于0")
        BigDecimal price,
        @Schema(description = "库存，不能小于0", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "库存不能为空")
        @Min(value = 0, message = "库存不能小于0")
        Integer stock,
        @Schema(description = "是否上架。true=立即上架；false=保存为下架", example = "true")
        Boolean available
    ) {
    }
}
