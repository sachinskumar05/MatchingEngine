package com.sk.matching.util;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.stream.Stream;

public interface FileUtils {


    static String getLastLineOf(Path filePath) {
        try (Stream<String> stream = Files.lines(filePath) ) {
            return stream.reduce( (f, s)-> s ).orElse(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



}
