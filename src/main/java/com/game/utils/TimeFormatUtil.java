package com.game.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author yanfang
 * &#064;date  2024/6/11 19:43
 * @version 1.0
 */

public class TimeFormatUtil {
    public static String getTime() {
        String format = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        //System.out.println(format);
        return format;
    }
}
