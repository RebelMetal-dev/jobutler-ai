package de.rebelmetal.jobutlerai.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobScraperService {

    private final Browser browser;

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
