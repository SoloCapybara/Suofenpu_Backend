package com.suofenpu.backend.media;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ImageAssetDtos {
    private ImageAssetDtos() {
    }

    @Schema(description = "图库图片信息")
    public record ImageAssetResponse(
        @Schema(description = "图片ID，后台选择商品图片时使用", example = "1")
        Long id,
        @Schema(description = "图片名称，方便后台识别图片内容", example = "经典螺蛳粉主图")
        String name,
        @Schema(description = "图片地址。商品保存时会把该地址写入 dish.imageUrl", example = "/assets/banner-1.jpg")
        String imageUrl,
        @Schema(description = "排序值，越小越靠前", example = "1")
        int sortOrder,
        @Schema(description = "是否启用。true=可被后台选择；false=保留但默认不推荐使用", example = "true")
        boolean enabled,
        @Schema(description = "创建时间", example = "2026-07-09T06:00:00Z")
        Instant createdAt
    ) {
        public static ImageAssetResponse from(ImageAsset imageAsset) {
            return new ImageAssetResponse(
                imageAsset.getId(),
                imageAsset.getName(),
                imageAsset.getImageUrl(),
                imageAsset.getSortOrder(),
                imageAsset.isEnabled(),
                imageAsset.getCreatedAt()
            );
        }
    }

    @Schema(description = "后台新增图库图片请求")
    public record AdminImageAssetCreateRequest(
        @Schema(description = "图片名称，方便后台搜索和选择", example = "经典螺蛳粉主图", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "图片名称不能为空")
        String name,
        @Schema(description = "图片地址。第一版先填写已有图片 URL，例如小程序 assets 路径或网络图片地址", example = "/assets/banner-1.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "图片地址不能为空")
        String imageUrl,
        @Schema(description = "排序值，越小越靠前", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "排序值不能为空")
        @Min(value = 0, message = "排序值不能小于0")
        Integer sortOrder,
        @Schema(description = "是否启用。true=可被后台选择；false=保留但默认不推荐使用", example = "true")
        Boolean enabled
    ) {
    }

    @Schema(description = "后台更新图库图片请求。字段均可选，只传需要修改的字段")
    public record AdminImageAssetUpdateRequest(
        @Schema(description = "图片名称。不传表示不修改", example = "加辣螺蛳粉主图")
        String name,
        @Schema(description = "图片地址。不传表示不修改", example = "/assets/banner-2.jpg")
        String imageUrl,
        @Schema(description = "排序值。不传表示不修改；越小越靠前", example = "2")
        @Min(value = 0, message = "排序值不能小于0")
        Integer sortOrder,
        @Schema(description = "是否启用。不传表示不修改", example = "true")
        Boolean enabled
    ) {
    }
}
