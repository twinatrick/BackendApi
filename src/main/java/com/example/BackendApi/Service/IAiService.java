package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.AiJobPostingDto;
import java.util.List;

public interface IAiService {

    List<AiJobPostingDto> analyzeJobPostings(String companyName, String htmlContent);
}
