# README and AI Docs Cleanup Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 루트 README를 문서 지도로 정리하고, 오래된 AI 초안 문서가 현재 기준 문서로 오해되지 않게 표시한다.

**Architecture:** 자동화 설정 파일은 이동하거나 수정하지 않고, 문서 계층만 정리한다. `README.md`는 현재 기준 문서와 AI 설정 위치를 안내하는 진입점이 되고, `README_JUNIE.md`는 과거 AI 초안임을 상단에서 명확히 알린다.

**Tech Stack:** Markdown, Git, PowerShell 정적 검증

---

## File Map

- Modify: `README.md`
  - 루트 진입점과 문서 지도로 재작성한다.
  - 기준 문서, 제품 문서, 초안 문서, AI 설정 위치를 안내한다.
- Modify: `README_JUNIE.md`
  - 상단에 보관 후보/과거 AI 초안 경고를 추가한다.
  - 원문은 삭제하지 않는다.
- Create: `docs/superpowers/plans/2026-05-22-readme-ai-docs-cleanup.md`
  - 이번 실행 계획을 기록한다.

## Chunk 1: README Role Cleanup

### Task 1: Rewrite root README as document map

**Files:**
- Modify: `README.md`

- [x] **Step 1: Replace old draft body**
  - 기존 초기 요구사항/TDD 메모 중심 내용을 제거한다.
  - 프로젝트 한 줄 설명, 문서 읽는 순서, 로컬 실행 명령, 폴더 역할, AI 설정 위치를 작성한다.

- [x] **Step 2: Keep scope explicit**
  - `README_CODEX.md`와 `README_ARCHITECTURE.md`가 현재 개발 기준임을 명시한다.
  - `README_JUNIE.md`가 과거 AI 초안임을 명시한다.
  - `.codex`, `.agents`, `.github` 파일은 이번 작업에서 이동하지 않았음을 명시한다.

### Task 2: Mark README_JUNIE as archived draft candidate

**Files:**
- Modify: `README_JUNIE.md`

- [x] **Step 1: Add notice at top**
  - 문서 최상단에 "현재 개발 기준이 아님"을 명시한다.
  - 최신 기준 문서 링크를 안내한다.

- [x] **Step 2: Preserve original draft**
  - 기존 본문은 삭제하지 않는다.
  - 후속 작업에서 `docs/archive/` 이동 또는 삭제를 검토한다고 남긴다.

## Chunk 2: Static Verification

### Task 3: Verify document consistency

**Files:**
- Check: `README.md`
- Check: `README_JUNIE.md`
- Check: `AGENTS.md`
- Check: `docs/superpowers/specs/2026-05-22-readme-ai-docs-cleanup-design.md`

- [x] **Step 1: Check changed files**
  - Run: `git -c safe.directory=* diff -- README.md README_JUNIE.md docs/superpowers/plans/2026-05-22-readme-ai-docs-cleanup.md`
  - Expected: only README and plan documentation changes appear.

- [x] **Step 2: Check UTF-8 BOM absence**
  - Run: `Get-Content -Path 'README.md' -Encoding Byte -TotalCount 3 | ForEach-Object { $_.ToString('X2') }`
  - Expected: first bytes are not `EF BB BF`.
  - Run: `Get-Content -Path 'README_JUNIE.md' -Encoding Byte -TotalCount 3 | ForEach-Object { $_.ToString('X2') }`
  - Expected: first bytes are not `EF BB BF`.

- [x] **Step 3: Check key references**
  - Run: `rg -n "README_CODEX.md|README_ARCHITECTURE.md|README_PRODUCT.md|README_JUNIE.md|\\.codex|\\.agents|CodeRabbit|Copilot|Claude|PR-Agent" README.md README_JUNIE.md`
  - Expected: root README references the current docs and AI settings; README_JUNIE points back to current docs.

- [x] **Step 4: Check worktree separation**
  - Run: `git -c safe.directory=* status --short`
  - Expected: only `README.md`, `README_JUNIE.md`, and this plan are new changes from this task, while pre-existing queue changes remain unstaged and untouched.
