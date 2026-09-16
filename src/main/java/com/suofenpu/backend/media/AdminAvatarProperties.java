package com.suofenpu.backend.media;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin-avatar")
public class AdminAvatarProperties {
    private String rootDir = "uploads/admin-avatars";
    private String publicPrefix = "/uploads/admin-avatars";
    private long maxFileBytes = 5L * 1024 * 1024;

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

    public long getMaxFileBytes() {
        return maxFileBytes;
    }

    public void setMaxFileBytes(long maxFileBytes) {
        this.maxFileBytes = maxFileBytes;
    }
}
