-- ==========================
-- 카테고리 생성
-- ==========================
INSERT INTO CATEGORYS (id, parent_id, name, code) VALUES (1, null, '콘서트', 'CONCERT');
INSERT INTO CATEGORYS (id, parent_id, name, code) VALUES (2, null, '연극', 'THEATER');

-- ==========================
-- 콘서트 공연 생성 (40개)
-- ==========================

-- K-POP 그룹 (1~15)
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (1, 'BTS 월드투어', 'Yet To Come in Seoul', 'BTS 데뷔 10주년 기념 콘서트', '2026-03-01', '2026-03-03', 150000, 'ACTIVE', 'EXCLUSIVE', '2026-02-01', '2026-03-01', 'https://example.com/bts.jpg', 'SEOUL', '잠실올림픽주경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (2, 'BLACKPINK 콘서트', 'Born Pink World Tour', '블랙핑크 월드투어 서울 공연', '2026-04-15', '2026-04-17', 120000, 'ACTIVE', 'GENERAL', '2026-03-15', '2026-04-15', 'https://example.com/bp.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (3, 'SEVENTEEN 콘서트', 'Follow Again', '세븐틴 앙코르 콘서트', '2026-01-28', '2026-01-30', 95000, 'ACTIVE', 'GENERAL', '2025-12-28', '2026-01-28', 'https://example.com/svt.jpg', 'INCHEON', '인천 인스파이어 아레나', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (4, 'aespa 콘서트', 'SYNK: Parallel Line', '에스파 첫 단독 콘서트', '2026-06-01', '2026-06-02', 78000, 'ACTIVE', 'GENERAL', '2026-05-01', '2026-06-01', 'https://example.com/aespa.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (5, 'Stray Kids 콘서트', 'MANIAC', '스트레이 키즈 월드투어', '2026-02-20', '2026-02-22', 92000, 'ACTIVE', 'EXCLUSIVE', '2026-01-20', '2026-02-20', 'https://example.com/skz.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (6, 'TWICE 콘서트', 'Ready To Be', '트와이스 앙코르', '2026-05-15', '2026-05-17', 88000, 'ACTIVE', 'GENERAL', '2026-04-15', '2026-05-15', 'https://example.com/twice.jpg', 'SEOUL', '잠실올림픽주경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (7, 'NCT 127 콘서트', 'NEO CITY', 'NCT 127 단독 콘서트', '2026-03-10', '2026-03-12', 75000, 'ACTIVE', 'GENERAL', '2026-02-10', '2026-03-10', 'https://example.com/nct127.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (8, 'NewJeans 팬미팅', 'Bunnies Day', '뉴진스 첫 팬미팅', '2026-05-10', '2026-05-11', 45000, 'ACTIVE', 'EXCLUSIVE', '2026-04-10', '2026-05-10', 'https://example.com/nj.jpg', 'SEOUL', '블루스퀘어 신한카드홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (9, 'ITZY 콘서트', 'CHECKMATE', '있지 단독 콘서트', '2026-04-05', '2026-04-06', 52000, 'ACTIVE', 'GENERAL', '2026-03-05', '2026-04-05', 'https://example.com/itzy.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (10, 'LE SSERAFIM 콘서트', 'FLAME RISES', '르세라핌 첫 단독 콘서트', '2026-07-20', '2026-07-21', 48000, 'ACTIVE', 'GENERAL', '2026-06-20', '2026-07-20', 'https://example.com/lsrf.jpg', 'SEOUL', '블루스퀘어 신한카드홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (11, 'EXO 콘서트', 'EXO PLANET', '엑소 완전체 콘서트', '2026-08-01', '2026-08-03', 115000, 'ACTIVE', 'EXCLUSIVE', '2026-07-01', '2026-08-01', 'https://example.com/exo.jpg', 'SEOUL', '잠실올림픽주경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (12, 'SHINee 콘서트', 'SHINee World', '샤이니 데뷔 18주년', '2026-05-25', '2026-05-27', 82000, 'ACTIVE', 'GENERAL', '2026-04-25', '2026-05-25', 'https://example.com/shinee.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (13, 'Red Velvet 콘서트', 'R to V', '레드벨벳 단독 콘서트', '2026-06-15', '2026-06-16', 58000, 'ACTIVE', 'GENERAL', '2026-05-15', '2026-06-15', 'https://example.com/rv.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (14, 'TXT 콘서트', 'ACT: PROMISE', '투모로우바이투게더 월드투어', '2026-03-25', '2026-03-27', 67000, 'ACTIVE', 'GENERAL', '2026-02-25', '2026-03-25', 'https://example.com/txt.jpg', 'INCHEON', '인천 인스파이어 아레나', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (15, 'ENHYPEN 콘서트', 'FATE', '엔하이픈 단독 콘서트', '2026-04-20', '2026-04-21', 55000, 'ACTIVE', 'GENERAL', '2026-03-20', '2026-04-20', 'https://example.com/enhypen.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');

-- K-POP 솔로 (16~25)
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (16, 'IU 콘서트', 'The Golden Hour', '아이유 단독 콘서트', '2026-02-14', '2026-02-16', 85000, 'ACTIVE', 'EXCLUSIVE', '2026-01-14', '2026-02-14', 'https://example.com/iu.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (17, '태연 콘서트', 'The ODDNESS', '태연 솔로 콘서트', '2026-01-25', '2026-01-26', 42000, 'ACTIVE', 'GENERAL', '2025-12-25', '2026-01-25', 'https://example.com/taeyeon.jpg', 'SEOUL', '블루스퀘어 신한카드홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (18, '정국 솔로 콘서트', 'GOLDEN', '정국 첫 솔로 월드투어', '2026-07-01', '2026-07-03', 135000, 'ACTIVE', 'EXCLUSIVE', '2026-06-01', '2026-07-01', 'https://example.com/jk.jpg', 'SEOUL', '잠실올림픽주경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (19, '지민 솔로 콘서트', 'FACE', '지민 솔로 앙코르', '2026-06-20', '2026-06-22', 98000, 'ACTIVE', 'GENERAL', '2026-05-20', '2026-06-20', 'https://example.com/jimin.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (20, '백현 콘서트', 'DOOR', '백현 솔로 콘서트', '2026-05-01', '2026-05-02', 63000, 'ACTIVE', 'GENERAL', '2026-04-01', '2026-05-01', 'https://example.com/baekhyun.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (21, '카이 콘서트', 'Rover', '카이 첫 솔로 콘서트', '2026-04-10', '2026-04-11', 38000, 'ACTIVE', 'GENERAL', '2026-03-10', '2026-04-10', 'https://example.com/kai.jpg', 'SEOUL', '블루스퀘어 신한카드홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (22, '수지 팬미팅', 'With', '수지 데뷔 15주년', '2026-03-15', '2026-03-15', 15000, 'ACTIVE', 'EXCLUSIVE', '2026-02-15', '2026-03-15', 'https://example.com/suzy.jpg', 'SEOUL', 'YES24 라이브홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (23, '청하 콘서트', 'Querencia', '청하 솔로 콘서트', '2026-02-01', '2026-02-02', 28000, 'ACTIVE', 'GENERAL', '2026-01-01', '2026-02-01', 'https://example.com/chungha.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (24, '선미 콘서트', 'LALALAY', '선미 단독 콘서트', '2026-08-10', '2026-08-11', 32000, 'ACTIVE', 'GENERAL', '2026-07-10', '2026-08-10', 'https://example.com/sunmi.jpg', 'SEOUL', '블루스퀘어 신한카드홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (25, '화사 콘서트', 'Maria', '화사 솔로 콘서트', '2026-06-05', '2026-06-06', 25000, 'ACTIVE', 'GENERAL', '2026-05-05', '2026-06-05', 'https://example.com/hwasa.jpg', 'SEOUL', 'YES24 라이브홀', '2026-01-01 10:00:00');

-- 해외 아티스트 (26~35)
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (26, 'Coldplay 내한공연', 'Music of the Spheres', '콜드플레이 첫 내한', '2026-04-25', '2026-04-26', 110000, 'ACTIVE', 'EXCLUSIVE', '2026-03-25', '2026-04-25', 'https://example.com/coldplay.jpg', 'SEOUL', '잠실올림픽주경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (27, 'Ed Sheeran 내한공연', 'Mathematics Tour', '에드 시런 아시아 투어', '2026-03-20', '2026-03-21', 88000, 'ACTIVE', 'GENERAL', '2026-02-20', '2026-03-20', 'https://example.com/ed.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (28, 'Taylor Swift 내한공연', 'Eras Tour', '테일러 스위프트 첫 내한', '2026-09-10', '2026-09-12', 180000, 'ACTIVE', 'EXCLUSIVE', '2026-08-10', '2026-09-10', 'https://example.com/taylor.jpg', 'SEOUL', '잠실올림픽주경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (29, 'Bruno Mars 내한공연', '24K Magic', '브루노 마스 콘서트', '2026-05-30', '2026-05-31', 95000, 'ACTIVE', 'GENERAL', '2026-04-30', '2026-05-30', 'https://example.com/bruno.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (30, 'Billie Eilish 내한공연', 'Happier Than Ever', '빌리 아일리시 첫 내한', '2026-07-15', '2026-07-16', 72000, 'ACTIVE', 'GENERAL', '2026-06-15', '2026-07-15', 'https://example.com/billie.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (31, 'The Weeknd 내한공연', 'After Hours', '더 위켄드 아시아 투어', '2026-08-20', '2026-08-21', 85000, 'ACTIVE', 'GENERAL', '2026-07-20', '2026-08-20', 'https://example.com/weeknd.jpg', 'SEOUL', '잠실올림픽주경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (32, 'Post Malone 내한공연', 'Twelve Carat', '포스트 말론 콘서트', '2026-06-25', '2026-06-26', 68000, 'ACTIVE', 'EXCLUSIVE', '2026-05-25', '2026-06-25', 'https://example.com/post.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (33, 'Harry Styles 내한공연', 'Love On Tour', '해리 스타일스 첫 내한', '2026-04-01', '2026-04-02', 78000, 'ACTIVE', 'GENERAL', '2026-03-01', '2026-04-01', 'https://example.com/harry.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (34, 'Doja Cat 내한공연', 'Scarlet Tour', '도자 캣 아시아 투어', '2026-09-05', '2026-09-06', 42000, 'ACTIVE', 'GENERAL', '2026-08-05', '2026-09-05', 'https://example.com/doja.jpg', 'SEOUL', '블루스퀘어 신한카드홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (35, 'Maroon 5 내한공연', 'Jordi World Tour', '마룬5 내한 콘서트', '2026-10-01', '2026-10-02', 75000, 'ACTIVE', 'EXCLUSIVE', '2026-09-01', '2026-10-01', 'https://example.com/maroon5.jpg', 'SEOUL', '고척스카이돔', '2026-01-01 10:00:00');

-- 국내 밴드/힙합 (36~40)
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (36, '잔나비 콘서트', 'PANORAMA', '잔나비 전국투어 서울', '2026-02-28', '2026-03-01', 45000, 'ACTIVE', 'GENERAL', '2026-01-28', '2026-02-28', 'https://example.com/jannabi.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (37, 'HYUKOH 콘서트', '연결의 밤', '혁오 단독 콘서트', '2026-03-08', '2026-03-09', 35000, 'ACTIVE', 'GENERAL', '2026-02-08', '2026-03-08', 'https://example.com/hyukoh.jpg', 'SEOUL', '블루스퀘어 신한카드홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (38, '넬 콘서트', 'Healing', '넬 데뷔 25주년', '2026-04-12', '2026-04-13', 22000, 'ACTIVE', 'GENERAL', '2026-03-12', '2026-04-12', 'https://example.com/nell.jpg', 'SEOUL', 'YES24 라이브홀', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (39, '박재범 콘서트', 'AOMG TOUR', '박재범 솔로 콘서트', '2026-05-20', '2026-05-21', 38000, 'ACTIVE', 'EXCLUSIVE', '2026-04-20', '2026-05-20', 'https://example.com/jaypark.jpg', 'SEOUL', '올림픽공원 체조경기장', '2026-01-01 10:00:00');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, status, sale_type, sale_start_date, sale_end_date, image, region, venue, created_at)
VALUES (40, 'pH-1 & 그루비룸', 'H1GHR MUSIC SHOW', '하이어뮤직 콘서트', '2026-06-10', '2026-06-11', 18000, 'ACTIVE', 'GENERAL', '2026-05-10', '2026-06-10', 'https://example.com/ph1.jpg', 'SEOUL', 'YES24 라이브홀', '2026-01-01 10:00:00');


-- ==========================
-- 공연-카테고리 매핑 (모두 콘서트)
-- ==========================
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (1, 1, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (2, 2, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (3, 3, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (4, 4, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (5, 5, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (6, 6, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (7, 7, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (8, 8, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (9, 9, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (10, 10, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (11, 11, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (12, 12, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (13, 13, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (14, 14, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (15, 15, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (16, 16, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (17, 17, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (18, 18, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (19, 19, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (20, 20, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (21, 21, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (22, 22, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (23, 23, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (24, 24, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (25, 25, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (26, 26, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (27, 27, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (28, 28, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (29, 29, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (30, 30, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (31, 31, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (32, 32, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (33, 33, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (34, 34, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (35, 35, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (36, 36, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (37, 37, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (38, 38, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (39, 39, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (40, 40, 1);
INSERT INTO SHOW_CATEGORYS (id, show_id, category_id) VALUES (41, 3, 2);


-- 기본 좌석(행/열) 생성
-- ==========================
INSERT INTO SEAT (id, seat_row, seat_col, created_at)
VALUES (1, 'A', '1', '2026-01-01 10:00:00');
INSERT INTO SEAT (id, seat_row, seat_col, created_at)
VALUES (2, 'A', '2', '2026-01-01 10:00:00');
INSERT INTO SEAT (id, seat_row, seat_col, created_at)
VALUES (3, 'A', '3', '2026-01-01 10:00:00');
INSERT INTO SEAT (id, seat_row, seat_col, created_at)
VALUES (4, 'B', '1', '2026-01-01 10:00:00');
INSERT INTO SEAT (id, seat_row, seat_col, created_at)
VALUES (5, 'B', '2', '2026-01-01 10:00:00');
INSERT INTO SEAT (id, seat_row, seat_col, created_at)
VALUES (6, 'B', '3', '2026-01-01 10:00:00');

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
                         hold_time,
                         created_at)
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
        300,
        '2026-01-01 10:00:00');

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
                         hold_time,
                         created_at)
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
        300,
        '2026-01-01 10:00:00');

-- ==========================
-- 회차 1번(performance_id = 1)의 좌석 상태
-- A1, A2는 예매 가능 / A3는 이미 예매된 상태로 가정
-- ==========================
INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state, created_at)
VALUES (1, 1, 1, 'AVAILABLE', '2026-01-01 10:00:00'); -- A1

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state, created_at)
VALUES (2, 1, 2, 'AVAILABLE', '2026-01-01 10:00:00'); -- A2

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state, created_at)
VALUES (3, 1, 3, 'AVAILABLE', '2026-01-01 10:00:00');
-- A3 (이미 예매됨)

-- 회차 2번(performance_id = 2)도 몇 개 넣어보자
INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state, created_at)
VALUES (4, 2, 1, 'AVAILABLE', '2026-01-01 10:00:00'); -- A1

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state, created_at)
VALUES (5, 2, 2, 'AVAILABLE', '2026-01-01 10:00:00'); -- A2

INSERT INTO PERFORMANCE_SEAT (id, performance_id, seat_id, state, created_at)
VALUES (6, 2, 3, 'AVAILABLE', '2026-01-01 10:00:00'); -- A3
