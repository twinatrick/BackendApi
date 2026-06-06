package com.example.BackendApi.Crawler.impl;

import com.example.BackendApi.Crawler.IJobCrawler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Jsoup-based crawler for static web pages.
 * Suitable for websites that do not rely on JavaScript for content rendering.
 */
@Slf4j
@Component
public class JsoupJobCrawler implements IJobCrawler {

    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Override
    public String crawl(String url) {
        try {
            log.info("Crawling URL with Jsoup: {}", url);
            Document doc = Jsoup.connect(url)
                    .timeout(TIMEOUT_MS)
                    .userAgent(USER_AGENT)
                    .followRedirects(true)
                    .get();
            return doc.html();
        } catch (IOException e) {
            log.error("Failed to crawl URL with Jsoup: {}", url, e);
            throw new RuntimeException("Failed to crawl URL: " + url, e);
        }
    }
}
