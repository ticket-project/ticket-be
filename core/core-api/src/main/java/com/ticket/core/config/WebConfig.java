package com.ticket.core.config;

import com.ticket.core.config.image.ImageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.util.StringUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@EnableConfigurationProperties(ImageProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final ImageProperties imageProperties;

    public WebConfig(final ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
    }

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        final String normalizedPublicPath = normalizePublicPath(imageProperties.getPublicPath());
        final String imageLocation = toFileResourceLocation(imageProperties.getStoragePath());

        registry.addResourceHandler(normalizedPublicPath + "/**")
                .addResourceLocations(imageLocation)
                .setCacheControl(CacheControl.maxAge(imageProperties.getCacheMaxAge()).cachePublic())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }

    private String normalizePublicPath(final String publicPath) {
        if (!StringUtils.hasText(publicPath)) {
            return "/images";
        }

        String normalized = publicPath.trim().replace('\\', '/');
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String toFileResourceLocation(final String storagePath) {
        final String path = StringUtils.hasText(storagePath) ? storagePath.trim() : "./storage/images";
        final Path absolutePath = resolveStoragePath(path);
        final String uri = absolutePath.toUri().toString();
        return uri.endsWith("/") ? uri : uri + "/";
    }

    private Path resolveStoragePath(final String configuredPath) {
        final Path path = Paths.get(configuredPath);
        if (path.isAbsolute()) {
            return path.normalize();
        }

        final Path current = Paths.get("").toAbsolutePath().normalize();
        final Path directCandidate = current.resolve(path).normalize();
        if (Files.isDirectory(directCandidate)) {
            return directCandidate;
        }

        for (Path cursor = current.getParent(); cursor != null; cursor = cursor.getParent()) {
            final Path candidate = cursor.resolve(path).normalize();
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }

        return directCandidate;
    }
}
