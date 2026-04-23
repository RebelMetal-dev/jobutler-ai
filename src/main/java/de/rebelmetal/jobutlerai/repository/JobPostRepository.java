package de.rebelmetal.jobutlerai.repository;

import de.rebelmetal.jobutlerai.domain.JobPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     *
     * Used by the scraper before persisting a new posting to prevent duplicates.
     * Spring Data JPA translates this method name into:
     *   SELECT COUNT(*) > 0 FROM job_posts WHERE source_url = ?
     *
     * @param sourceUrl the URL of the job posting to check
     * @return true if a posting with this URL already exists, false otherwise
     */
    boolean existsBySourceUrl(String sourceUrl);
}
