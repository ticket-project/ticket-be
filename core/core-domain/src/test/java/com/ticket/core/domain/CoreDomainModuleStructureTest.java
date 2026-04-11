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
    void core_domain_모듈은_주문과_큐_비즈니스를_소유해야_한다() {
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/order")))
                .as("order 패키지는 core-domain에 존재해야 합니다. "
                        + "주문 비즈니스 로직은 core-domain 모듈이 소유합니다. "
                        + "참고: docs/ARCHITECTURE.md#비즈니스-도메인-의존-관계")
                .isTrue();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/queue")))
                .as("queue 패키지는 core-domain에 존재해야 합니다. "
                        + "대기열 비즈니스 로직은 core-domain 모듈이 소유합니다. "
                        + "참고: docs/ARCHITECTURE.md#비즈니스-도메인-의존-관계")
                .isTrue();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/domain/order")))
                .as("order 패키지가 core-api에 존재하면 안 됩니다. "
                        + "해결: core-api/src/.../domain/order/ 디렉터리를 삭제하고, "
                        + "비즈니스 로직은 core-domain으로 이동하세요.")
                .isFalse();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/domain/queue")))
                .as("queue 패키지가 core-api에 존재하면 안 됩니다. "
                        + "해결: core-api/src/.../domain/queue/ 디렉터리를 삭제하고, "
                        + "비즈니스 로직은 core-domain으로 이동하세요.")
                .isFalse();
    }

    @Test
    void core_api는_core_domain을_의존해야_한다() throws Exception {
        final String apiBuild = Files.readString(resolve("../core-api/build.gradle"));
        assertThat(apiBuild)
                .as("core-api/build.gradle에 core-domain 의존이 있어야 합니다. "
                        + "해결: core/core-api/build.gradle의 dependencies 블록에 "
                        + "implementation project(':core:core-domain')를 추가하세요.")
                .contains("implementation project(':core:core-domain')");
    }

    @Test
    void core_enum_모듈은_제거되고_enum은_core_domain에_존재해야_한다() throws Exception {
        final String settings = Files.readString(resolve("../../settings.gradle"));
        final String apiBuild = Files.readString(resolve("../core-api/build.gradle"));
        final String domainBuild = Files.readString(resolve("build.gradle"));

        assertThat(settings)
                .as("settings.gradle에 core-enum 모듈이 남아있습니다. "
                        + "해결: settings.gradle에서 'core:core-enum' include를 제거하세요. "
                        + "enum은 core-domain에 통합되었습니다.")
                .doesNotContain("'core:core-enum'");
        assertThat(apiBuild)
                .as("core-api가 core-enum에 의존하고 있습니다. "
                        + "해결: core-api/build.gradle에서 project(':core:core-enum') 의존을 제거하세요.")
                .doesNotContain("project(':core:core-enum')");
        assertThat(domainBuild)
                .as("core-domain이 core-enum에 의존하고 있습니다. "
                        + "해결: core-domain/build.gradle에서 project(':core:core-enum') 의존을 제거하세요.")
                .doesNotContain("project(':core:core-enum')");
        assertThat(Files.exists(resolve("../core-enum")))
                .as("core-enum 디렉터리가 아직 존재합니다. 해결: core/core-enum/ 디렉터리를 삭제하세요.")
                .isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/member/model/Role.java")))
                .as("Role enum이 core-domain에 존재해야 합니다.")
                .isTrue();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/show/BookingStatus.java")))
                .as("BookingStatus enum이 core-domain에 존재해야 합니다.")
                .isTrue();
    }

    @Test
    void jwt_보안_구현은_core_api에_있고_core_domain에는_남지_않아야_한다() throws Exception {
        final String apiBuild = Files.readString(resolve("../core-api/build.gradle"));
        final String domainBuild = Files.readString(resolve("build.gradle"));

        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/config/security/JwtTokenService.java")))
                .as("JwtTokenService는 core-domain에 있으면 안 됩니다. "
                        + "해결: core-api/src/.../config/security/로 이동하세요. "
                        + "JWT는 HTTP 계층 관심사입니다. 참고: docs/ARCHITECTURE.md#core-api-소유물")
                .isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/config/security/JwtProperties.java")))
                .as("JwtProperties는 core-domain에 있으면 안 됩니다. "
                        + "해결: core-api/src/.../config/security/로 이동하세요.")
                .isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/config/security/OAuth2EndpointConstants.java")))
                .as("OAuth2EndpointConstants는 core-domain에 있으면 안 됩니다. "
                        + "해결: core-api/src/.../config/security/로 이동하세요.")
                .isFalse();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/config/security/JwtTokenService.java")))
                .as("JwtTokenService가 core-api에 존재해야 합니다.")
                .isTrue();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/config/security/JwtProperties.java")))
                .as("JwtProperties가 core-api에 존재해야 합니다.")
                .isTrue();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/config/security/OAuth2EndpointConstants.java")))
                .as("OAuth2EndpointConstants가 core-api에 존재해야 합니다.")
                .isTrue();
        assertThat(apiBuild)
                .as("core-api/build.gradle에 jjwt 의존이 있어야 합니다.")
                .contains("io.jsonwebtoken:jjwt-api:0.12.6");
        assertThat(domainBuild)
                .as("core-domain/build.gradle에 jjwt 의존이 있으면 안 됩니다. "
                        + "해결: core-domain/build.gradle에서 jjwt 관련 의존을 제거하세요. "
                        + "JWT는 core-api 모듈에서만 사용합니다.")
                .doesNotContain("io.jsonwebtoken:jjwt-api:0.12.6");
    }

    @Test
    void core_domain은_swagger_의존을_직접_가지지_않아야_한다() throws Exception {
        final String domainBuild = Files.readString(resolve("build.gradle"));

        assertThat(domainBuild)
                .as("core-domain/build.gradle에 springdoc-openapi 의존이 있으면 안 됩니다. "
                        + "해결: Swagger 의존을 core-api/build.gradle로 옮기세요. "
                        + "API 문서는 HTTP 계층 관심사입니다. 참고: docs/ARCHITECTURE.md#core-api-소유물")
                .doesNotContain("springdoc-openapi");
        assertThat(findSwaggerImports(resolve("src/main/java")))
                .as("core-domain 소스에 Swagger import가 있으면 안 됩니다. "
                        + "해결: io.swagger.v3.oas.annotations 또는 org.springdoc import를 "
                        + "core-domain 소스에서 제거하세요. Swagger 어노테이션은 core-api의 "
                        + "컨트롤러/DTO에서만 사용합니다.")
                .isEmpty();
    }

    @Test
    void 직접_Output으로_대체한_response_파일은_core_domain에_남지_않아야_한다() {
        final String fixGuide = "해결: Response DTO는 core-api 모듈로 이동하세요. "
                + "core-domain은 도메인 Output 인터페이스를 사용합니다. "
                + "참고: docs/ARCHITECTURE.md#core-api-소유물";

        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/AuthLoginResponse.java")))
                .as("AuthLoginResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/OrderDetailResponse.java")))
                .as("OrderDetailResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/PerformanceScheduleListResponse.java")))
                .as("PerformanceScheduleListResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/SeatAvailabilityResponse.java")))
                .as("SeatAvailabilityResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/SeatStatusResponse.java")))
                .as("SeatStatusResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/GenreResponse.java")))
                .as("GenreResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/MetaCodesResponse.java")))
                .as("MetaCodesResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowDetailResponse.java")))
                .as("ShowDetailResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowLikeSummaryResponse.java")))
                .as("ShowLikeSummaryResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowOpeningSoonDetailResponse.java")))
                .as("ShowOpeningSoonDetailResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowResponse.java")))
                .as("ShowResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowSearchResponse.java")))
                .as("ShowSearchResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowSeatResponse.java")))
                .as("ShowSeatResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowSummaryResponse.java")))
                .as("ShowSummaryResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/domain/response/ShowOpeningSoonSummaryResponse.java")))
                .as("ShowOpeningSoonSummaryResponse가 core-domain에 남아있습니다. " + fixGuide).isFalse();
    }

    @Test
    void http_cookie_유틸리티는_core_api에_있고_core_domain에는_없어야_한다() {
        assertThat(Files.exists(resolve("src/main/java/com/ticket/core/support/util/CookieUtils.java")))
                .as("CookieUtils가 core-domain에 있으면 안 됩니다. "
                        + "해결: core-api/src/.../support/util/로 이동하세요. "
                        + "HTTP Cookie는 API 계층 관심사입니다. 참고: docs/ARCHITECTURE.md#core-api-소유물")
                .isFalse();
        assertThat(Files.exists(resolve("../core-api/src/main/java/com/ticket/core/support/util/CookieUtils.java")))
                .as("CookieUtils가 core-api에 존재해야 합니다.")
                .isTrue();
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
