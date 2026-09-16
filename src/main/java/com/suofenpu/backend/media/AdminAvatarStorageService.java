package com.suofenpu.backend.media;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.suofenpu.backend.common.ApiException;

@Service
public class AdminAvatarStorageService {
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");

    private final Path rootDir;
    private final String publicPrefix;
    private final long maxFileBytes;

    public AdminAvatarStorageService(AdminAvatarProperties properties) {
        this.rootDir = Path.of(properties.getRootDir()).toAbsolutePath().normalize();
        this.publicPrefix = stripTrailingSlash(properties.getPublicPrefix());
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

    public String upload(Long userId, MultipartFile file) {
        if (userId == null || userId <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "用户ID无效");
        }
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请选择要上传的头像图片");
        }
        String originalFilename = file.getOriginalFilename() == null ? "avatar" : file.getOriginalFilename();
        String extension = extensionOf(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "只支持 jpg、jpeg、png、webp 头像图片");
        }
        if (file.getSize() > maxFileBytes) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "头像图片不能超过 " + readableSize(maxFileBytes));
        }

        Path userDir = rootDir.resolve(String.valueOf(userId)).normalize();
        ensureInsideRoot(userDir);
        try {
            deleteDirectoryContents(userDir);
            Files.createDirectories(userDir);
            String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
            Path target = userDir.resolve(filename).normalize();
            ensureInsideRoot(target);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            }
            String relative = rootDir.relativize(target).toString().replace('\\', '/');
            return publicPrefix + "/" + relative;
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "上传头像失败：" + exception.getMessage());
        }
    }

    public void deleteByUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) return;
        String prefix = publicPrefix + "/";
        if (!avatarUrl.startsWith(prefix)) return;
        String relative = avatarUrl.substring(prefix.length());
        Path target = rootDir.resolve(relative).normalize();
        ensureInsideRoot(target);
        try {
            if (Files.isRegularFile(target)) {
                Files.delete(target);
            }
            Path parent = target.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                deleteDirectoryContents(parent);
                Files.deleteIfExists(parent);
            }
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "删除头像失败：" + exception.getMessage());
        }
    }

    private void deleteDirectoryContents(Path directory) throws IOException {
        if (!Files.exists(directory)) return;
        try (Stream<Path> stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder())
                .filter(path -> !path.equals(directory))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
        }
    }

    private void ensureRootDir() {
        try {
            Files.createDirectories(rootDir);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "创建头像目录失败：" + exception.getMessage());
        }
    }

    private void ensureInsideRoot(Path path) {
        if (!path.toAbsolutePath().normalize().startsWith(rootDir)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "头像路径不合法");
        }
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String stripTrailingSlash(String value) {
        if (value == null || value.isBlank()) return "";
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String readableSize(long bytes) {
        if (bytes >= 1024 * 1024) return (bytes / (1024 * 1024)) + "MB";
        return (bytes / 1024) + "KB";
    }
}
