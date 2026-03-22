package com.ticket.core.config.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class SocialLoginBaseUrlResolverTest {

    private final SocialLoginBaseUrlResolver resolver = new SocialLoginBaseUrlResolver(
            "http://localhost:8080/",
            "https://oneticket.site/",
            "https://api.oneticket.site/"
    );

    @Test
    void origin이_로컬호스트면_로컬_소셜로그인_base_url을_반환한다() {
        String result = resolver.resolve("http://localhost:3000", null, "https://api.oneticket.site");

        assertThat(result).isEqualTo("http://localhost:8080");
    }

    @Test
    void referer가_로컬호스트면_로컬_소셜로그인_base_url을_반환한다() {
        String result = resolver.resolve(null, "http://127.0.0.1:5173/login", "https://api.oneticket.site");

        assertThat(result).isEqualTo("http://localhost:8080");
    }

    @Test
    void origin이_로컬호스트가_아니면_운영_소셜로그인_base_url을_반환한다() {
        String result = resolver.resolve("https://www.oneticket.site", null, "https://api.oneticket.site");

        assertThat(result).isEqualTo("https://oneticket.site");
    }

    @Test
    void origin과_referer가_없으면_요청_base_url을_반환한다() {
        String result = resolver.resolve(null, null, "https://api.oneticket.site/");

        assertThat(result).isEqualTo("https://api.oneticket.site");
    }
}
