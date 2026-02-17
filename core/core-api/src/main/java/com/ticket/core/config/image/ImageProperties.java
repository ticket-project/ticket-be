package com.ticket.core.config.image;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.image")
public class ImageProperties {

    private String publicPath = "/images";
    private String storagePath = "./storage/images";
    private Duration cacheMaxAge = Duration.ofDays(7);

    public String getPublicPath() {
        return publicPath;
    }

    public void setPublicPath(final String publicPath) {
        this.publicPath = publicPath;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(final String storagePath) {
        this.storagePath = storagePath;
    }

    public Duration getCacheMaxAge() {
        return cacheMaxAge;
    }

    public void setCacheMaxAge(final Duration cacheMaxAge) {
        this.cacheMaxAge = cacheMaxAge;
    }
}
