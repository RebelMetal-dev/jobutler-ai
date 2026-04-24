package de.rebelmetal.jobutlerai.controller;

import de.rebelmetal.jobutlerai.repository.JobPostRepository;
import de.rebelmetal.jobutlerai.service.AiAnalysisService;
import de.rebelmetal.jobutlerai.service.JobScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Cockpit-Controller — verbindet das Web-Dashboard mit dem Scraper.
 *
 * Zwei Verantwortlichkeiten:
 *   GET  /                    → Jobs aus der DB laden und ans Template übergeben.
 *   POST /scrape/hackernews   → Scraper starten, dann zurück zum Dashboard redirecten.
 *
 * Architektur-Notiz: Das Repository wird hier direkt injiziert (kein Service-Layer
 * dazwischen) — pragmatische Entscheidung für Phase 4, da es sich um eine reine
 * Leseabfrage handelt. Wird in Phase 5 in einen JobQueryService ausgelagert.
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class JobController {

    private final JobScraperService scraperService;
    private final AiAnalysisService aiAnalysisService;
    private final JobPostRepository repository;

    /**
     * Startseite des Cockpits.
     * Lädt alle Jobs aus der DB und stellt sie dem Thymeleaf-Template bereit.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("jobs", repository.findAll());
        return "index"; // → src/main/resources/templates/index.html
    }

    /**
     * HTMX-Endpoint: liefert nur den Tabellen-Fragment zurück (kein ganzes HTML).
     * Wird alle 3 Sekunden vom Dashboard aufgerufen um Status-Updates zu zeigen.
     */
    @GetMapping("/jobs/table")
    public String jobsTable(Model model) {
        model.addAttribute("jobs", repository.findAll());
        return "index :: jobsTable"; // Thymeleaf Fragment-Syntax
    }

    /**
     * Startet die Butler-KI-Analyse für alle NEW-Jobs — asynchron im Hintergrund.
     * Der Browser bekommt sofort den Redirect zurück, während Llama3 arbeitet.
     */
    @PostMapping("/analyze")
    public String triggerAnalysis(RedirectAttributes redirectAttributes) {
        log.info("Butler AI analysis triggered via dashboard");
        aiAnalysisService.analyzeAllNewJobs();
        redirectAttributes.addFlashAttribute("message",
                "Butler-Analyse gestartet! Die Status-Badges aktualisieren sich automatisch.");
        return "redirect:/";
    }

    /**
     * Startet den HackerNews-Scraper manuell (On-Demand).
     *
     * Post-Redirect-Get Pattern: Nach dem Scrape wird auf GET / weitergeleitet,
     * damit ein Browser-Reload die Aktion nicht doppelt ausführt.
     * Das FlashAttribute überlebt den Redirect und wird einmalig angezeigt.
     */
    @PostMapping("/scrape/hackernews")
    public String triggerHackerNews(RedirectAttributes redirectAttributes) {
        log.info("Manual HackerNews scrape triggered via dashboard");
        scraperService.scrapeHackerNewsJobs();
        redirectAttributes.addFlashAttribute("message", "HackerNews Scrape erfolgreich abgeschlossen!");
        return "redirect:/";
    }
}
