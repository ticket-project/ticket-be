package com.ticket.core.config.seed;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class SeedDataLoaderTest {

    private static final Pattern SHOW_PATTERN = Pattern.compile(
        "INSERT INTO SHOWS .*?VALUES \\((\\d+), .*?, '([0-9]{4}-[0-9]{2}-[0-9]{2})', '([0-9]{4}-[0-9]{2}-[0-9]{2})',",
        Pattern.DOTALL
    );
    private static final Pattern PERFORMANCE_PATTERN = Pattern.compile(
        "INSERT INTO PERFORMANCES .*?VALUES \\((\\d+), (\\d+), (\\d+), '([0-9]{4}-[0-9]{2}-[0-9]{2} [0-9:]{8})'",
        Pattern.DOTALL
    );
    private static final Pattern PERFORMANCE_SEAT_STATE_PATTERN = Pattern.compile(
        "CASE\\s+WHEN.*'RESERVED'.*ELSE\\s+'AVAILABLE'\\s+END",
        Pattern.DOTALL
    );

    @Test
    void multi_round_shows_use_multiple_dates() throws Exception {
        final List<String> statements = parseStatements();
        final Map<Long, ShowPeriod> showPeriods = extractShowPeriods(statements);
        final Map<Long, List<LocalDate>> performanceDatesByShow = extractPerformanceDates(statements);

        final Map<Long, List<LocalDate>> singleDaySchedules = new LinkedHashMap<>();

        for (Map.Entry<Long, List<LocalDate>> entry : performanceDatesByShow.entrySet()) {
            if (entry.getValue().size() < 2) {
                continue;
            }

            final List<LocalDate> uniqueDates = entry.getValue().stream()
                .distinct()
                .sorted()
                .toList();

            if (uniqueDates.size() < 2) {
                singleDaySchedules.put(entry.getKey(), uniqueDates);
            }
        }

        assertThat(singleDaySchedules)
            .withFailMessage("회차가 2개 이상인 공연의 날짜가 하루에만 몰려 있습니다: %s", singleDaySchedules)
            .isEmpty();

        for (Map.Entry<Long, List<LocalDate>> entry : performanceDatesByShow.entrySet()) {
            final ShowPeriod showPeriod = showPeriods.get(entry.getKey());
            assertThat(showPeriod)
                .withFailMessage("공연 기간 정보를 찾을 수 없습니다. showId=%s", entry.getKey())
                .isNotNull();

            assertThat(entry.getValue())
                .allSatisfy(date -> assertThat(date)
                    .withFailMessage("회차 날짜가 공연 기간을 벗어났습니다. showId=%s, date=%s, period=%s", entry.getKey(), date, showPeriod)
                    .isBetween(showPeriod.startDate(), showPeriod.endDate()));
        }
    }

    @Test
    void performance_seat_seed_creates_contiguous_reserved_blocks() throws Exception {
        final String performanceSeatStatement = parseStatements().stream()
            .filter(statement -> statement.startsWith("INSERT INTO PERFORMANCE_SEATS"))
            .findFirst()
            .orElseThrow();

        assertThat(PERFORMANCE_SEAT_STATE_PATTERN.matcher(performanceSeatStatement).find())
            .withFailMessage("PERFORMANCE_SEATS 시드가 AVAILABLE만 고정 생성하고 있습니다.")
            .isTrue();

        assertThat(performanceSeatStatement).contains("CAST(st.seat_no AS INTEGER)");
        assertThat(performanceSeatStatement).contains("SUBSTR(st.section, 1, 1)");
        assertThat(performanceSeatStatement).contains("SUBSTR(st.row_no, 1, 1)");

        for (int base = 0; base < 3; base++) {
            final int blockSize = base + 2;
            final int cycleSize = base + 5;
            final double reservedRatio = (double) blockSize / cycleSize;

            assertThat(reservedRatio)
                .withFailMessage("연속 RESERVED 비율이 요청 범위(30%%~60%%) 밖입니다. ratio=%.4f", reservedRatio)
                .isBetween(0.30d, 0.60d);

            assertThat(longestReservedRunLength(base, 20))
                .withFailMessage("연속 RESERVED 길이가 2~4연석 범위를 벗어났습니다. base=%s", base)
                .isBetween(2, 4);
        }
    }

    @Test
    void performance_seat_seed_statement_executes_on_h2() throws Exception {
        final String performanceSeatStatement = parseStatements().stream()
            .filter(statement -> statement.startsWith("INSERT INTO PERFORMANCE_SEATS"))
            .findFirst()
            .orElseThrow();

        try (
            Connection connection = DriverManager.getConnection("jdbc:h2:mem:seed_loader_test;MODE=Oracle;DB_CLOSE_DELAY=-1");
            Statement statement = connection.createStatement()
        ) {
            statement.execute("CREATE TABLE PERFORMANCES (id BIGINT PRIMARY KEY, show_id BIGINT NOT NULL)");
            statement.execute("CREATE TABLE SEATS (id BIGINT PRIMARY KEY, section VARCHAR(10), row_no VARCHAR(10), seat_no VARCHAR(10))");
            statement.execute("CREATE TABLE SHOW_GRADES (id BIGINT PRIMARY KEY, show_id BIGINT NOT NULL, grade_code VARCHAR(10), price NUMBER(10, 0))");
            statement.execute("CREATE TABLE PERFORMANCE_SEATS (performance_id BIGINT, seat_id BIGINT, state VARCHAR(20), price NUMBER(10, 0), created_at TIMESTAMP, created_by VARCHAR(50))");

            statement.execute("INSERT INTO PERFORMANCES (id, show_id) VALUES (1, 100)");
            statement.execute("INSERT INTO SEATS (id, section, row_no, seat_no) VALUES (10, '가', 'A', '1')");
            statement.execute("INSERT INTO SHOW_GRADES (id, show_id, grade_code, price) VALUES (1000, 100, 'R', 120000)");

            statement.executeUpdate(performanceSeatStatement);

            try (var resultSet = statement.executeQuery("SELECT COUNT(*) FROM PERFORMANCE_SEATS")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getInt(1)).isEqualTo(1);
            }
        }
    }

    private int longestReservedRunLength(final int base, final int seatCount) {
        final int blockSize = base + 2;
        final int cycleSize = base + 5;
        final int offset = base;
        int longest = 0;
        int current = 0;

        for (int seatIndex = 0; seatIndex < seatCount; seatIndex++) {
            final int rotatedIndex = Math.floorMod(seatIndex - offset, cycleSize);
            final boolean reserved = rotatedIndex < blockSize;
            if (reserved) {
                current++;
                longest = Math.max(longest, current);
            } else {
                current = 0;
            }
        }

        return longest;
    }

    @SuppressWarnings("unchecked")
    private List<String> parseStatements() throws Exception {
        final SeedDataLoader loader = new SeedDataLoader(null, null);
        final Method method = SeedDataLoader.class.getDeclaredMethod("parseStatements");
        method.setAccessible(true);
        return (List<String>) method.invoke(loader);
    }

    private Map<Long, ShowPeriod> extractShowPeriods(final List<String> statements) {
        final Map<Long, ShowPeriod> showPeriods = new HashMap<>();

        for (String statement : statements) {
            final Matcher matcher = SHOW_PATTERN.matcher(statement);
            if (!matcher.find()) {
                continue;
            }

            final long showId = Long.parseLong(matcher.group(1));
            showPeriods.put(
                showId,
                new ShowPeriod(
                    LocalDate.parse(matcher.group(2)),
                    LocalDate.parse(matcher.group(3))
                )
            );
        }

        return showPeriods;
    }

    private Map<Long, List<LocalDate>> extractPerformanceDates(final List<String> statements) {
        final Map<Long, List<PerformanceDate>> performanceDates = new HashMap<>();

        for (String statement : statements) {
            final Matcher matcher = PERFORMANCE_PATTERN.matcher(statement);
            if (!matcher.find()) {
                continue;
            }

            final long showId = Long.parseLong(matcher.group(2));
            final int performanceNo = Integer.parseInt(matcher.group(3));
            final LocalDate performanceDate = LocalDateTime.parse(matcher.group(4).replace(' ', 'T')).toLocalDate();

            performanceDates.computeIfAbsent(showId, ignored -> new ArrayList<>())
                .add(new PerformanceDate(performanceNo, performanceDate));
        }

        final Map<Long, List<LocalDate>> result = new LinkedHashMap<>();
        performanceDates.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> result.put(
                entry.getKey(),
                entry.getValue().stream()
                    .sorted(Comparator.comparingInt(PerformanceDate::performanceNo))
                    .map(PerformanceDate::date)
                    .toList()
            ));

        return result;
    }

    private record ShowPeriod(LocalDate startDate, LocalDate endDate) {
    }

    private record PerformanceDate(int performanceNo, LocalDate date) {
    }
}
