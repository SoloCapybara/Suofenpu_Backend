package com.suofenpu.backend.media;

import static com.suofenpu.backend.media.GalleryDtos.GalleryFolderResponse;
import static com.suofenpu.backend.media.GalleryDtos.GalleryImageResponse;
import static com.suofenpu.backend.media.GalleryDtos.GalleryResponse;

import java.io.IOException;
import java.io.InputStream;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.suofenpu.backend.common.ApiException;

@Service
public class GalleryStorageService {
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp", "gif");

    private final Path rootDir;
    private final String publicPrefix;
    private final long maxStorageBytes;
    private final long maxFileBytes;

    public GalleryStorageService(GalleryProperties properties) {
        this.rootDir = Path.of(properties.getRootDir()).toAbsolutePath().normalize();
        this.publicPrefix = stripTrailingSlash(properties.getPublicPrefix());
        this.maxStorageBytes = properties.getMaxStorageBytes();
        this.maxFileBytes = properties.getMaxFileBytes();
        ensureRootDir();
    }

    public Path rootDir() {
        ensureRootDir();
        return rootDir;
    }

    public String publicPrefix() {
        return publicPrefix;
    }

    public GalleryResponse list() {
        ensureRootDir();
        try {
            List<GalleryFolderResponse> folders = listFolders();
            List<GalleryImageResponse> images = listImages();
            long usedStorageBytes = images.stream().mapToLong(GalleryImageResponse::size).sum();
            long remainingStorageBytes = Math.max(0, maxStorageBytes - usedStorageBytes);
            return new GalleryResponse(
                rootDir.toString(),
                publicPrefix,
                maxStorageBytes,
                usedStorageBytes,
                remainingStorageBytes,
                maxFileBytes,
                folders,
                images
            );
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "读取图库失败：" + exception.getMessage());
        }
    }

    public GalleryFolderResponse createFolder(String folder) {
        Path folderPath = resolveFolder(folder);
        try {
            Files.createDirectories(folderPath);
            return toFolderResponse(folderPath);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "创建图库目录失败：" + exception.getMessage());
        }
    }

    public GalleryImageResponse upload(String folder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请选择要上传的图片");
        }
        String originalFilename = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "只支持 jpg、jpeg、png、webp、gif 图片");
        }
        if (file.getSize() > maxFileBytes) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "单张图片不能超过 " + readableSize(maxFileBytes));
        }

        Path folderPath = resolveFolder(folder == null ? "" : folder);
        try {
            Files.createDirectories(folderPath);
            long usedStorageBytes = usedStorageBytes();
            if (usedStorageBytes + file.getSize() > maxStorageBytes) {
                throw new ApiException(HttpStatus.CONFLICT, "图库容量不足，当前最多还能上传 " + readableSize(Math.max(0, maxStorageBytes - usedStorageBytes)));
            }
            String filename = uniqueFilename(originalFilename, extension);
            Path target = folderPath.resolve(filename).normalize();
            ensureInsideRoot(target);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return toImageResponse(target);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "上传图片失败：" + exception.getMessage());
        }
    }

    public void deleteImage(String imageId) {
        Path imagePath = resolveImage(imageId);
        try {
            Files.delete(imagePath);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "删除图片失败：" + exception.getMessage());
        }
    }

    private List<GalleryFolderResponse> listFolders() throws IOException {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            List<GalleryFolderResponse> folders = stream
                .filter(Files::isDirectory)
                .map(this::toFolderResponse)
                .sorted(Comparator.comparing(GalleryFolderResponse::path))
                .toList();
            return folders;
        }
    }

    private List<GalleryImageResponse> listImages() throws IOException {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(this::isAllowedImage)
                .map(this::toImageResponse)
                .sorted(Comparator.comparing(GalleryImageResponse::folder).thenComparing(GalleryImageResponse::fileName))
                .toList();
        }
    }

    private GalleryFolderResponse toFolderResponse(Path folderPath) {
        Path relative = rootDir.relativize(folderPath);
        String path = normalizeRelative(relative);
        String name = path.isBlank() ? "全部图片" : folderPath.getFileName().toString();
        long imageCount = 0;
        try (Stream<Path> stream = Files.list(folderPath)) {
            imageCount = stream.filter(Files::isRegularFile).filter(this::isAllowedImage).count();
        } catch (IOException ignored) {
            imageCount = 0;
        }
        return new GalleryFolderResponse(path, name, imageCount);
    }

    private GalleryImageResponse toImageResponse(Path imagePath) {
        try {
            Path relativePath = rootDir.relativize(imagePath);
            String id = normalizeRelative(relativePath);
            String folder = normalizeRelative(relativePath.getParent());
            String fileName = imagePath.getFileName().toString();
            ImageSize imageSize = imageSize(imagePath);
            return new GalleryImageResponse(
                id,
                folder,
                stripExtension(fileName),
                fileName,
                publicPrefix + "/" + id,
                Files.size(imagePath),
                imageSize.width(),
                imageSize.height(),
                Files.getLastModifiedTime(imagePath).toInstant()
            );
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "读取图片信息失败：" + exception.getMessage());
        }
    }

    private Path resolveFolder(String folder) {
        Path path = folder == null || folder.isBlank() ? rootDir : rootDir.resolve(folder).normalize();
        ensureInsideRoot(path);
        return path;
    }

    private Path resolveImage(String imageId) {
        if (imageId == null || imageId.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "图片ID不能为空");
        }
        Path path = rootDir.resolve(imageId).normalize();
        ensureInsideRoot(path);
        if (!Files.isRegularFile(path) || !isAllowedImage(path)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "图库图片不存在：" + imageId);
        }
        return path;
    }

    private void ensureRootDir() {
        try {
            Files.createDirectories(rootDir);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "创建图库根目录失败：" + exception.getMessage());
        }
    }

    private void ensureInsideRoot(Path path) {
        if (!path.toAbsolutePath().normalize().startsWith(rootDir)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "目录路径不合法");
        }
    }

    private boolean isAllowedImage(Path path) {
        return ALLOWED_EXTENSIONS.contains(extensionOf(path.getFileName().toString()));
    }

    private long usedStorageBytes() throws IOException {
        try (Stream<Path> stream = Files.walk(rootDir)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(this::isAllowedImage)
                .mapToLong(this::safeSize)
                .sum();
        }
    }

    private long safeSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException ignored) {
            return 0;
        }
    }

    private ImageSize imageSize(Path imagePath) {
        try {
            BufferedImage image = ImageIO.read(imagePath.toFile());
            if (image == null) {
                return new ImageSize(null, null);
            }
            return new ImageSize(image.getWidth(), image.getHeight());
        } catch (IOException ignored) {
            return new ImageSize(null, null);
        }
    }

    private String readableSize(long bytes) {
        if (bytes < 1024) {
            return bytes + "B";
        }
        if (bytes < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1fKB", bytes / 1024.0);
        }
        if (bytes < 1024L * 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1fMB", bytes / 1024.0 / 1024.0);
        }
        return String.format(Locale.ROOT, "%.2fGB", bytes / 1024.0 / 1024.0 / 1024.0);
    }

    private String uniqueFilename(String originalFilename, String extension) {
        String baseName = stripExtension(originalFilename);
        String normalized = Normalizer.normalize(baseName, Normalizer.Form.NFKD)
            .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5_-]+", "-")
            .replaceAll("(^-+|-+$)", "");
        if (normalized.isBlank()) {
            normalized = "image";
        }
        return normalized + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + extension;
    }

    private String extensionOf(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String stripExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index <= 0) {
            return filename;
        }
        return filename.substring(0, index);
    }

    private String normalizeRelative(Path path) {
        if (path == null) {
            return "";
        }
        String value = path.toString().replace('\\', '/');
        return ".".equals(value) ? "" : value;
    }

    private String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "/uploads/gallery";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private record ImageSize(Integer width, Integer height) {
    }
}
