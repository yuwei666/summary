package org.example.deepseek.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.deepseek.config.DeepSeekConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeepSeekService {

    private final RestTemplate restTemplate;
    private final DeepSeekConfig deepSeekConfig;
    private final ObjectMapper objectMapper;

    public DeepSeekService(RestTemplate restTemplate, DeepSeekConfig deepSeekConfig, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.deepSeekConfig = deepSeekConfig;
        this.objectMapper = objectMapper;
    }

    public String callDeepSeekApi(String prompt) {
        String url = deepSeekConfig.getApiUrl() + "/chat/completions"; // 示例 API 路径

        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());

        // 设置请求体
        // 2. 构建请求体（JSON）
        String requestBody = String.format("""
                    {
                       "messages": [
                         {
                           "content": "%s",
                           "role": "system"
                         }
                       ],
                       "model": "deepseek-chat",
                       "frequency_penalty": 0,
                       "max_tokens": 40,
                       "presence_penalty": 0,
                       "response_format": {
                         "type": "text"
                       },
                       "stop": null,
                       "stream": false,
                       "stream_options": null,
                       "temperature": 1,
                       "top_p": 1,
                       "tools": null,
                       "tool_choice": "none",
                       "logprobs": false,
                       "top_logprobs": null
                    }
                """, prompt);

        // 发送请求
        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        // 返回响应
        return response.getBody();
    }

    public String translate_copy(String text, String sourceLang, String targetLang) {

        String url = deepSeekConfig.getApiUrl() + "/chat/completions"; // 示例 API 路径

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestBody = String.format("""
                {
                    messages: [
                                {
                                    "content": "你是一个多语言翻译助手，能够准确地将文本从一种语言翻译到另一种语言。请根据提供的源语言（source）、目标语言（target）和待翻译的文本（text），生成准确的翻译结果。
                                     输入格式 - source: 当前文本的语言代码（例如：zh-CN 表示简体中文，en 表示英语，es 表示西班牙语等） - target: 目标语言代码（例如：en 表示英语，fr 表示法语，de 表示德语等） - text: 需要翻译的文本  输出格式：  - 返回一个JSON对象，包含以下字段： - result: 翻译后的文本。
                                     示例输入：{"source": "zh-CN","target": "en","text": "你好，世界！"} 示例输出：{"result": "Hello, world!"}
                                     请根据以下输入生成翻译结果： {"source": "%s","target": "%s","text": "%s"}",
                                     "role": "system"
                                }
                            ],
                    "model": "deepseek-chat",
                    "frequency_penalty": 0,
                    "max_tokens": 40,
                    "presence_penalty": 0,
                    "response_format": {
                      "type": "text"
                    },
                    "stop": null,
                    "stream": false,
                    "stream_options": null,
                    "temperature": 1.3,
                    "top_p": 1,
                    "tools": null,
                    "tool_choice": "none",
                    "logprobs": false,
                    "top_logprobs": null
                }
        """, sourceLang, targetLang, text);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new RuntimeException("Translation failed: " + response.getStatusCode());
        }
    }

    public String translate(String text, String sourceLang, String targetLang) {
        String url = deepSeekConfig.getApiUrl() + "/chat/completions";

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        requestBody.put("frequency_penalty", 0);
        requestBody.put("max_tokens", 40);
        requestBody.put("presence_penalty", 0);
        requestBody.put("temperature", 1.3);
        requestBody.put("top_p", 1);

        Map<String, String> message = new HashMap<>();
        message.put("role", "system");
        message.put("content", String.format("""
                你是一个多语言翻译助手，能够准确地将文本从一种语言翻译到另一种语言。
                请根据提供的源语言（source）、目标语言（target）和待翻译的文本（text），生成准确的翻译结果。
                输入格式：
                - source: 当前文本的语言代码（例如：zh-CN 表示简体中文，en 表示英语，es 表示西班牙语等）
                - target: 目标语言代码（例如：en 表示英语，fr 表示法语，de 表示德语等）
                - text: 需要翻译的文本
                输出格式：
                - 返回一个JSON对象，包含以下字段：
                - result: 翻译后的文本
                示例输入：{"source": "zh-CN","target": "en","text": "你好，世界！"}
                示例输出：{"result": "Hello, world!"}
                请根据以下输入生成翻译结果： {"source": "%s","target": "%s","text": "%s"}
                """, sourceLang, targetLang, text));

        requestBody.put("messages", new Map[]{message});

        try {
            // 将请求体转换为 JSON 字符串
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // 发送请求
            HttpEntity<String> request = new HttpEntity<>(requestBodyJson, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // 处理响应
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("Translation failed: " + response.getStatusCode());
            }
        } catch (RestClientException e) {
            throw new RuntimeException("API call failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }
}