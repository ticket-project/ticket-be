package com.ticket.core.support.image;

import com.ticket.core.config.image.ImageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImageUrlResolverTest {

    private ImageUrlResolver imageUrlResolver;

    @BeforeEach
    void setUp() {
        ImageProperties imageProperties = new ImageProperties();
        imageProperties.setPublicPath("/images/");
        imageUrlResolver = new ImageUrlResolver(imageProperties);
    }

    @Test
    void returnsAbsoluteUrlAsIs() {
        String resolved = imageUrlResolver.resolve("https://cdn.example.com/shows/poster.jpg");

        assertThat(resolved).isEqualTo("https://cdn.example.com/shows/poster.jpg");
    }

    @Test
    void convertsRelativePathToPublicImageUrl() {
        String resolved = imageUrlResolver.resolve("shows/poster.jpg");

        assertThat(resolved).isEqualTo("/images/shows/poster.jpg");
    }

    @Test
    void normalizesLeadingSlashAndWindowsSeparator() {
        String resolved = imageUrlResolver.resolve("\\shows\\poster.jpg");

        assertThat(resolved).isEqualTo("/images/shows/poster.jpg");
    }

    @Test
    void returnsNullWhenPathTraversalIsDetected() {
        String resolved = imageUrlResolver.resolve("../secret.txt");

        assertThat(resolved).isNull();
    }

    @Test
    void returnsNullWhenDriveLetterStylePathIsProvided() {
        String resolved = imageUrlResolver.resolve("C:/secret/poster.jpg");

        assertThat(resolved).isNull();
    }
}
