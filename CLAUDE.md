# CLAUDE.md - Project Guidelines & Developer Instructions

## 1. Project Overview
- **Name:** JoButler-AI
- **Stack:** Java 21, Spring Boot 3.x, Maven, Playwright (Scraping), PostgreSQL.
- **Goal:** Intelligent Job-Scraper with AI-based analysis.

## 2. Professional Standards & Role Play
* **Language:** English for all code, comments, and technical documentation.
* **Role:** You are the Lead Developer. Gemini is the Senior Architect/Devil's Advocate.
* **Teaching Role:** You MUST explain the "How" and "Why" before proposing code. No spoonfeeding.
* **Safety:** No silent file modifications. All proposals must be confirmed by the User after Gemini's review.

## 3. Mandatory Git Commit Conventions
Format: `type: short subject`
Body MUST answer:
1. Why the old code was problematic.
2. The triggering scenario.
3. The new behavior.

## 4. Collaboration Workflow
1. Analyze existing logic.
2. Propose architectural change + explanation.
3. Wait for User/Gemini approval.
4. Execute implementation.

---

## 5. Current Project State (Stand: 2026-04-24)

### Abgeschlossene Phasen

**Phase 1–3: Scraper Core**
- Playwright scrapes HackerNews "Who is hiring?" threads
- Deduplication via `sourceUrl` (unique constraint)
- `JobPost` entity mit `ApplicationStatus` enum in PostgreSQL

**Phase 4: Cockpit Dashboard**
- Thymeleaf + HTMX Dashboard unter `http://localhost:8080`
- Button "HN scannen" → POST /scrape/hackernews
- HTMX-Polling alle 3s → GET /jobs/table (Fragment-Update, kein Full-Page-Reload)
- Post-Redirect-Get Pattern mit FlashAttributes

**Phase 5: Butler AI Brain ✅ (gerade abgeschlossen)**
- Spring AI 1.0.0 + Ollama (llama3:latest, lokal auf Port 11434)
- `AiAnalysisService`: analysiert alle NEW-Jobs asynchron (@Async, aiTaskExecutor-Pool)
- Strukturierter JSON-Prompt → extrahiert: techStack, seniority, remote, summary (DE)
- Status-Lifecycle: NEW → ANALYZING → ANALYZED / ANALYSIS_FAILED
- Startup-Recovery: resetStuckJobs() setzt ANALYZING → NEW bei App-Start
- Dashboard zeigt: animierte Status-Badges, Tech-Stack-Tags, Remote-Indikator, Butler-Check

### Letzter Commit-Stand
```
build: add Spring AI 1.0.0 Ollama starter for local LLM integration
```
Noch ausstehend (nach diesem Update zu committen):
- `feat: implement Phase 5 AI analysis via local Ollama/Llama3`
- `feat: upgrade Cockpit UI with Butler button and live status badges`

### Bekannte technische Entscheidungen / Gotchas
- PostgreSQL CHECK constraint `job_posts_status_check` manuell gedroppt (DBeaver), da Hibernate keine ALTER-Constraints macht
- Playwright `.first()` nötig wegen strict mode (td.title a liefert 2 Elemente pro Row)
- Spring AI Artifact heißt `spring-ai-starter-model-ollama` (NICHT spring-ai-ollama-spring-boot-starter)
- Git-Workflow: Claude schreibt Messages, User committet via IntelliJ (Strg+K)

---

## 6. Mögliche nächste Schritte (Phase 6+)

**Option A — Weitere Job-Quellen**
- LinkedIn, StepStone, Indeed scrapen (eigene Scraper-Klassen)
- Gemeinsames Interface `JobScraperStrategy` für alle Quellen

**Option B — Smarter Filtering / Scoring**
- AI bewertet Jobs nach Fit (Skillset des Users vs. Tech Stack)
- Score 0–100, sortierbar im Dashboard

**Option C — User Interaction**
- Buttons pro Job: "Interessiert" → USER_REVIEWED, "Archivieren" → ARCHIVED
- Detailansicht: vollständiger Job-Text, AI-Begründung

**Option D — Volltext-Analyse**
- Aktuell analysiert Butler nur den Job-Titel
- Nächster Schritt: komplette Stellenbeschreibung scrapen + analysieren

**Option E — Scheduling**
- Automatischer täglicher Scrape (Spring @Scheduled oder Quartz)
- Keine manuelle Button-Aktion mehr nötig