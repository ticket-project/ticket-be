package com.ticket.core.domain;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.ticket.core",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
@SuppressWarnings("NonAsciiCharacters")
class CoreDomainArchitectureTest {

    @ArchTest
    static final ArchRule infra_패키지_밖에서는_redisson에_직접_의존하지_않는다 =
            noClasses()
                    .that().resideOutsideOfPackages("..infra..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.redisson..")
                    .because("Redisson은 인프라 구현 세부사항입니다. "
                            + "도메인/서비스 코드에서 직접 사용하지 마세요. "
                            + "해결: ..infra.. 패키지에 Redis 연동 클래스를 만들고, "
                            + "도메인 인터페이스를 통해 접근하세요. "
                            + "참고: docs/ARCHITECTURE.md#인프라-격리-규칙");

    @ArchTest
    static final ArchRule infra_밖에서는_spring_data_redis를_직접_참조할_수_없다 =
            noClasses()
                    .that().resideOutsideOfPackages("..infra..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework.data.redis..")
                    .because("Spring Data Redis는 인프라 계층에서만 사용해야 합니다. "
                            + "해결: RedisTemplate, StringRedisTemplate 등의 사용을 "
                            + "..infra.. 패키지의 Store/Repository 클래스로 옮기세요. "
                            + "참고: docs/ARCHITECTURE.md#인프라-격리-규칙");

    @ArchTest
    static final ArchRule infra_패키지_밖에서는_websocket_messaging에_직접_의존하지_않는다 =
            noClasses()
                    .that().resideOutsideOfPackages("..infra..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework.messaging..")
                    .because("WebSocket/Messaging은 인프라 관심사입니다. "
                            + "해결: SimpMessagingTemplate 등의 사용을 ..infra.. 패키지의 "
                            + "Publisher/Broadcaster 클래스로 옮기고, "
                            + "도메인에서는 이벤트를 발행하는 방식으로 전환하세요. "
                            + "참고: docs/ARCHITECTURE.md#인프라-격리-규칙");

    @ArchTest
    static final ArchRule infra_패키지_밖에서는_http_interface_client에_직접_의존하지_않는다 =
            noClasses()
                    .that().resideOutsideOfPackages("..infra..", "com.ticket.core.config..")
                    .should().dependOnClassesThat().resideInAnyPackage("org.springframework.web.service.annotation..")
                    .because("HTTP Interface Client 어노테이션은 인프라 또는 config 패키지에서만 사용합니다. "
                            + "해결: @HttpExchange 등의 인터페이스 정의를 ..infra.. 또는 "
                            + "com.ticket.core.config.. 패키지로 이동하세요. "
                            + "참고: docs/ARCHITECTURE.md#인프라-격리-규칙");
}
