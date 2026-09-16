package com.suofenpu.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.suofenpu.backend.media.AdminAvatarProperties;
import com.suofenpu.backend.media.AdminAvatarStorageService;
import com.suofenpu.backend.media.GalleryProperties;
import com.suofenpu.backend.media.GalleryStorageService;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final GalleryStorageService galleryStorageService;
    private final GalleryProperties galleryProperties;
    private final AdminAvatarStorageService adminAvatarStorageService;
    private final AdminAvatarProperties adminAvatarProperties;

    public WebConfig(
        GalleryStorageService galleryStorageService,
        GalleryProperties galleryProperties,
        AdminAvatarStorageService adminAvatarStorageService,
        AdminAvatarProperties adminAvatarProperties
    ) {
        this.galleryStorageService = galleryStorageService;
        this.galleryProperties = galleryProperties;
        this.adminAvatarStorageService = adminAvatarStorageService;
        this.adminAvatarProperties = adminAvatarProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*")
            .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = galleryProperties.getPublicPrefix();
        String pattern = (prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix) + "/**";
        registry.addResourceHandler(pattern)
            .addResourceLocations(galleryStorageService.rootDir().toUri().toString());

        String avatarPrefix = adminAvatarProperties.getPublicPrefix();
        String avatarPattern = (avatarPrefix.endsWith("/") ? avatarPrefix.substring(0, avatarPrefix.length() - 1) : avatarPrefix) + "/**";
        registry.addResourceHandler(avatarPattern)
            .addResourceLocations(adminAvatarStorageService.rootDir().toUri().toString());
    }
}
