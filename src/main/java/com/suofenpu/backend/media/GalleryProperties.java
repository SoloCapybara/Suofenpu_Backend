package com.suofenpu.backend.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gallery")
public class GalleryProperties {
    private String rootDir = "uploads/gallery";
    private String publicPrefix = "/uploads/gallery";
    private long maxStorageBytes = 5L * 1024 * 1024 * 1024;
    private long maxFileBytes = 20L * 1024 * 1024;

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public String getPublicPrefix() {
        return publicPrefix;
    }

    public void setPublicPrefix(String publicPrefix) {
        this.publicPrefix = publicPrefix;
    }

    public long getMaxStorageBytes() {
        return maxStorageBytes;
    }

    public void setMaxStorageBytes(long maxStorageBytes) {
        this.maxStorageBytes = maxStorageBytes;
    }

    public long getMaxFileBytes() {
        return maxFileBytes;
    }

    public void setMaxFileBytes(long maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
    }
}
