# Hold Order Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redis 기반 HOLD와 DB 기반 주문/이력, API, 예외 코드를 현재 티켓 예매 서비스에 연결한다.

**Architecture:** `hold`, `order`, `performanceseat`를 분리하고, 엔티티는 식별자 참조 중심으로 유지한다. Redis는 실시간 hold 상태를 관리하고, DB는 주문과 hold 이력을 관리한다.

**Tech Stack:** Spring Boot, Spring Data JPA, Redisson, Swagger/OpenAPI

---

## Chunk 1: 공통 타입과 기반 구성
- [ ] Hold/Order enum 정리
- [ ] ErrorCode/ErrorType 확장
- [ ] Redis 키 유틸 확장
- [ ] Scheduling 활성화

## Chunk 2: hold/order 도메인 추가
- [ ] `SeatHold`, `TicketOrder`, `OrderSeat` 엔티티 추가
- [ ] JPA 리포지토리/조회 헬퍼 추가
- [ ] Redis hold 서비스 추가

## Chunk 3: 유스케이스와 스케줄러
- [ ] hold 생성
- [ ] 주문 조회
- [ ] 주문 취소
- [ ] 주문 확정
- [ ] 만료 스케줄러
- [ ] 좌석 상태 통합 조회에 hold 반영

## Chunk 4: API 계약
- [ ] 요청/응답 DTO 추가
- [ ] Controller + Docs 인터페이스 추가
- [ ] Swagger 설명과 예외 응답 반영

## Chunk 6: 주문 상세 응답 재구성
- [ ] `POST /holds` 본문 제거 및 `X-Order-Id` 헤더 반환
- [ ] `OrderDetailResponse` 추가
- [ ] `GET /orders/{orderId}`를 주문/결제 화면 전용 응답으로 전환
- [ ] 상세 응답 조합용 매퍼/조회 로직 정리

## Chunk 5: 검증
- [ ] 컴파일 검증
- [ ] 변경 파일 점검
