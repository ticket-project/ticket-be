package com.ticket.core.support.image;

import com.ticket.core.config.image.ImageProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ImageUrlResolver {

    private final ImageProperties imageProperties;

    public ImageUrlResolver(final ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
    }

    public String resolve(final String imagePath) {
        if (!StringUtils.hasText(imagePath)) {
            return imagePath;
        }

        final String trimmed = imagePath.trim();
        if (isHttpUrl(trimmed)) {
            return trimmed;
        }

        final String normalizedRelativePath = normalizeRelativePath(trimmed);
        if (!StringUtils.hasText(normalizedRelativePath)) {
            return null;
        }

        return normalizePublicPath(imageProperties.getPublicPath()) + "/" + normalizedRelativePath;
    }

    private boolean isHttpUrl(final String value) {
        try {
            final URI uri = URI.create(value);
            if (!uri.isAbsolute() || uri.getScheme() == null) {
                return false;
            }
            final String scheme = uri.getScheme().toLowerCase();
            return "http".equals(scheme) || "https".equals(scheme);
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private String normalizeRelativePath(final String value) {
        final String slashUnified = value.replace('\\', '/');
        final String withoutLeadingSlash = slashUnified.replaceFirst("^/+", "");
        if (!StringUtils.hasText(withoutLeadingSlash)) {
            return null;
        }
        if (withoutLeadingSlash.contains(":")) {
            return null;
        }

        final Path normalizedPath = Paths.get(withoutLeadingSlash).normalize();
        if (normalizedPath.isAbsolute() || normalizedPath.startsWith("..")) {
            return null;
        }

        final String normalized = normalizedPath.toString().replace('\\', '/');
        return StringUtils.hasText(normalized) ? normalized : null;
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
}
