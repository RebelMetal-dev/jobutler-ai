package de.rebelmetal.jobutlerai.service;

import de.rebelmetal.jobutlerai.domain.JobPost;
import de.rebelmetal.jobutlerai.repository.JobPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobPersistenceService {

    private final JobPostRepository repository;

    /**
     * Saves a job post only if the URL is not already in the database.
     * Each call runs in its own short transaction — prevents DB connection
     * starvation during long scrape runs.
     */
    @Transactional
    public void saveIfNew(JobPost jobPost) {
        if (repository.existsBySourceUrl(jobPost.getSourceUrl())) {
            log.debug("Skipping existing job: {}", jobPost.getSourceUrl());
            return;
        }
        repository.save(jobPost);
        log.info("New job saved: [{}] at {}", jobPost.getTitle(), jobPost.getCompany());
    }
}
