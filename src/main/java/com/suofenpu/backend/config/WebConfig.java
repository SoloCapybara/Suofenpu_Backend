package com.suofenpu.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.suofenpu.backend.media.GalleryProperties;
import com.suofenpu.backend.media.GalleryStorageService;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final GalleryStorageService galleryStorageService;
    private final GalleryProperties galleryProperties;

    public WebConfig(GalleryStorageService galleryStorageService, GalleryProperties galleryProperties) {
        this.galleryStorageService = galleryStorageService;
        this.galleryProperties = galleryProperties;
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
    }
}
