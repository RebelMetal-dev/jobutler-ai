# GEMINI.md - JoButler-AI Master Manifest

## 1. Project Vision
An automated assistant that scrapes job boards, cleans the data, and uses AI to evaluate fit.
**Stack:** Java 21, Spring Boot, Playwright, Spring Data JPA, PostgreSQL.

## 2. Technical Golden Rules
1. **Lombok:** Never use `@Data` on JPA entities. Use explicit `@Getter`, `@Setter`.
2. **Scraping Safety:** Implement wait-strategies to avoid being blocked by websites.
3. **Database:** Persistence Layer first (Entities -> Repositories).

## 3. Current Roadmap
- [x] Phase 0: Fresh IntelliJ Ultimate Setup (Java 21).
- [ ] Phase 1: Maven Configuration (Playwright & Spring Dependencies).
- [ ] Phase 2: Core Scraper Implementation (The "Butler's Eye").
- [ ] Phase 3: AI Integration (The "Butler's Brain").