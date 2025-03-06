package org.example.deepseek.entity;

import lombok.Data;
import lombok.Getter;

@Data
public class DeepSeekVO {

    /**
     * 文本
     */
    private String text;

    /**
     * 来源语言
     ISO 639-1 标准的语言代码
     语言	代码
         英语	en
         中文（简体）	zh
         中文（繁体）	zh-TW 或 zh-HK
         日语	ja
         韩语	ko
         法语	fr
         德语	de
         西班牙语	es
         俄语	ru
     */
    private String sourceLang;

    /**
     * 目标语言
     */
    private String targetLang;

}
