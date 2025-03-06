package org.example.deepseek.controller;

import org.example.deepseek.config.DeepSeekConfig;
import org.example.deepseek.entity.ApiResponse;
import org.example.deepseek.entity.DeepSeekVO;
import org.example.deepseek.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/deepseek")
public class DeepSeekController {

    @Autowired
    DeepSeekConfig config;

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public ApiResponse<String> chat(@RequestBody DeepSeekVO deepSeekVO) {
        String result = deepSeekService.callDeepSeekApi(deepSeekVO.getText());
        return ApiResponse.success(result);
    }

    @PostMapping("/translate")
    public ApiResponse<String> translate(@RequestBody DeepSeekVO deepSeekVO) {
        String result = deepSeekService.translate(deepSeekVO.getText(), deepSeekVO.getSourceLang(), deepSeekVO.getTargetLang());
        return ApiResponse.success(result);
    }

}