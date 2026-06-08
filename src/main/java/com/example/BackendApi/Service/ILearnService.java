package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.AudioRecognizeVo;
import org.springframework.web.multipart.MultipartFile;

public interface ILearnService {
    AudioRecognizeVo processAudio(MultipartFile file, String lang, String mode);
}