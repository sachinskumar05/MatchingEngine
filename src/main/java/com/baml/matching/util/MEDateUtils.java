package com.baml.matching.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class MEDateUtils {

    private MEDateUtils(){
        throw new UnsupportedOperationException("Instantiation Restricted");
    }

    public static final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    public static long getCurrentMillis(){
        return LocalDateTime.now().atZone(ZoneId.of("GMT")).toInstant().toEpochMilli();
    }

}
