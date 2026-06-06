package com.example.BackendApi.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Google Gemini API.
 */
public interface IGeminiService {

    /**
     * Analyze raw HTML content from a company's career page and extract job listings.
     *
     * @param companyName the name of the company
     * @param htmlContent the raw HTML content
     * @return a list of structured job posting data (as Maps with keys matching JobPosting fields)
     */
    List<Map<String, String>> analyzeJobPostings(String companyName, String htmlContent);
}
