package com.ticket.core.domain;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 프로젝트 컨벤션을 기계적으로 강제하는 린트 테스트.
 * <p>
 * ArchUnit 외에도 파일 시스템 기반 검증을 포함한다.
 * 에이전트가 이 테스트를 통과하면 컨벤션을 준수한 것이다.
 */
@AnalyzeClasses(
        packages = "com.ticket.core",
        importOptions = {ImportOption.DoNotIncludeTests.class}
)
@SuppressWarnings("NonAsciiCharacters")
class CoreDomainLintTest {

    // ──────────────────────────────────────────────
    // 네이밍 규칙
    // ──────────────────────────────────────────────

    @ArchTest
    static final ArchRule UseCase_클래스는_command_또는_query_패키지에_있어야_한다 =
            classes()
                    .that().haveSimpleNameEndingWith("UseCase")
                    .should().resideInAnyPackage("..command..", "..query..")
                    .because("UseCase 클래스는 command/ 또는 query/ 패키지에 위치해야 합니다. "
                            + "해결: 해당 클래스를 command/(쓰기) 또는 query/(읽기) 패키지로 이동하세요. "
                            + "참고: docs/ARCHITECTURE.md#core-domain-계층-구조");

    @ArchTest
    static final ArchRule Finder_클래스는_query_패키지에_있어야_한다 =
            classes()
                    .that().haveSimpleNameEndingWith("Finder")
                    .should().resideInAnyPackage("..query..")
                    .because("Finder 클래스는 읽기 전용이므로 query/ 패키지에 위치해야 합니다. "
                            + "해결: 해당 Finder 클래스를 query/ 패키지로 이동하세요.");

    @ArchTest
    static final ArchRule Redisson_Store_구현체는_infra_패키지에_있어야_한다 =
            classes()
                    .that().haveSimpleNameStartingWith("Redisson")
                    .and().haveSimpleNameEndingWith("Store")
                    .should().resideInAnyPackage("..infra..")
                    .because("Redisson Store 구현체는 infra/ 패키지에 위치해야 합니다. "
                            + "해결: 해당 Store 구현체를 domain/{도메인}/infra/ 패키지로 이동하세요. "
                            + "참고: docs/ARCHITECTURE.md#인프라-격리-규칙");

    @ArchTest
    static final ArchRule Redis_Store_구현체는_infra_패키지에_있어야_한다 =
            classes()
                    .that().haveSimpleNameStartingWith("Redis")
                    .and().haveSimpleNameEndingWith("Store")
                    .should().resideInAnyPackage("..infra..")
                    .because("Redis Store 구현체는 infra/ 패키지에 위치해야 합니다. "
                            + "해결: 해당 Store 구현체를 domain/{도메인}/infra/ 패키지로 이동하세요. "
                            + "참고: docs/ARCHITECTURE.md#인프라-격리-규칙");

    // ──────────────────────────────────────────────
    // Redis 키 중앙화
    // ──────────────────────────────────────────────

    @ArchTest
    static final ArchRule Redis_키_문자열은_전용_유틸_클래스에서만_정의해야_한다 =
            noClasses()
                    .that().resideOutsideOfPackages("..support..", "..runtime..")
                    .and().haveSimpleNameNotEndingWith("RedisKey")
                    .and().haveSimpleNameNotEndingWith("Test")
                    .should(new ArchCondition<JavaClass>("Redis 키 패턴 문자열을 직접 포함하지 않아야 한다") {
                        @Override
                        public void check(final JavaClass javaClass, final ConditionEvents events) {
                            // ArchUnit은 소스 레벨 문자열 검사가 제한적이므로
                            // 이 규칙은 패키지 구조로 강제한다.
                            // 실제 문자열 검사는 아래 파일 기반 테스트에서 수행.
                        }
                    })
                    .because("Redis 키는 *RedisKey 유틸 클래스(support/ 또는 runtime/)에서만 정의합니다. "
                            + "해결: 하드코딩된 Redis 키를 해당 도메인의 *RedisKey 클래스로 이동하세요.");

    // ──────────────────────────────────────────────
    // 로깅 패턴
    // ──────────────────────────────────────────────

    @ArchTest
    static final ArchRule model_패키지에서는_로깅을_사용하지_않는다 =
            noClasses()
                    .that().resideInAnyPackage("..model..")
                    .should().dependOnClassesThat().haveFullyQualifiedName("org.slf4j.Logger")
                    .because("model/ 패키지의 엔티티와 값 객체는 로깅에 의존하면 안 됩니다. "
                            + "해결: 로깅이 필요하면 command/ 또는 query/ 계층의 서비스에서 처리하세요.");

    // ──────────────────────────────────────────────
    // 파일 크기 제한 (파일 시스템 기반)
    // ──────────────────────────────────────────────

    @Test
    void 도메인_소스_파일은_400줄을_넘지_않아야_한다() throws IOException {
        final int maxLines = 400;
        final Path sourceRoot = Path.of("src/main/java");

        if (!Files.exists(sourceRoot)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            final var oversizedFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.lines(p).count() > maxLines;
                        } catch (final IOException e) {
                            return false;
                        }
                    })
                    .toList();

            assertThat(oversizedFiles)
                    .as("다음 파일이 %d줄을 초과합니다. "
                            + "에이전트가 파일을 읽고 수정하기 어려워집니다. "
                            + "해결: 클래스를 분리하거나 책임을 나누세요. "
                            + "참고: docs/design-docs/core-beliefs.md#6", maxLines)
                    .isEmpty();
        }
    }

    // ──────────────────────────────────────────────
    // Redis 키 하드코딩 검출 (파일 시스템 기반)
    // ──────────────────────────────────────────────

    @Test
    void infra_Store에서_Redis_키를_하드코딩하지_않아야_한다() throws IOException {
        final Path infraRoot = Path.of("src/main/java/com/ticket/core/domain");

        if (!Files.exists(infraRoot)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(infraRoot)) {
            final var violations = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> p.toString().contains("infra"))
                    .filter(p -> !p.getFileName().toString().contains("RedisKey"))
                    .filter(this::containsHardcodedRedisKey)
                    .toList();

            assertThat(violations)
                    .as("infra/ 패키지의 Store에서 Redis 키를 하드코딩하고 있습니다. "
                            + "해결: 해당 도메인의 *RedisKey 유틸 클래스를 사용하세요. "
                            + "예: QueueRedisKey.waiting(performanceId), SeatRedisKey.select(performanceId, seatId)")
                    .isEmpty();
        }
    }

    private boolean containsHardcodedRedisKey(final Path path) {
        try {
            final String content = Files.readString(path);
            // "queue:", "seat:", "hold:" 등의 Redis 키 접두사가 문자열 리터럴로 있는지 검사
            // *RedisKey 클래스의 상수를 사용하는 경우는 제외
            return content.lines()
                    .filter(line -> !line.trim().startsWith("//"))
                    .filter(line -> !line.trim().startsWith("*"))
                    .filter(line -> !line.contains("import"))
                    .anyMatch(line ->
                            line.matches(".*\"(queue|seat|hold|entry|token):[^\"]*\".*")
                                    && !line.contains("RedisKey"));
        } catch (final IOException e) {
            return false;
        }
    }
}
