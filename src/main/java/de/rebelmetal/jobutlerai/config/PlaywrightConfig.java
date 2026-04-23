package de.rebelmetal.jobutlerai.config;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PlaywrightConfig {

    @Bean(destroyMethod = "close")
    public Playwright playwright() {
        return Playwright.create();
    }

    @Bean(destroyMethod = "close")
    public Browser browser(Playwright playwright,
            @Value("${playwright.no-sandbox:false}") boolean noSandbox) {

        List<String> args = noSandbox
                ? List.of("--no-sandbox", "--disable-setuid-sandbox", "--disable-dev-shm-usage")
                : List.of("--disable-dev-shm-usage");

        return playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        // Auf false setzen, wenn du zusehen willst
                        .setArgs(args)
        );
    }
}
