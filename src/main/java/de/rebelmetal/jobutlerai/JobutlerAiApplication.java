package de.rebelmetal.jobutlerai;

import de.rebelmetal.jobutlerai.service.JobScraperService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JobutlerAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobutlerAiApplication.class, args);
    }

    // TEMPORARY: First real scrape test — remove after validation.
    @Bean
    public CommandLineRunner runTest(JobScraperService scraperService) {
        return args -> scraperService.scrapeHackerNewsJobs();
    }
}
