package com.example.BackendApi.Service;

import java.util.List;
import java.util.Map;

public interface IAiService {

    List<Map<String, String>> analyzeJobPostings(String companyName, String htmlContent);
}
