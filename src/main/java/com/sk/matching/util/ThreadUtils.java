package com.sk.matching.util;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Log4j2
public class ThreadUtils {

    private ThreadUtils(){ throw new UnsupportedOperationException("Instantiation Restricted"); }


    public static void pause(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
            log.error("Interrupted while sleep ", e);
            Thread.currentThread().interrupt();
        }
    }

}
