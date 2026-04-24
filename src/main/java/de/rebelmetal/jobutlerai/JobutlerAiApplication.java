package de.rebelmetal.jobutlerai;

import de.rebelmetal.jobutlerai.service.AiAnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class JobutlerAiApplication {

    private final AiAnalysisService aiAnalysisService;

    public static void main(String[] args) {
        SpringApplication.run(JobutlerAiApplication.class, args);
    }

    /**
     * Startup recovery: runs once after the application is fully started.
     * Resets any jobs stuck in ANALYZING (from a previous crash) back to NEW.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        aiAnalysisService.resetStuckJobs();
    }
}
