# Project Status: JobButler AI - Infrastructure Baseline

## Current State
- **Backend:** Spring Boot 3.4.5 with Java 21.
- **Database:** PostgreSQL 16 active on port 5432.
- **ORM:** Hibernate is configured to `update` mode.
- **Connection:** Verified between IntelliJ and PostgreSQL.

## Database Schema (Verified)
Table `job_posts` includes:
- `id` (Primary Key)
- `title`, `company`, `location`
- `source_url` (Unique Constraint)
- `status` (Enum: NEW, AI_REVIEWED, etc.)
- `ai_rating`, `ai_summary` (AI processing fields)

## Configuration (`application.properties`)
- App Name: `jobutler-ai`
- Database: `jobutler-ai`
- Driver: `org.postgresql.Driver`