package com.sk.matching.util;

import lombok.extern.log4j.Log4j2;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public interface DateUtils {


    DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

    static long getCurrentNanos(){
        ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.of("GMT"));
        Instant instant = zdt.toInstant();
        return instant.getEpochSecond() * 100000000L + instant.getNano();
    }


}
