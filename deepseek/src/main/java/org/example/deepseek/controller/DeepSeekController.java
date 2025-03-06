package org.example.deepseek.controller;

import org.example.deepseek.config.DeepSeekConfig;
import org.example.deepseek.entity.DeepSeekVO;
import org.example.deepseek.service.DeepSeekService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deepseek")
public class DeepSeekController {

    @Autowired
    DeepSeekConfig config;

    @Autowired
    private DeepSeekService deepSeekService;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody DeepSeekVO deepSeekVO) {
        String result = deepSeekService.callDeepSeekApi(deepSeekVO.getText());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/translate")
    public ResponseEntity<String> translate(@RequestBody DeepSeekVO deepSeekVO) {
        String result = deepSeekService.translate(deepSeekVO.getText(), deepSeekVO.getSourceLang(), deepSeekVO.getTargetLang());
        return ResponseEntity.ok(result);
    }
}