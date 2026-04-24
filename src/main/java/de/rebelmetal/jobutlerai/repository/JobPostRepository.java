package de.rebelmetal.jobutlerai.repository;

import de.rebelmetal.jobutlerai.domain.ApplicationStatus;
import de.rebelmetal.jobutlerai.domain.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persistence layer interface for {@link JobPost} entities.
 *
 * Spring Data JPA generates the full implementation at application startup.
 * No SQL, no boilerplate — the method names ARE the query definitions.
 *
 * Generic parameters:
 *   - JobPost : the entity type this repository manages
 *   - Long    : the type of JobPost's primary key (@Id field)
 */
@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    /**
     * Checks whether a job posting with the given source URL already exists.
     * Used by the scraper to prevent duplicates.
     */
    boolean existsBySourceUrl(String sourceUrl);

    /**
     * Finds all job postings with a specific status.
     * Used by the AI service to find NEW jobs waiting for analysis.
     * Spring Data JPA translates this into: SELECT * FROM job_posts WHERE status = ?
     */
    List<JobPost> findAllByStatus(ApplicationStatus status);

    /**
     * Startup recovery: resets all stuck ANALYZING jobs back to NEW.
     *
     * If the application crashes mid-analysis, some jobs get stuck in ANALYZING
     * forever. On next startup, this query resets them so they can be re-processed.
     *
     * @Modifying: required for UPDATE/DELETE queries — tells Spring Data JPA
     *   this is not a SELECT but a state-changing operation.
     * @Transactional: each bulk update runs in its own transaction.
     */
    @Modifying
    @Query("UPDATE JobPost j SET j.status = :newStatus WHERE j.status = :oldStatus")
    int resetStuckJobs(@Param("oldStatus") ApplicationStatus oldStatus,
                       @Param("newStatus") ApplicationStatus newStatus);
}
