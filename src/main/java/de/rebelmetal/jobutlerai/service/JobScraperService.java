package de.rebelmetal.jobutlerai.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import de.rebelmetal.jobutlerai.domain.JobPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobScraperService {

    private final Browser browser;
    private final JobPersistenceService persistenceService;

    public void scrapeHackerNewsJobs() {
        String url = "https://news.ycombinator.com/jobs";

        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            page.setDefaultTimeout(15_000);
            page.setDefaultNavigationTimeout(30_000);

            log.info("Fetching jobs from: {}", url);
            page.navigate(url);

            Locator jobRows = page.locator("tr.athing");
            int count = jobRows.count();
            log.info("Found {} potential job entries", count);

            for (int i = 0; i < count; i++) {
                try {
                    Locator row = jobRows.nth(i);
                    String fullText = row.innerText().trim();

                    // .first() — HN rows contain two anchors in td.title:
                    // 1) the actual job link, 2) a "from?site=..." domain reference.
                    // Playwright strict mode throws if a locator resolves to >1 element,
                    // so we explicitly take the first anchor (always the job link).
                    String link = row.locator("td.title a").first().getAttribute("href");
                    if (link == null || link.isBlank()) {
                        log.warn("No link found for row {}, skipping", i);
                        continue;
                    }

                    String sourceUrl = link.startsWith("http")
                            ? link
                            : "https://news.ycombinator.com/" + link;

                    JobPost job = new JobPost();
                    job.setTitle(fullText);
                    job.setCompany("Hacker News Jobs");
                    job.setSourceUrl(sourceUrl);
                    // status defaults to ApplicationStatus.NEW in the entity

                    persistenceService.saveIfNew(job);

                } catch (Exception e) {
                    log.warn("Failed to parse job row {}: {}", i, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Scraper run failed: ", e);
        }
    }

    /** Kept for quick connectivity checks — remove after Phase 3 is stable. */
    public void testScrape(String url) {
        try (BrowserContext context = browser.newContext();
             Page page = context.newPage()) {

            page.setDefaultTimeout(15_000);
            page.setDefaultNavigationTimeout(30_000);

            log.info("Navigating to: {}", url);
            page.navigate(url);
            log.info("Page loaded! Title: {}", page.title());

        } catch (Exception e) {
            log.error("Scraping failed: ", e);
        }
    }
}
