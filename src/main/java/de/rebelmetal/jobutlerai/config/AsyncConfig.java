package de.rebelmetal.jobutlerai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Async configuration for background AI analysis tasks.
 *
 * Why a custom TaskExecutor instead of Spring's default?
 *   Spring's default async executor is unbounded — it would spawn a new thread
 *   for every job simultaneously. With 30 jobs and Llama3 running locally,
 *   that would saturate the CPU and potentially crash the machine.
 *
 *   This executor limits concurrency to 2 threads max, so Llama3 processes
 *   at most 2 jobs at a time. The remaining jobs wait in the queue (capacity: 100).
 *
 * Thread naming: "ai-analysis-1", "ai-analysis-2" — visible in logs and profilers.
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "aiTaskExecutor")
    public TaskExecutor aiTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Always keep 1 thread alive — ready for the next analysis request.
        executor.setCorePoolSize(1);

        // Allow up to 2 concurrent AI calls — protects local hardware.
        executor.setMaxPoolSize(2);

        // Queue up to 100 jobs waiting for an available thread.
        executor.setQueueCapacity(100);

        // Prefix for thread names — makes logs readable.
        executor.setThreadNamePrefix("ai-analysis-");

        executor.initialize();
        return executor;
    }
}
