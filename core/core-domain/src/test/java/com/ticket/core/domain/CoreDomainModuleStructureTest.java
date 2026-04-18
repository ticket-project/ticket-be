package com.ticket.core.domain;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class CoreDomainModuleStructureTest {

    @Test
    void core_domain_should_own_order_and_queue_business_packages() {
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/order"))).isTrue();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/queue"))).isTrue();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/domain/order"))).isFalse();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/domain/queue"))).isFalse();
    }

    @Test
    void core_api_should_depend_on_core_domain() throws Exception {
        final String apiBuild = Files.readString(resolve("../core-api/build.gradle"));
        assertThat(apiBuild).contains("implementation project(':core:core-domain')");
    }

    @Test
    void core_infra_module_should_exist_and_be_used_by_core_api() throws Exception {
        final String settings = Files.readString(resolve("../../settings.gradle"));
        final String apiBuild = Files.readString(resolve("../core-api/build.gradle"));

        assertThat(settings).contains("'core:core-infra'");
        assertThat(Files.exists(resolve("../core-infra/build.gradle"))).isTrue();
        assertThat(apiBuild).contains("implementation project(':core:core-infra')");
    }

    @Test
    void core_enum_module_should_not_exist_anymore() throws Exception {
        final String settings = Files.readString(resolve("../../settings.gradle"));
        final String apiBuild = Files.readString(resolve("../core-api/build.gradle"));
        final String domainBuild = Files.readString(resolve("build.gradle"));

        assertThat(settings).doesNotContain("'core:core-enum'");
        assertThat(apiBuild).doesNotContain("project(':core:core-enum')");
        assertThat(domainBuild).doesNotContain("project(':core:core-enum')");
        assertThat(Files.exists(resolve("../core-enum"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/member/model/Role.java"))).isTrue();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/show/BookingStatus.java"))).isTrue();
    }

    @Test
    void jwt_security_should_stay_in_core_api() throws Exception {
        final String apiBuild = Files.readString(resolve("../core-api/build.gradle"));
        final String domainBuild = Files.readString(resolve("build.gradle"));

        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/config/security/JwtTokenService.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/config/security/JwtProperties.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/config/security/OAuth2EndpointConstants.java"))).isFalse();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/config/security/JwtTokenService.java"))).isTrue();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/config/security/JwtProperties.java"))).isTrue();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/config/security/OAuth2EndpointConstants.java"))).isTrue();
        assertThat(apiBuild).contains("io.jsonwebtoken:jjwt-api:0.12.6");
        assertThat(domainBuild).doesNotContain("io.jsonwebtoken:jjwt-api:0.12.6");
    }

    @Test
    void core_domain_should_not_depend_on_swagger() throws Exception {
        final String domainBuild = Files.readString(resolve("build.gradle"));

        assertThat(domainBuild).doesNotContain("springdoc-openapi");
        assertThat(findSwaggerImports(resolve("src/main/java"))).isEmpty();
    }

    @Test
    void core_domain_should_not_keep_websocket_or_p6spy_runtime_adapters() throws Exception {
        final String domainBuild = Files.readString(resolve("build.gradle"));

        assertThat(domainBuild).doesNotContain("spring-boot-starter-websocket");
        assertThat(domainBuild).doesNotContain("p6spy-spring-boot-starter");
    }

    @Test
    void response_files_should_not_exist_in_core_domain() {
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/AuthLoginResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/OrderDetailResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/PerformanceScheduleListResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/SeatAvailabilityResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/SeatStatusResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/GenreResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/MetaCodesResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowDetailResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowLikeSummaryResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowOpeningSoonDetailResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowSearchResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowSeatResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowSummaryResponse.java"))).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowOpeningSoonSummaryResponse.java"))).isFalse();
    }

    @Test
    void http_cookie_utility_should_stay_in_core_api() {
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/support/util/CookieUtils.java"))).isFalse();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/support/util/CookieUtils.java"))).isTrue();
    }

    private List<Path> findSwaggerImports(final Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(this::containsSwaggerImport)
                    .toList();
        }
    }

    private boolean containsSwaggerImport(final Path path) {
        try {
            final String content = Files.readString(path);
            return content.contains("io.swagger.v3.oas.annotations")
                    || content.contains("org.springdoc");
        } catch (final IOException exception) {
            throw new IllegalStateException("파일을 읽을 수 없습니다: " + path, exception);
        }
    }

    private Path resolve(final String relativePath) {
        return Path.of(relativePath).normalize();
    }
}
