package de.rebelmetal.jobutlerai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rebelmetal.jobutlerai.domain.ApplicationStatus;
import de.rebelmetal.jobutlerai.domain.JobPost;
import de.rebelmetal.jobutlerai.repository.JobPostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AI-powered job analysis service — the "Butler Brain".
 *
 * Responsibilities:
 *   1. Fetch all NEW jobs from the database.
 *   2. For each job: send the title to Llama3 via Ollama and ask for structured JSON.
 *   3. Parse the JSON response and persist the extracted fields.
 *   4. Handle failures gracefully — set ANALYSIS_FAILED instead of crashing.
 *
 * Async: the public analyzeAllNewJobs() method runs in a background thread
 * (aiTaskExecutor pool). The HTTP request returns immediately to the browser.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiAnalysisService {

    private final JobPostRepository repository;
    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    private ChatClient chatClient;

    /**
     * Builds the ChatClient once after Spring has injected all dependencies.
     * @PostConstruct runs after the constructor — safe to use injected fields here.
     */
    @PostConstruct
    void init() {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Startup recovery: resets jobs stuck in ANALYZING back to NEW.
     * Called once at application start to recover from potential crashes.
     */
    @Transactional
    public void resetStuckJobs() {
        int count = repository.resetStuckJobs(ApplicationStatus.ANALYZING, ApplicationStatus.NEW);
        if (count > 0) {
            log.warn("Startup recovery: reset {} stuck ANALYZING jobs back to NEW", count);
        }
    }

    /**
     * Triggers AI analysis for all NEW jobs — runs asynchronously in background.
     *
     * The @Async annotation tells Spring: "run this method in the aiTaskExecutor
     * thread pool, not in the HTTP request thread". The caller (JobController)
     * returns immediately while this runs in the background.
     */
    @Async("aiTaskExecutor")
    public void analyzeAllNewJobs() {
        List<JobPost> newJobs = repository.findAllByStatus(ApplicationStatus.NEW);
        log.info("Butler starting AI analysis for {} NEW jobs", newJobs.size());

        for (JobPost job : newJobs) {
            analyzeSingleJob(job);
        }

        log.info("Butler AI analysis complete.");
    }

    /**
     * Analyzes a single job posting using Llama3.
     *
     * Flow:
     *   1. Mark as ANALYZING (so the UI shows the spinner badge)
     *   2. Send prompt to Llama3
     *   3. Parse JSON response
     *   4. Persist extracted fields + set ANALYZED
     *   On any error: set ANALYSIS_FAILED and log — never crash the loop.
     */
    @Transactional
    public void analyzeSingleJob(JobPost job) {
        log.info("Analyzing job [{}]: {}", job.getId(), job.getTitle());

        // Step 1: Mark as ANALYZING so the UI can show live progress
        job.setStatus(ApplicationStatus.ANALYZING);
        repository.save(job);

        try {
            // Step 2: Build the prompt and call Llama3
            String prompt = buildPrompt(job.getTitle());
            String rawResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("Raw AI response for job {}: {}", job.getId(), rawResponse);

            // Step 3: Parse the JSON response
            applyAiResponse(job, rawResponse);

            // Step 4: Mark as ANALYZED
            job.setStatus(ApplicationStatus.ANALYZED);
            repository.save(job);

            log.info("Job [{}] analyzed successfully — seniority={}, remote={}, techStack={}",
                    job.getId(), job.getSeniority(), job.getRemote(), job.getTechStack());

        } catch (Exception e) {
            // Never let one failed job stop the entire analysis loop.
            log.error("AI analysis failed for job [{}]: {}", job.getId(), e.getMessage());
            job.setStatus(ApplicationStatus.ANALYSIS_FAILED);
            repository.save(job);
        }
    }

    /**
     * Builds the prompt sent to Llama3.
     *
     * We instruct the model to respond ONLY with valid JSON — no explanation,
     * no markdown, no prose. This makes parsing reliable.
     */
    private String buildPrompt(String jobTitle) {
        return """
                You are a job analysis assistant. Analyze the following job title and respond ONLY with valid JSON.
                Do not add any explanation, markdown, or text outside the JSON object.

                Job title: "%s"

                Respond with this exact JSON structure:
                {
                  "techStack": "comma-separated list of technologies (e.g. Java, Spring Boot, React) or null if unclear",
                  "seniority": "one of: Junior, Mid, Senior, Lead, Staff, Principal, Founding, or null if unclear",
                  "remote": true or false or null if unclear,
                  "summary": "one short sentence describing this role in German"
                }
                """.formatted(jobTitle);
    }

    /**
     * Parses the JSON response from Llama3 and sets the fields on the JobPost.
     *
     * Llama3 sometimes wraps JSON in markdown code blocks like ```json ... ```
     * We strip those before parsing to ensure clean input to Jackson.
     */
    private void applyAiResponse(JobPost job, String rawResponse) throws Exception {
        // Strip markdown code blocks if Llama3 added them
        String cleaned = rawResponse
                .replaceAll("(?s)```json\\s*", "")
                .replaceAll("(?s)```\\s*", "")
                .trim();

        JsonNode json = objectMapper.readTree(cleaned);

        if (json.hasNonNull("techStack"))  job.setTechStack(json.get("techStack").asText());
        if (json.hasNonNull("seniority"))  job.setSeniority(json.get("seniority").asText());
        if (json.hasNonNull("remote"))     job.setRemote(json.get("remote").asBoolean());
        if (json.hasNonNull("summary"))    job.setAiSummary(json.get("summary").asText());
    }
}
