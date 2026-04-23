# GEMINI.md - JoButler-AI Master Manifest

## 1. Project Vision
An automated assistant for job scraping and AI evaluation.
**Stack:** Java 21, Spring Boot 3, Playwright, Spring Data JPA, PostgreSQL.

## 2. Technical Golden Rules
1. **Lombok:** Strict ban on `@Data` for JPA entities. Use explicit `@Getter`, `@Setter`.
2. **Scraping Safety:** Mandatory wait-strategies and headless browser management.
3. **Database:** Persistence Layer first (Entities -> Repositories).

