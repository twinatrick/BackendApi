package com.example.BackendArchitectureLab.Service.impl;

import com.example.BackendArchitectureLab.Dto.Vo.AudioRecognizeVo;
import com.example.BackendArchitectureLab.Service.ILearnService;
import com.example.BackendArchitectureLab.Service.Nlp.PhoneticConvertService;
import com.example.BackendArchitectureLab.Service.Onnx.WhisperOnnxService;
import com.example.BackendArchitectureLab.Util.AudioProcessUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LearnServiceImpl implements ILearnService {

    private final AudioProcessUtil audioProcessUtil;
    private final WhisperOnnxService whisperOnnxService;
    private final PhoneticConvertService phoneticConvertService;

    public LearnServiceImpl(AudioProcessUtil audioProcessUtil,
                            WhisperOnnxService whisperOnnxService,
                            PhoneticConvertService phoneticConvertService) {
        this.audioProcessUtil = audioProcessUtil;
        this.whisperOnnxService = whisperOnnxService;
        this.phoneticConvertService = phoneticConvertService;
    }

    @Override
    public AudioRecognizeVo processAudio(MultipartFile file, String lang, String mode) {
        AudioRecognizeVo vo = new AudioRecognizeVo();
        
        try {
            // 1. 處理音訊轉檔 (16kHz PCM Float Array)
            float[] audioData = audioProcessUtil.convertTo16kMonoFloatArray(file);

            // 2. Whisper ONNX 推論出文字
            String text = whisperOnnxService.transcribe(audioData, lang);
            vo.setText(text);

            // 3. 轉換拼音/注音/羅馬音
            String phonetic = phoneticConvertService.convert(text, mode, lang);
            vo.setPhonetic(phonetic);

        } catch (Exception e) {
            e.printStackTrace();
            vo.setText("音訊處理失敗: " + e.getMessage());
        }

        return vo;
    }
}
