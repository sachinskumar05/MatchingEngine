package com.sk.matching.util;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.stream.Stream;

@Log4j2
public class MEFileUtils {

    private MEFileUtils(){throw new UnsupportedOperationException("Instantiation Restricted");}

    public static String getLastLineOf(Path filePath) {
        try (Stream<String> stream = Files.lines(filePath) ) {
            return stream.reduce( (f, s)-> s ).orElse(null);
        } catch (IOException e) {
            log.error(" Failed while reading file ");
        }
        return null;
    }



}
