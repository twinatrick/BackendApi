package com.example.BackendApi.Crawler.impl;

import com.example.BackendApi.Crawler.IJobCrawler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Composite crawler that tries Jsoup first, then falls back to Selenium.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompositeJobCrawler implements IJobCrawler {

    private final JsoupJobCrawler jsoupJobCrawler;
    private final SeleniumJobCrawler seleniumJobCrawler;

    @Override
    public String crawl(String url) {
        try {
            log.info("Attempting to crawl with Jsoup first: {}", url);
            return jsoupJobCrawler.crawl(url);
        } catch (Exception e) {
            log.warn("Jsoup crawling failed, falling back to Selenium: {}", url, e);
            return seleniumJobCrawler.crawl(url);
        }
    }
}
