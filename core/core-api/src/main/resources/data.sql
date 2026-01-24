
INSERT INTO CATEGORYS (id, parent_id, name, depth)
VALUES (1, null, '콘서트', 1);

-- ==========================
-- 기본 공연 생성
-- ==========================
INSERT INTO SHOWS (id, title, status, view_count)
VALUES (1, '공연1', 'ACTIVE', 1);
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (2, '공연2', 'ACTIVE', 2, '2026-03-02');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (3, '공연3', 'ACTIVE', 3, '2026-03-03');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (4, '공연4', 'ACTIVE', 4, '2026-03-04');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (5, '공연5', 'ACTIVE', 5, '2026-03-05');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (6, '공연6', 'ACTIVE', 6, '2026-03-06');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (7, '공연7', 'ACTIVE', 7, '2026-03-07');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (8, '공연8', 'ACTIVE', 8, '2026-03-08');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (9, '공연9', 'ACTIVE', 9, '2026-03-09');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (10, '공연10', 'ACTIVE', 10, '2026-03-10');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (11, '공연11', 'ACTIVE', 11, '2026-03-11');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (12, '공연12', 'ACTIVE', 12, '2026-03-12');
INSERT INTO SHOWS (id, title, status, view_count, start_date)
VALUES (13, '공연13', 'ACTIVE', 13, '2026-03-13');


INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (1, 1, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (2, 2, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (3, 3, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (4, 4, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (5, 5, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (6, 6, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (7, 7, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (8, 8, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (9, 9, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (10, 10, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (11, 11, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id)
VALUES (12, 12, 1);


-- 기본 좌석(행/열) 생성
-- ==========================
INSERT INTO SEAT (id, seat_row, seat_col)
VALUES (1, 'A', '1');
INSERT INTO SEAT (id, seat_row, seat_col)
VALUES (2, 'A', '2');
INSERT INTO SEAT (id, seat_row, seat_col)
VALUES (3, 'A', '3');
INSERT INTO SEAT (id, seat_row, seat_col)
VALUES (4, 'B', '1');
INSERT INTO SEAT (id, seat_row, seat_col)
VALUES (5, 'B', '2');
INSERT INTO SEAT (id, seat_row, seat_col)
VALUES (6, 'B', '3');

-- ==========================
-- 공연 회차(Performance) 샘플 2개
-- show_id 는 그냥 100 번으로 가정
-- ==========================

INSERT INTO PERFORMANCE (id,
                         show_id,
                         round_no,
                         start_time,
                         end_time,
                         order_open_time,
                         order_close_time,
                         state,
                         status,
                         max_can_hold_count,
                         hold_time)
VALUES (1,
        1,
        1,
        '2025-12-24 19:00:00',
        '2025-12-24 21:30:00',
        '2025-12-01 10:00:00',
        '2025-12-24 18:00:00',
        'OPEN',
        'ACTIVE',
        4,
        300);

INSERT INTO PERFORMANCE (id,
                         show_id,
                         round_no,
                         start_time,
                         end_time,
                         order_open_time,
                         order_close_time,
                         state,
                         status,
                         max_can_hold_count,
                         hold_time)
VALUES (2,
        1,
        1,
        '2025-12-25 14:00:00',
        '2025-12-25 16:30:00',
        '2025-12-01 10:00:00',
        '2025-12-25 13:00:00',
        'OPEN',
        'ACTIVE',
        4,
        300);

-- ==========================
-- 회차 1번(performance_id = 1)의 좌석 상태
-- A1, A2는 예매 가능 / A3는 이미 예매된 상태로 가정
-- ==========================
INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state)
VALUES (1, 1, 1, 'AVAILABLE'); -- A1

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state)
VALUES (2, 1, 2, 'AVAILABLE'); -- A2

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state)
VALUES (3, 1, 3, 'AVAILABLE');
-- A3 (이미 예매됨)

-- 회차 2번(performance_id = 2)도 몇 개 넣어보자
INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state)
VALUES (4, 2, 1, 'AVAILABLE'); -- A1

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state)
VALUES (5, 2, 2, 'AVAILABLE'); -- A2

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state)
VALUES (6, 2, 3, 'AVAILABLE'); -- A3
