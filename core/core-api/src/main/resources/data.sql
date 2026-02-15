-- ==========================
-- 카테고리 생성
-- ==========================
INSERT INTO CATEGORIES (id, name, code, created_at, created_by, status) VALUES (1, '콘서트', 'CONCERT', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO CATEGORIES (id, name, code, created_at, created_by, status) VALUES (2, '연극', 'THEATER', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 장르 생성 (카테고리별)
-- ==========================
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (1, 1, 'K-POP', 'KPOP', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (2, 1, '힙합', 'HIPHOP', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (3, 1, 'R&B', 'RNB', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (4, 1, '팝', 'POP', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (5, 1, '록/메탈', 'ROCK', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (6, 1, '발라드', 'BALLAD', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (7, 1, '인디/밴드', 'INDIE', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (8, 2, '드라마', 'DRAMA', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (9, 2, '코미디', 'COMEDY', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO GENRES (id, category_id, name, code, created_at, created_by, status) VALUES (10, 2, '뮤지컬', 'MUSICAL', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 아티스트(Performer) 생성
-- ==========================
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (1, 'BTS', 'https://example.com/performers/bts.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (2, 'BLACKPINK', 'https://example.com/performers/bp.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (3, 'SEVENTEEN', 'https://example.com/performers/svt.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (4, 'aespa', 'https://example.com/performers/aespa.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (5, 'Stray Kids', 'https://example.com/performers/skz.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (6, 'TWICE', 'https://example.com/performers/twice.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (7, 'NCT 127', 'https://example.com/performers/nct127.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (8, 'NewJeans', 'https://example.com/performers/nj.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (9, 'IU', 'https://example.com/performers/iu.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (10, '태연', 'https://example.com/performers/taeyeon.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (11, 'Coldplay', 'https://example.com/performers/coldplay.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (12, 'Ed Sheeran', 'https://example.com/performers/ed.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (13, 'Taylor Swift', 'https://example.com/performers/taylor.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (14, 'Bruno Mars', 'https://example.com/performers/bruno.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (15, '잔나비', 'https://example.com/performers/jannabi.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- 추가 아티스트
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (16, 'AKMU', 'https://example.com/performers/akmu.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (17, 'DAY6', 'https://example.com/performers/day6.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (18, '10CM', 'https://example.com/performers/10cm.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (19, '검정치마', 'https://example.com/performers/blackskirt.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (20, 'HYUKOH', 'https://example.com/performers/hyukoh.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (21, '이무진', 'https://example.com/performers/leemujin.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (22, '박효신', 'https://example.com/performers/parkhyoshin.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (23, '성시경', 'https://example.com/performers/sungsikyung.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (24, '폴킴', 'https://example.com/performers/paulkim.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (25, '볼빨간사춘기', 'https://example.com/performers/bol4.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (26, 'YB', 'https://example.com/performers/yb.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (27, '자우림', 'https://example.com/performers/jaurim.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (28, '국카스텐', 'https://example.com/performers/guckkasten.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (29, '장범준', 'https://example.com/performers/jangbumjune.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (30, '백예린', 'https://example.com/performers/baekyerin.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (31, 'Charlie Puth', 'https://example.com/performers/charlieputh.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (32, 'Dua Lipa', 'https://example.com/performers/dualipa.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (33, 'Maroon 5', 'https://example.com/performers/maroon5.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (34, 'Imagine Dragons', 'https://example.com/performers/imaginedragons.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (35, 'Sam Smith', 'https://example.com/performers/samsmith.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (36, '국립극단', 'https://example.com/performers/nationaltheater.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (37, '서울시뮤지컬단', 'https://example.com/performers/seoulmusical.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (38, '극단청춘', 'https://example.com/performers/youththeater.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (39, '뮤지컬컴퍼니R', 'https://example.com/performers/musicalr.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMERS (id, name, profile_image_url, created_at, created_by, status) VALUES (40, '연우무대', 'https://example.com/performers/yeonwoostage.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 공연장(Venue) 생성
-- ==========================
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (1, '잠실올림픽주경기장', '서울특별시 송파구 올림픽로 25', 'SEOUL', 37.51536200, 127.07332200, '02-2240-8800', 'https://example.com/venues/jamsil.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (2, '고척스카이돔', '서울특별시 구로구 경인로 430', 'SEOUL', 37.49818700, 126.86715800, '02-2128-2300', 'https://example.com/venues/gocheok.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (3, '인천 인스파이어 아레나', '인천광역시 중구 공항문화로 127', 'INCHEON', 37.46011200, 126.43805900, '032-729-2000', 'https://example.com/venues/inspire.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (4, '올림픽공원 체조경기장', '서울특별시 송파구 올림픽로 424', 'SEOUL', 37.52125600, 127.12596100, '02-410-1114', 'https://example.com/venues/kspo.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (5, '블루스퀘어 신한카드홀', '서울특별시 용산구 이태원로 294', 'SEOUL', 37.54131500, 126.99724500, '02-1588-5212', 'https://example.com/venues/bluesquare.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- 추가 공연장
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (6, '부산아시아드주경기장', '부산광역시 연제구 월드컵대로 344', 'GYEONGSANG', 35.19029500, 129.05923100, '051-500-2114', 'https://example.com/venues/busan_asiad.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (7, '대구 EXCO 오디토리움', '대구광역시 북구 엑스코로 10', 'GYEONGSANG', 35.90611900, 128.61363700, '053-601-5000', 'https://example.com/venues/daegu_exco.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (8, '광주 유니버시아드체육관', '광주광역시 서구 금화로 278', 'JEOLLA', 35.13487400, 126.87522100, '062-613-8240', 'https://example.com/venues/gwangju_uni.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (9, '대전컨벤션센터 제2전시장', '대전광역시 유성구 엑스포로 107', 'CHUNGCHEONG', 36.37317000, 127.38027000, '042-250-1100', 'https://example.com/venues/dcc2.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO VENUES (id, name, address, region, latitude, longitude, phone, image_url, created_at, created_by, status) VALUES (10, '제주국제컨벤션센터', '제주특별자치도 서귀포시 중문관광로 224', 'JEJU', 33.24709200, 126.40887000, '064-735-1000', 'https://example.com/venues/icc_jeju.jpg', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 공연 생성 (1~60)
-- ==========================
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (1, 'BTS 월드투어', 'Yet To Come in Seoul', 'BTS 데뷔 10주년 기념 콘서트', '2026-03-01', '2026-03-03', 150000, 'EXCLUSIVE', '2026-02-01', '2026-03-01', 'https://example.com/bts.jpg', 1, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (2, 'BLACKPINK 콘서트', 'Born Pink World Tour', '블랙핑크 월드투어 서울 공연', '2026-04-15', '2026-04-17', 120000, 'GENERAL', '2026-03-15', '2026-04-15', 'https://example.com/bp.jpg', 2, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (3, 'SEVENTEEN 콘서트', 'Follow Again', '세븐틴 앙코르 콘서트', '2026-01-28', '2026-01-30', 95000, 'GENERAL', '2025-12-28', '2026-01-28', 'https://example.com/svt.jpg', 3, 3, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (4, 'aespa 콘서트', 'SYNK: Parallel Line', '에스파 첫 단독 콘서트', '2026-06-01', '2026-06-02', 78000, 'GENERAL', '2026-05-01', '2026-06-01', 'https://example.com/aespa.jpg', 4, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (5, 'Stray Kids 콘서트', 'MANIAC', '스트레이 키즈 월드투어', '2026-02-20', '2026-02-22', 92000, 'EXCLUSIVE', '2026-01-20', '2026-02-20', 'https://example.com/skz.jpg', 2, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (6, 'TWICE 콘서트', 'Ready To Be', '트와이스 앙코르', '2026-05-15', '2026-05-17', 88000, 'GENERAL', '2026-04-15', '2026-05-15', 'https://example.com/twice.jpg', 1, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (7, 'NCT 127 콘서트', 'NEO CITY', 'NCT 127 단독 콘서트', '2026-03-10', '2026-03-12', 75000, 'GENERAL', '2026-02-10', '2026-03-10', 'https://example.com/nct127.jpg', 4, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (8, 'NewJeans 팬미팅', 'Bunnies Day', '뉴진스 첫 팬미팅', '2026-05-10', '2026-05-11', 45000, 'EXCLUSIVE', '2026-04-10', '2026-05-10', 'https://example.com/nj.jpg', 5, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (9, 'IU 콘서트', 'The Golden Hour', '아이유 단독 콘서트', '2026-02-14', '2026-02-16', 85000, 'EXCLUSIVE', '2026-01-14', '2026-02-14', 'https://example.com/iu.jpg', 4, 9, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (10, '태연 콘서트', 'The ODDNESS', '태연 솔로 콘서트', '2026-01-25', '2026-01-26', 42000, 'GENERAL', '2025-12-25', '2026-01-25', 'https://example.com/taeyeon.jpg', 5, 10, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (11, 'Coldplay 내한공연', 'Music of the Spheres', '콜드플레이 첫 내한', '2026-04-25', '2026-04-26', 110000, 'EXCLUSIVE', '2026-03-25', '2026-04-25', 'https://example.com/coldplay.jpg', 1, 11, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (12, 'Ed Sheeran 내한공연', 'Mathematics Tour', '에드 시런 아시아 투어', '2026-03-20', '2026-03-21', 88000, 'GENERAL', '2026-02-20', '2026-03-20', 'https://example.com/ed.jpg', 2, 12, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (13, 'Taylor Swift 내한공연', 'Eras Tour', '테일러 스위프트 첫 내한', '2026-09-10', '2026-09-12', 180000, 'EXCLUSIVE', '2026-08-10', '2026-09-10', 'https://example.com/taylor.jpg', 1, 13, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (14, 'Bruno Mars 내한공연', '24K Magic', '브루노 마스 콘서트', '2026-05-30', '2026-05-31', 95000, 'GENERAL', '2026-04-30', '2026-05-30', 'https://example.com/bruno.jpg', 2, 14, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status)
VALUES (15, '잔나비 콘서트', 'PANORAMA', '잔나비 전국투어 서울', '2026-02-28', '2026-03-01', 45000, 'GENERAL', '2026-01-28', '2026-02-28', 'https://example.com/jannabi.jpg', 4, 15, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- 추가 SHOWS (16~60)
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (16, 'AKMU 전국투어 서울', '항해 앵콜', '밴드 라이브 기반 전국투어 서울 공연', '2026-04-04', '2026-04-05', 68000, 'GENERAL', '2026-03-04', '2026-04-04', 'https://example.com/shows/akmu_seoul.jpg', 4, 16, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (17, 'DAY6 콘서트', 'Welcome to the Show', 'DAY6 라이브 밴드 콘서트', '2026-04-18', '2026-04-19', 74000, 'EXCLUSIVE', '2026-03-18', '2026-04-18', 'https://example.com/shows/day6_show.jpg', 2, 17, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (18, '10CM 소극장 콘서트', '너에게 닿기를', '감성 어쿠스틱 중심 소극장 공연', '2026-04-25', '2026-04-27', 39000, 'GENERAL', '2026-03-25', '2026-04-25', 'https://example.com/shows/10cm_smallhall.jpg', 5, 18, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (19, '검정치마 투어', 'Teen Troubles Live', '인디 록 사운드 단독 투어', '2026-05-02', '2026-05-03', 47000, 'GENERAL', '2026-04-02', '2026-05-02', 'https://example.com/shows/blackskirt_tour.jpg', 4, 19, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (20, 'HYUKOH 단독공연', 'AAA Live', '하이브리드 록 사운드 공연', '2026-05-09', '2026-05-10', 52000, 'GENERAL', '2026-04-09', '2026-05-09', 'https://example.com/shows/hyukoh_live.jpg', 4, 20, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (21, '이무진 전국투어', '별책부록', '보컬 중심 라이브 투어', '2026-05-16', '2026-05-17', 56000, 'GENERAL', '2026-04-16', '2026-05-16', 'https://example.com/shows/leemujin_tour.jpg', 8, 21, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (22, '박효신 콘서트', '겨울소리', '오케스트라 협연 스페셜 공연', '2026-06-06', '2026-06-07', 98000, 'EXCLUSIVE', '2026-05-06', '2026-06-06', 'https://example.com/shows/parkhyoshin_winter.jpg', 1, 22, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (23, '성시경 연말 콘서트', '축가 2026', '연말 시즌 대표 발라드 공연', '2026-12-19', '2026-12-21', 83000, 'EXCLUSIVE', '2026-11-19', '2026-12-19', 'https://example.com/shows/sungsikyung_yearend.jpg', 4, 23, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (24, '폴킴 팬콘서트', '마음, 둘', '토크+라이브 결합 팬콘서트', '2026-06-20', '2026-06-21', 44000, 'GENERAL', '2026-05-20', '2026-06-20', 'https://example.com/shows/paulkim_fancon.jpg', 5, 24, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (25, '볼빨간사춘기 투어', 'Seoul Breeze', '밴드셋 기반 인디팝 공연', '2026-07-03', '2026-07-04', 51000, 'GENERAL', '2026-06-03', '2026-07-03', 'https://example.com/shows/bol4_tour.jpg', 4, 25, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (26, 'YB 록 페스타', 'Rock Will Never Die', '강한 사운드 중심 록 공연', '2026-07-11', '2026-07-12', 62000, 'GENERAL', '2026-06-11', '2026-07-11', 'https://example.com/shows/yb_rockfesta.jpg', 6, 26, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (27, '자우림 단독 공연', '스테이 위드 미', '레전드 밴드 정규 셋리스트 공연', '2026-07-18', '2026-07-19', 59000, 'GENERAL', '2026-06-18', '2026-07-18', 'https://example.com/shows/jaurim_live.jpg', 7, 27, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (28, '국카스텐 콘서트', 'Pulse', '하드록 중심 단독 공연', '2026-07-25', '2026-07-26', 53000, 'GENERAL', '2026-06-25', '2026-07-25', 'https://example.com/shows/guckkasten_pulse.jpg', 8, 28, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (29, '장범준 소극장 공연', '잠이 오질 않네요', '어쿠스틱 소극장 시즌 공연', '2026-08-01', '2026-08-03', 41000, 'GENERAL', '2026-07-01', '2026-08-01', 'https://example.com/shows/jangbumjun_smallhall.jpg', 5, 29, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (30, '백예린 단독 라이브', 'Every letter I sent you', 'R&B 기반 라이브 공연', '2026-08-08', '2026-08-09', 46000, 'GENERAL', '2026-07-08', '2026-08-08', 'https://example.com/shows/baekyerin_live.jpg', 4, 30, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (31, 'Charlie Puth Live in Seoul', 'One Night Only', '글로벌 팝 스타 내한 단독 공연', '2026-08-15', '2026-08-16', 105000, 'EXCLUSIVE', '2026-07-15', '2026-08-15', 'https://example.com/shows/charlieputh_live.jpg', 2, 31, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (32, 'Dua Lipa Live in Korea', 'Future Nostalgia Night', '댄스팝 중심 내한 공연', '2026-08-22', '2026-08-23', 118000, 'EXCLUSIVE', '2026-07-22', '2026-08-22', 'https://example.com/shows/dualipa_live.jpg', 1, 32, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (33, 'Maroon 5 Live in Seoul', 'V Tour Korea', '히트곡 중심 밴드 내한 공연', '2026-08-29', '2026-08-30', 124000, 'GENERAL', '2026-07-29', '2026-08-29', 'https://example.com/shows/maroon5_live.jpg', 1, 33, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (34, 'Imagine Dragons Live', 'Mercury Seoul', '록 기반 글로벌 밴드 내한공연', '2026-09-05', '2026-09-06', 111000, 'GENERAL', '2026-08-05', '2026-09-05', 'https://example.com/shows/imagine_dragons_live.jpg', 2, 34, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (35, 'Sam Smith Live in Seoul', 'Gloria Tour', '보컬 중심 팝 라이브', '2026-09-12', '2026-09-13', 97000, 'GENERAL', '2026-08-12', '2026-09-12', 'https://example.com/shows/samsmith_live.jpg', 2, 35, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (36, 'BTS 부산 스페셜', 'Busan Festa', '부산 지역 스페셜 공연', '2026-09-19', '2026-09-20', 172000, 'EXCLUSIVE', '2026-08-19', '2026-09-19', 'https://example.com/shows/bts_busan_festa.jpg', 6, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (37, 'BLACKPINK 부산 콘서트', 'Pink Wave Busan', '부산 스타디움 투어 공연', '2026-09-26', '2026-09-27', 149000, 'EXCLUSIVE', '2026-08-26', '2026-09-26', 'https://example.com/shows/blackpink_busan.jpg', 6, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (38, 'SEVENTEEN 대구 공연', 'Follow Again Daegu', '대구 지역 앵콜 공연', '2026-10-03', '2026-10-04', 92000, 'GENERAL', '2026-09-03', '2026-10-03', 'https://example.com/shows/seventeen_daegu.jpg', 7, 3, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (39, 'TWICE 대전 공연', 'Ready To Be Daejeon', '대전 특별 편성 공연', '2026-10-10', '2026-10-11', 88000, 'GENERAL', '2026-09-10', '2026-10-10', 'https://example.com/shows/twice_daejeon.jpg', 9, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (40, 'IU 제주 콘서트', 'Island Mood', '제주 야외무대 스페셜 콘서트', '2026-10-17', '2026-10-18', 81000, 'EXCLUSIVE', '2026-09-17', '2026-10-17', 'https://example.com/shows/iu_jeju.jpg', 10, 9, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (41, '태연 부산 라이브', 'VOICE in Busan', '보컬 중심 부산 특별 공연', '2026-10-24', '2026-10-25', 67000, 'GENERAL', '2026-09-24', '2026-10-24', 'https://example.com/shows/taeyeon_busan.jpg', 6, 10, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (42, 'NCT 127 광주 공연', 'NEO CITY Gwangju', '광주 지역 단독 공연', '2026-11-01', '2026-11-02', 72000, 'GENERAL', '2026-10-01', '2026-11-01', 'https://example.com/shows/nct127_gwangju.jpg', 8, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (43, 'NewJeans 대구 팬콘', 'Bunnies in Daegu', '팬 참여형 스페셜 팬콘', '2026-11-08', '2026-11-09', 76000, 'EXCLUSIVE', '2026-10-08', '2026-11-08', 'https://example.com/shows/newjeans_daegu.jpg', 7, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (44, 'Stray Kids 부산 공연', 'MANIAC Busan', '부산 대형 공연장 일정', '2026-11-15', '2026-11-16', 102000, 'EXCLUSIVE', '2026-10-15', '2026-11-15', 'https://example.com/shows/skz_busan.jpg', 6, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (45, 'Bruno Mars 추가공연', '24K Magic Extra', '매진으로 인한 추가 편성 공연', '2026-11-22', '2026-11-23', 116000, 'GENERAL', '2026-10-22', '2026-11-22', 'https://example.com/shows/bruno_extra.jpg', 2, 14, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (46, 'Coldplay 추가공연', 'Music of the Spheres Extra', '내한 2차 추가 공연', '2026-11-29', '2026-11-30', 158000, 'EXCLUSIVE', '2026-10-29', '2026-11-29', 'https://example.com/shows/coldplay_extra_2.jpg', 1, 11, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (47, 'Taylor Swift 추가공연', 'Eras Tour Extra Seoul', '요청 폭주로 추가된 2회차 일정', '2026-12-06', '2026-12-07', 230000, 'EXCLUSIVE', '2026-11-06', '2026-12-06', 'https://example.com/shows/taylor_extra.jpg', 1, 13, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (48, '뮤지컬 레미제라블', 'Les Miserables Korea', '대형 라이선스 뮤지컬 내한팀', '2026-06-13', '2026-07-19', 96000, 'GENERAL', '2026-05-13', '2026-06-13', 'https://example.com/shows/lesmis_korea.jpg', 5, 39, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (49, '뮤지컬 위키드', 'Wicked Seoul Season', '서울 장기 공연 시즌', '2026-08-01', '2026-09-20', 102000, 'GENERAL', '2026-07-01', '2026-08-01', 'https://example.com/shows/wicked_seoul.jpg', 5, 37, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (50, '연극 햄릿', 'HAMLET 2026', '현대적 연출의 셰익스피어 연극', '2026-06-20', '2026-07-05', 35000, 'GENERAL', '2026-05-20', '2026-06-20', 'https://example.com/shows/hamlet_2026.jpg', 7, 36, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (51, '연극 리어왕', 'KING LEAR', '원전 기반 고전극 재해석', '2026-09-03', '2026-09-21', 31000, 'GENERAL', '2026-08-03', '2026-09-03', 'https://example.com/shows/kinglear.jpg', 7, 36, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (52, '코미디 연극 수상한 집', 'Funny House', '코미디 장르의 가족형 연극', '2026-09-26', '2026-10-18', 28000, 'GENERAL', '2026-08-26', '2026-09-26', 'https://example.com/shows/funny_house.jpg', 9, 38, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (53, '연극 어느 날', 'One Day Story', '현대극 기반 감정선 중심 작품', '2026-10-01', '2026-10-26', 26000, 'GENERAL', '2026-09-01', '2026-10-01', 'https://example.com/shows/one_day_story.jpg', 9, 40, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (54, '뮤지컬 맘마미아', 'Mamma Mia Korea', '가족 관람형 인기 뮤지컬', '2026-10-10', '2026-11-29', 89000, 'GENERAL', '2026-09-10', '2026-10-10', 'https://example.com/shows/mammamia_korea.jpg', 5, 37, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (55, '연극 오셀로', 'OTHELLO', '고전 비극 무대화 작품', '2026-11-05', '2026-11-23', 29500, 'GENERAL', '2026-10-05', '2026-11-05', 'https://example.com/shows/othello.jpg', 7, 36, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (56, '코미디 연극 웃픈 인생', 'Laugh and Cry', '코미디+휴먼 드라마 결합 작품', '2026-11-12', '2026-12-06', 33000, 'GENERAL', '2026-10-12', '2026-11-12', 'https://example.com/shows/laugh_and_cry.jpg', 9, 38, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (57, '뮤지컬 지킬앤하이드', 'Jekyll and Hyde', '스테디셀러 라이선스 뮤지컬', '2026-11-21', '2027-01-31', 115000, 'EXCLUSIVE', '2026-10-21', '2026-11-21', 'https://example.com/shows/jekyll_hyde.jpg', 4, 37, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (58, '드라마 연극 봄의 기억', 'Memory of Spring', '서정적 드라마 장르 연극', '2026-12-03', '2026-12-28', 24500, 'GENERAL', '2026-11-03', '2026-12-03', 'https://example.com/shows/memory_of_spring.jpg', 9, 40, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (59, '국립극단 정기공연', '시즌 레퍼토리', '국립극단 시즌 레퍼토리 연극', '2027-01-08', '2027-02-07', 38000, 'GENERAL', '2026-12-08', '2027-01-08', 'https://example.com/shows/national_repertory.jpg', 7, 36, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOWS (id, title, sub_title, info, start_date, end_date, view_count, sale_type, sale_start_date, sale_end_date, image, venue_id, performer_id, created_at, created_by, status) VALUES (60, '서울시뮤지컬 갈라', 'Seoul Musical Gala', '주요 넘버로 구성한 갈라 콘서트', '2027-01-15', '2027-01-17', 52000, 'GENERAL', '2026-12-15', '2027-01-15', 'https://example.com/shows/seoul_musical_gala.jpg', 4, 37, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 공연-장르 매핑 (1~63)
-- ==========================
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (1, 1, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (2, 2, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (3, 3, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (4, 4, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (5, 5, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (6, 6, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (7, 7, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (8, 8, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (9, 9, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (10, 9, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (11, 10, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (12, 11, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (13, 11, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (14, 12, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (15, 13, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (16, 14, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (17, 14, 3, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (18, 15, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (19, 16, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (20, 17, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (21, 18, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (22, 19, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (23, 20, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (24, 21, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (25, 22, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (26, 23, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (27, 24, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (28, 25, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (29, 26, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (30, 27, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (31, 28, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (32, 29, 7, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (33, 30, 3, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (34, 31, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (35, 32, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (36, 33, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (37, 34, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (38, 35, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (39, 36, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (40, 37, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (41, 38, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (42, 39, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (43, 40, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (44, 41, 6, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (45, 42, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (46, 43, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (47, 44, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (48, 45, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (49, 46, 5, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (50, 47, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (51, 48, 10, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (52, 49, 10, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (53, 50, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (54, 51, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (55, 52, 9, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (56, 53, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (57, 54, 10, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (58, 55, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (59, 56, 9, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (60, 57, 10, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (61, 58, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (62, 59, 8, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GENRES (id, show_id, genre_id, created_at, created_by, status) VALUES (63, 60, 10, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 좌석 생성 (section/row_no/seat_no)
-- ==========================
-- A구역
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (1, 'A', '1', '1', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (2, 'A', '1', '2', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (3, 'A', '1', '3', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (4, 'A', '2', '1', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (5, 'A', '2', '2', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (6, 'A', '2', '3', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- B구역
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (7, 'B', '1', '1', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (8, 'B', '1', '2', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (9, 'B', '1', '3', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (10, 'B', '2', '1', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (11, 'B', '2', '2', '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SEATS (id, section, row_no, seat_no, created_at, created_by, status) VALUES (12, 'B', '2', '3', '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 공연 등급 (show_id = 1,2 샘플)
-- ==========================
INSERT INTO SHOW_GRADES (id, show_id, grade_code, grade_name, price, sort_order, created_at, created_by, status) VALUES (1, 1, 'VIP', 'VIP석', 220000, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GRADES (id, show_id, grade_code, grade_name, price, sort_order, created_at, created_by, status) VALUES (2, 1, 'R', 'R석', 176000, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GRADES (id, show_id, grade_code, grade_name, price, sort_order, created_at, created_by, status) VALUES (3, 1, 'S', 'S석', 132000, 3, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GRADES (id, show_id, grade_code, grade_name, price, sort_order, created_at, created_by, status) VALUES (4, 1, 'A', 'A석', 99000, 4, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO SHOW_GRADES (id, show_id, grade_code, grade_name, price, sort_order, created_at, created_by, status) VALUES (5, 2, 'VIP', 'VIP석', 198000, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GRADES (id, show_id, grade_code, grade_name, price, sort_order, created_at, created_by, status) VALUES (6, 2, 'R', 'R석', 154000, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_GRADES (id, show_id, grade_code, grade_name, price, sort_order, created_at, created_by, status) VALUES (7, 2, 'S', 'S석', 110000, 3, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 공연-좌석 매핑 (show_id = 1)
-- ==========================
-- A구역 → VIP
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (1, 1, 1, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (2, 1, 2, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (3, 1, 3, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (4, 1, 4, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (5, 1, 5, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (6, 1, 6, 1, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- B구역 → R석
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (7, 1, 7, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (8, 1, 8, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (9, 1, 9, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (10, 1, 10, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (11, 1, 11, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO SHOW_SEATS (id, show_id, seat_id, show_grade_id, created_at, created_by, status) VALUES (12, 1, 12, 2, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 공연 회차 (Performance) - show_id = 1
-- ==========================
INSERT INTO PERFORMANCES (id, show_id, round_no, start_time, end_time, order_open_time, order_close_time, state, max_can_hold_count, hold_time, created_at, created_by, status)
VALUES (1, 1, 1, '2026-03-01 19:00:00', '2026-03-01 21:30:00', '2026-02-01 10:00:00', '2026-03-01 18:00:00', 'OPEN', 4, 300, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO PERFORMANCES (id, show_id, round_no, start_time, end_time, order_open_time, order_close_time, state, max_can_hold_count, hold_time, created_at, created_by, status)
VALUES (2, 1, 2, '2026-03-02 17:00:00', '2026-03-02 19:30:00', '2026-02-01 10:00:00', '2026-03-02 16:00:00', 'OPEN', 4, 300, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

INSERT INTO PERFORMANCES (id, show_id, round_no, start_time, end_time, order_open_time, order_close_time, state, max_can_hold_count, hold_time, created_at, created_by, status)
VALUES (3, 1, 3, '2026-03-03 17:00:00', '2026-03-03 19:30:00', '2026-02-01 10:00:00', '2026-03-03 16:00:00', 'OPEN', 4, 300, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 회차별 좌석 상태 (performance_id = 1)
-- ==========================
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (1, 1, 1, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (2, 1, 2, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (3, 1, 3, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (4, 1, 4, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (5, 1, 5, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (6, 1, 6, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (7, 1, 7, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (8, 1, 8, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (9, 1, 9, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (10, 1, 10, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (11, 1, 11, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (12, 1, 12, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
-- ==========================
-- 회차별 좌석 상태 (performance_id = 2)
-- ==========================
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (13, 2, 1, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (14, 2, 2, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (15, 2, 3, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (16, 2, 4, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (17, 2, 5, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (18, 2, 6, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (19, 2, 7, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (20, 2, 8, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (21, 2, 9, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (22, 2, 10, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (23, 2, 11, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (24, 2, 12, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');

-- ==========================
-- 회차별 좌석 상태 (performance_id = 3)
-- ==========================
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (25, 3, 1, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (26, 3, 2, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (27, 3, 3, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (28, 3, 4, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (29, 3, 5, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (30, 3, 6, 'AVAILABLE', 220000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (31, 3, 7, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (32, 3, 8, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (33, 3, 9, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (34, 3, 10, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (35, 3, 11, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
INSERT INTO PERFORMANCE_SEATS (id, performance_id, seat_id, state, price, created_at, created_by, status) VALUES (36, 3, 12, 'AVAILABLE', 176000, '2026-01-01 10:00:00', 'seed', 'ACTIVE');
