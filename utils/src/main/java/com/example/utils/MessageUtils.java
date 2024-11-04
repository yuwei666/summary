package com.example.utils;

import cn.hutool.extra.spring.SpringUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * 获取i18n资源文件
 * 18n（其来源是英文单词 internationalization的首末字符i和n，18为中间的字符数）
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtils {
    private static final MessageSource MESSAGE_SOURCE = SpringUtil.getBean(MessageSource.class);

    /**
     * todo 这块不是很懂，后续补充吧
     * @param code
     * @param args
     * @return
     */
    public static String message(String code, Object... args) {
        return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
    }

}
