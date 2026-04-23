# GEMINI.md - JoButler-AI Master Manifest

## 1. Project Vision
An automated assistant for job scraping and AI evaluation.
**Stack:** Java 21, Spring Boot 3, Playwright, Spring Data JPA, PostgreSQL.

## 2. Technical Golden Rules
1. **Lombok:** Strict ban on `@Data` for JPA entities. Use explicit `@Getter`, `@Setter`.
2. **Scraping Safety:** Mandatory wait-strategies and headless browser management.
3. **Database:** Persistence Layer first (Entities -> Repositories).

## 3. Current Roadmap
- [x] Phase 0: IntelliJ Ultimate Setup (Java 21).
- [ ] Phase 1: Maven Configuration (Playwright & Spring Dependencies).
- [ ] Phase 2: Persistence Layer (Entities & Repositories).
- [ ] Phase 3: Core Scraper Implementation.