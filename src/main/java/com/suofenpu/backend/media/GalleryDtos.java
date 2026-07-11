package com.suofenpu.backend.media;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class GalleryDtos {
    private GalleryDtos() {
    }

    @Schema(description = "图库目录信息")
    public record GalleryFolderResponse(
        @Schema(description = "目录路径。空字符串代表图库根目录", example = "products/noodles")
        String path,
        @Schema(description = "目录名称。根目录返回“全部图片”", example = "noodles")
        String name,
        @Schema(description = "该目录下的直接图片数量", example = "4")
        long imageCount
    ) {
    }

    @Schema(description = "图库图片信息")
    public record GalleryImageResponse(
        @Schema(description = "图片ID，等同于图库内相对文件路径，删除时传该值", example = "products/noodles/luosifen.jpg")
        String id,
        @Schema(description = "所在目录。空字符串代表图库根目录", example = "products/noodles")
        String folder,
        @Schema(description = "图片名称，不含扩展名", example = "luosifen")
        String name,
        @Schema(description = "文件名", example = "luosifen.jpg")
        String fileName,
        @Schema(description = "可访问图片地址，商品保存时写入 dish.imageUrl", example = "/uploads/gallery/products/noodles/luosifen.jpg")
        String imageUrl,
        @Schema(description = "文件大小，单位 byte", example = "128000")
        long size,
        @Schema(description = "图片宽度，单位像素。无法识别时为空", example = "1200")
        Integer width,
        @Schema(description = "图片高度，单位像素。无法识别时为空", example = "900")
        Integer height,
        @Schema(description = "最后修改时间", example = "2026-07-09T08:00:00Z")
        Instant updatedAt
    ) {
    }

    @Schema(description = "图库列表响应")
    public record GalleryResponse(
        @Schema(description = "图库根目录在服务器上的绝对路径", example = "/Users/xxx/project/backend/uploads/gallery")
        String rootDir,
        @Schema(description = "图片访问前缀", example = "/uploads/gallery")
        String publicPrefix,
        @Schema(description = "图库容量上限，单位 byte。默认 5GB", example = "5368709120")
        long maxStorageBytes,
        @Schema(description = "已使用图库容量，单位 byte", example = "2630667468")
        long usedStorageBytes,
        @Schema(description = "剩余图库容量，单位 byte", example = "2738041652")
        long remainingStorageBytes,
        @Schema(description = "单张图片最大大小，单位 byte。默认 20MB", example = "20971520")
        long maxFileBytes,
        @Schema(description = "图库目录列表")
        List<GalleryFolderResponse> folders,
        @Schema(description = "图库图片列表")
        List<GalleryImageResponse> images
    ) {
    }

    @Schema(description = "创建图库目录请求")
    public record CreateGalleryFolderRequest(
        @Schema(description = "目录路径。支持多级目录，例如 products/noodles。不能包含 .. 或绝对路径", example = "products/noodles")
        @NotBlank(message = "目录路径不能为空")
        String path
    ) {
    }
}
