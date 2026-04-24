package de.rebelmetal.jobutlerai.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Central domain entity representing a single scraped job posting.
 *
 * Lombok rules (Manifesto-compliant):
 *   - @Getter / @Setter at class level: generates accessors without the
 *     dangerous equals/hashCode/toString that @Data would add.
 *   - @NoArgsConstructor: required by the JPA specification — Hibernate
 *     instantiates entities via reflection and needs a no-args constructor.
 *   - @Data is FORBIDDEN on JPA entities (GEMINI.md, Rule 1).
 */
@Entity
@Table(name = "job_posts")
@Getter
@Setter
@NoArgsConstructor
public class JobPost {

    // ── Identity ──────────────────────────────────────────────────────────
    // SEQUENCE strategy: Hibernate pre-allocates 50 IDs per database round-trip
    // (allocationSize = 50). This is far more efficient than IDENTITY, which
    // requires one round-trip to the DB per INSERT — critical for batch scraping.
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_post_seq")
    @SequenceGenerator(
            name           = "job_post_seq",
            sequenceName   = "job_post_sequence",
            allocationSize = 50
    )
    private Long id;

    // ── Core Job Data ─────────────────────────────────────────────────────
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    private String location;

    // columnDefinition = "TEXT": bypasses the default 255-char VARCHAR limit.
    // Job descriptions routinely exceed 2,000 characters.
    @Column(columnDefinition = "TEXT")
    private String description;

    // unique = true: the scraper uses this URL as a natural deduplication key.
    // If a posting is encountered again in a later run, the INSERT is rejected
    // by the DB constraint before Hibernate even tries to persist it.
    @Column(name = "source_url", nullable = false, unique = true)
    private String sourceUrl;

    // The date the job was originally posted externally (scraped from the site).
    // Distinct from createdAt, which is when WE stored it in our system.
    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    // ── Salary Fields (Raw + Parsed pattern) ─────────────────────────────
    // rawSalary: exact text scraped from the posting. Never discard this —
    // it is the audit trail and the input for any future salary parser.
    @Column(name = "raw_salary")
    private String rawSalary;

    // minSalary: the parsed, normalised floor value in full EUR integers.
    // e.g. "80k - 100k EUR" -> 80000. Used for: WHERE min_salary >= ?
    // NULL is valid: not every posting has a machine-readable salary.
    @Column(name = "min_salary")
    private Integer minSalary;

    // ── AI Features ───────────────────────────────────────────────────────
    // All fields nullable by design: populated only after the AI pipeline
    // has run. NULL means "not yet analysed" — a valid, expected state.

    // Comma-separated tech keywords extracted by the AI.
    // e.g. "Java, Spring Boot, Kubernetes"
    // String (not enum) — LLM output is unpredictable.
    @Column(name = "tech_stack")
    private String techStack;

    // Seniority level extracted by the AI.
    // e.g. "Junior", "Senior", "Staff", "Founding Engineer"
    // String (not enum) — LLM may return creative values like "Staff Engineer".
    @Column(name = "seniority")
    private String seniority;

    // Remote-availability as extracted by the AI.
    // Boolean (wrapper, not primitive) — null = AI could not determine this.
    // primitive boolean would default to false, implying "not remote" when unknown.
    @Column(name = "remote")
    private Boolean remote;

    @Column(name = "ai_rating")
    private Integer aiRating;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    // ── Lifecycle Status ──────────────────────────────────────────────────
    // Default set in Java, not in SQL — the application layer owns this truth.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status = ApplicationStatus.NEW;

    // ── Audit Timestamps ──────────────────────────────────────────────────
    // @CreationTimestamp: Hibernate sets this exactly once, on first INSERT.
    // updatable = false enforces this at the DB column level — even a direct
    // SQL UPDATE cannot overwrite it accidentally.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // @UpdateTimestamp: Hibernate refreshes this on every MERGE/UPDATE.
    // Together with createdAt, this gives us a full audit trail for free.
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
