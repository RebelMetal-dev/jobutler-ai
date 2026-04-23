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

                    // Anchor-Element einmal holen und für Text + URL wiederverwenden.
                    // .first() schützt vor dem Strict-Mode-Fehler (zwei Anchors pro Zeile).
                    Locator titleAnchor = row.locator("td.title a").first();
                    String rawTitle = titleAnchor.innerText().trim();
                    String link = titleAnchor.getAttribute("href");

                    if (link == null || link.isBlank() || rawTitle == null || rawTitle.isBlank()) {
                        log.warn("Incomplete data for row {}, skipping", i);
                        continue;
                    }

                    // Zeilennummern entfernen (z.B. "1. " am Anfang) — Sicherheitsnetz,
                    // falls HN die Nummer doch in den Anchor-Text einbettet.
                    String cleanTitle = rawTitle.replaceFirst("^\\d+\\.\\s+", "");

                    // Firmennamen aus dem HN-Titelformat extrahieren.
                    // Typisches Muster: "Firmenname (YC Batch) Is Hiring ..."
                    // → Alles vor der ersten Klammer ist der Firmenname.
                    String company = "Hacker News Post"; // Fallback
                    if (cleanTitle.contains("(")) {
                        company = cleanTitle.substring(0, cleanTitle.indexOf("(")).trim();
                        // Sonderfall: "Zep AI Is Hiring – Beschreibung (YC Batch)"
                        // Die Klammer steht am Ende, nicht direkt nach dem Firmennamen.
                        // Sekundär-Check: enthält der extrahierte Name noch " is hiring"?
                        // Falls ja, dort nochmal abschneiden → "Zep AI"
                        if (company.toLowerCase().contains(" is hiring")) {
                            company = company.substring(0, company.toLowerCase().indexOf(" is hiring")).trim();
                        }
                    } else if (cleanTitle.toLowerCase().contains(" is hiring")) {
                        int idx = cleanTitle.toLowerCase().indexOf(" is hiring");
                        company = cleanTitle.substring(0, idx).trim();
                    }

                    String sourceUrl = link.startsWith("http")
                            ? link
                            : "https://news.ycombinator.com/" + link;

                    JobPost job = new JobPost();
                    job.setTitle(cleanTitle);
                    job.setCompany(company);
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
