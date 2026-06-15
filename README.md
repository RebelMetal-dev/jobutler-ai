# JoButler AI 🤖

> **An intelligent job scraper that collects, deduplicates and AI-rates job postings — built with Spring Boot 3, Playwright and a local LLM via Ollama.**

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Build-Maven-red?logo=apachemaven)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%2016-blue?logo=postgresql)
![Playwright](https://img.shields.io/badge/Scraping-Playwright-45ba4b?logo=playwright)
![Status](https://img.shields.io/badge/Status-In%20Active%20Development-yellow)

---

## What is JoButler AI?

Job hunting is tedious. You visit the same boards daily, copy-paste postings into spreadsheets, and lose track of what you've already seen.

**JoButler AI automates this entire workflow:**

1. A Playwright-powered scraper crawls job boards and extracts postings
2. Spring Data JPA persists them in PostgreSQL — with built-in URL-based deduplication so the same posting is never stored twice
3. A local AI model (Ollama) reads each job description and produces a relevance rating and a plain-language summary
4. A Thymeleaf dashboard lets you review, filter, and manage your pipeline

No cloud AI subscription needed. No data leaves your machine.

---

## Tech Stack

| Layer | Technology | Why |
|---|---|---|
| Language | Java 21 | Modern LTS — Records, Pattern Matching, Virtual Threads |
| Framework | Spring Boot 3.4.5 | Production-grade IoC, auto-configuration, embedded server |
| Persistence | Spring Data JPA + Hibernate | Repository pattern, automatic schema generation |
| Database | PostgreSQL 16 | Robust, production-ready relational DB |
| Scraping Engine | Microsoft Playwright 1.44 | Controls a real Chromium browser — handles JS-heavy sites |
| AI Layer | Spring AI 1.0.0 + Ollama | Local LLM inference — no API key, no data sharing |
| View Layer | Thymeleaf | Server-side rendered dashboard |
| Build | Maven (mvnw wrapper) | Reproducible builds without a global Maven install |
| Testing | JUnit 5 + Mockito + AssertJ | Full test toolchain |
| Code gen | Lombok | Reduces JPA entity boilerplate |

---

## Architecture Overview

```
┌─────────────────────────────────────────────────┐
│                  JoButler AI                    │
│                                                 │
│  ┌──────────────┐    ┌────────────────────────┐ │
│  │  Playwright  │───▶│   JobScraperService    │ │
│  │  (Scraper)   │    │   (Orchestration)      │ │
│  └──────────────┘    └──────────┬─────────────┘ │
│                                 │               │
│                    ┌────────────▼─────────────┐ │
│                    │    JobPostRepository     │ │
│                    │    (deduplication via    │ │
│                    │    existsBySourceUrl)    │ │
│                    └────────────┬─────────────┘ │
│                                 │               │
│                    ┌────────────▼─────────────┐ │
│                    │       PostgreSQL         │ │
│                    │       job_posts table    │ │
│                    └────────────┬─────────────┘ │
│                                 │               │
│  ┌──────────────┐    ┌──────────▼─────────────┐ │
│  │  Ollama LLM  │◀───│    AI Rating Service   │ │
│  │  (local)     │───▶│    (rating + summary)  │ │
│  └──────────────┘    └────────────────────────┘ │
│                                 │               │
│                    ┌────────────▼─────────────┐ │
│                    │   Thymeleaf Dashboard    │ │
│                    │   (review & manage)      │ │
│                    └──────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

---

## Domain Model

The central entity `JobPost` captures everything relevant about a job posting:

```java
// Key fields in JobPost.java
String title          // Job title
String company        // Hiring company
String location       // Office location or "Remote"
String description    // Full job description text
String sourceUrl      // UNIQUE — prevents duplicate scrapes
String rawSalary      // Original salary string from the posting
Integer minSalary     // Parsed minimum salary (for filtering)
Integer aiRating      // 1–10 score from the local LLM
String aiSummary      // Plain-language summary from the local LLM
ApplicationStatus status  // ENUM: NEW / APPLIED / REJECTED / INTERESTING
LocalDateTime createdAt   // Set once on insert — immutable
LocalDateTime updatedAt   // Auto-updated on every change
```

The `ApplicationStatus` enum tracks your personal pipeline — from first discovery to final decision.

---

## Current Status

> **Phase 1 complete. Phase 3 (Scraper) in active development.**

| Phase | Component | Status |
|---|---|---|
| 1 | Spring Boot + PostgreSQL setup | ✅ Complete |
| 1 | Domain model (`JobPost`, `ApplicationStatus`) | ✅ Complete |
| 1 | Repository layer with deduplication | ✅ Complete |
| 1 | Credential security (`.gitignore`) | ✅ Complete |
| 3 | `JobScraperService` (Playwright integration) | 🔧 In Progress |
| 3 | First live scrape + deduplication validation | 🔧 In Progress |
| 4 | REST API layer | ⏳ Planned |
| 5 | AI rating + summary pipeline | ⏳ Planned |
| 6 | Thymeleaf dashboard | ⏳ Planned |

---

## Getting Started

### Prerequisites

- Java 21+
- Maven (or use the included `./mvnw` wrapper)
- PostgreSQL 16 running locally on port `5432`
- [Ollama](https://ollama.ai) installed and running locally

### Database Setup

```sql
-- Create the database in PostgreSQL
CREATE DATABASE "jobutler-ai";
```

### Configuration

Copy the example config and fill in your credentials:

```bash
cp src/main/resources/application.properties.example \
   src/main/resources/application.properties
```

Then edit `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jobutler-ai
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
```

> **Note:** `application.properties` is listed in `.gitignore` — your credentials never leave your machine.

### Run the Application

```bash
# Using the Maven wrapper (no global Maven installation needed)
./mvnw spring-boot:run
```

Hibernate auto-generates the `job_posts` table on first startup.

---

## Design Decisions Worth Noting

**URL-based deduplication:** The `source_url` column has a `UNIQUE` constraint and the repository exposes `existsBySourceUrl()`. Before any insert, the scraper checks this — no duplicate postings, no matter how many times you run it.

**Raw + Parsed Salary Pattern:** The `rawSalary` field stores the original string exactly as scraped (`"€55.000 – 70.000 / Jahr"`). The `minSalary` field holds the parsed integer for filtering. This separation means parsing bugs never destroy the original data.

**Local AI only:** By using Ollama instead of a cloud API, all job data stays on your machine. No subscriptions, no rate limits, no privacy concerns.

**Phase ordering (Scraper before REST API):** The service layer and REST endpoints are deliberately built after the first successful live scrape. Real-world data validates the schema before we lock in an API contract.

---

## Roadmap

- [ ] `JobScraperService` — Playwright scraping orchestration
- [ ] First successful live scrape with deduplication validation
- [ ] REST API: `GET /jobs`, `GET /jobs/{id}`, `PATCH /jobs/{id}/status`
- [ ] AI pipeline: Ollama rating + summary on each new job post
- [ ] Thymeleaf dashboard with filter and sort
- [ ] Unit tests for service and repository layers
- [ ] Salary range filtering via `minSalary`

---

## Part of the JavaRoad Portfolio

This project is part of my [JavaRoad](https://github.com/RebelMetal-dev/JavaRoad) learning path — a structured progression from Java fundamentals to production-grade Spring Boot applications.

| Project | Focus |
|---|---|
| [JavaRoad](https://github.com/RebelMetal-dev/JavaRoad) | Multi-Module Maven, REST API, Spring Data JPA, JUnit 5, SOLID Principles |
| [schocken-web-app](https://github.com/RebelMetal-dev/schocken-web-app) | Full-Stack with Spring Boot 3.5, HTMX, Thymeleaf, Gradle (Kotlin DSL) |
| **jobutler-ai** | Web Scraping, AI Integration, PostgreSQL, Spring AI + Ollama |

---

## Author

**Christoph Breddin** — Junior Java Backend Developer
[GitHub](https://github.com/RebelMetal-dev) · [LinkedIn](https://www.linkedin.com/in/christoph-breddin-735b1b2a7/)