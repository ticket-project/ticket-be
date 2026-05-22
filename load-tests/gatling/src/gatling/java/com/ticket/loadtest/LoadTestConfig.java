package com.ticket.loadtest;

import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.OpenInjectionStep;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public final class LoadTestConfig {
    private static final AtomicInteger LOGIN_COUNTER = new AtomicInteger(intProperty("loginStartIndex", 1));
    private static final AtomicInteger TOKEN_COUNTER = new AtomicInteger();
    private static final AtomicInteger QUEUE_TOKEN_COUNTER = new AtomicInteger();
    private static final AtomicLong SYNTHETIC_MEMBER_COUNTER = new AtomicLong(longProperty("syntheticMemberStartId", 1L));

    private LoadTestConfig() {
    }

    public static String baseUrl() {
        return property("baseUrl", "http://localhost:8080");
    }

    public static String performanceId() {
        return property("performanceId", "1");
    }

    public static int statusPolls() {
        return intProperty("statusPolls", 3);
    }

    public static int statusPollPauseSeconds() {
        return intProperty("statusPollPauseSeconds", 1);
    }

    public static OpenInjectionStep injection() {
        final int users = intProperty("users", 10);
        final int durationSeconds = intProperty("durationSeconds", 10);
        final String mode = property("injectionMode", "ramp-users").toLowerCase(Locale.ROOT);
        return switch (mode) {
            case "at-once-users" -> atOnceUsers(users);
            case "constant-users-per-sec" -> constantUsersPerSec(doubleProperty("usersPerSecond", 1.0))
                    .during(Duration.ofSeconds(durationSeconds));
            case "ramp-users-per-sec" -> rampUsersPerSec(doubleProperty("usersPerSecond", 1.0))
                    .to(doubleProperty("targetUsersPerSecond", 10.0))
                    .during(Duration.ofSeconds(durationSeconds));
            default -> rampUsers(users).during(Duration.ofSeconds(durationSeconds));
        };
    }

    public static ChainBuilder initializeSession() {
        return exec(session -> session
                .set("performanceId", performanceId())
                .set("seatIdsJson", seatIdsJsonArray()));
    }

    public static ChainBuilder authenticate() {
        final String mode = property("accessTokenMode", "login").toLowerCase(Locale.ROOT);
        if ("tokens".equals(mode)) {
            return exec(session -> session.set("accessToken", nextFromCsv(property("accessTokens", ""), TOKEN_COUNTER)));
        }
        if ("synthetic-jwt".equals(mode)) {
            return exec(session -> session.set("accessToken", createSyntheticJwt(SYNTHETIC_MEMBER_COUNTER.getAndIncrement())));
        }
        return exec(session -> {
            final int loginIndex = LOGIN_COUNTER.getAndIncrement();
            final String email = property("loginEmailPrefix", "loadtest")
                    + loginIndex + "@" + property("loginEmailDomain", "test.com");
            return session.set("loginEmail", email)
                    .set("loginPassword", property("loginPassword", "password1234"));
        }).exec(http("login")
                .post("/api/v1/auth/login")
                .body(StringBody("""
                        {
                          "email": "#{loginEmail}",
                          "password": "#{loginPassword}"
                        }
                        """))
                .check(status().is(200))
                .check(io.gatling.javaapi.core.CoreDsl.jsonPath("$.result").is("SUCCESS"))
                .check(io.gatling.javaapi.core.CoreDsl.jsonPath("$.data.accessToken").saveAs("accessToken")));
    }

    public static ChainBuilder withConfiguredQueueToken() {
        return exec(session -> session.set("queueToken", nextFromCsv(property("queueTokens", ""), QUEUE_TOKEN_COUNTER)));
    }

    public static Map<CharSequence, String> authHeaders() {
        return Map.of("Authorization", "Bearer #{accessToken}");
    }

    public static Map<CharSequence, String> authAndQueueHeaders() {
        return Map.of(
                "Authorization", "Bearer #{accessToken}",
                "X-Queue-Token", "#{queueToken}"
        );
    }

    private static String property(final String key, final String defaultValue) {
        final String value = System.getProperty(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private static int intProperty(final String key, final int defaultValue) {
        return Integer.parseInt(property(key, String.valueOf(defaultValue)));
    }

    private static long longProperty(final String key, final long defaultValue) {
        return Long.parseLong(property(key, String.valueOf(defaultValue)));
    }

    private static double doubleProperty(final String key, final double defaultValue) {
        return Double.parseDouble(property(key, String.valueOf(defaultValue)));
    }

    private static String nextFromCsv(final String csv, final AtomicInteger counter) {
        final String[] values = Stream.of(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toArray(String[]::new);
        if (values.length == 0) {
            return "";
        }
        return values[Math.floorMod(counter.getAndIncrement(), values.length)];
    }

    private static String seatIdsJsonArray() {
        return Stream.of(property("seatIds", "1").split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.joining(",", "[", "]"));
    }

    private static String createSyntheticJwt(final long memberId) {
        try {
            final Instant now = Instant.now();
            final long issuedAt = now.getEpochSecond();
            final long expiresAt = issuedAt + intProperty("syntheticTokenTtlSeconds", 3600);
            final String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            final String payload = "{\"iss\":\"" + escapeJson(property("jwtIssuer", "ticket"))
                    + "\",\"sub\":\"" + memberId
                    + "\",\"role\":\"" + escapeJson(property("syntheticJwtRole", "MEMBER"))
                    + "\",\"iat\":" + issuedAt
                    + ",\"exp\":" + expiresAt + "}";
            final String unsignedToken = base64Url(header.getBytes(StandardCharsets.UTF_8))
                    + "." + base64Url(payload.getBytes(StandardCharsets.UTF_8));
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(property("jwtSecret", "0123456789abcdef0123456789abcdef")
                    .getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return unsignedToken + "." + base64Url(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create synthetic JWT", exception);
        }
    }

    private static String base64Url(final byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String escapeJson(final String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}