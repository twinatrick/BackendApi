package com.example.BackendApi.Crawler.impl;

import com.example.BackendApi.Crawler.IJobCrawler;
import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Component;

/**
 * Selenium-based crawler for dynamic web pages.
 * Suitable for websites that rely on JavaScript for content rendering.
 */
@Slf4j
@Component
public class SeleniumJobCrawler implements IJobCrawler {

    private static final int PAGE_LOAD_TIMEOUT_MS = 30000;

    @Override
    public String crawl(String url) {
        WebDriver driver = null;
        try {
            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(java.time.Duration.ofMillis(PAGE_LOAD_TIMEOUT_MS));

            log.info("Crawling URL with Selenium: {}", url);
            driver.get(url);

            // Wait for dynamic content to load
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return driver.getPageSource();
        } catch (Exception e) {
            log.error("Failed to crawl URL with Selenium: {}", url, e);
            throw new RuntimeException("Failed to crawl URL: " + url, e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
