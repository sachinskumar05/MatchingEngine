package com.sk.matching.util;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Log4j2
class FileUtilsTest {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetLastLineOf() {
        Path testFilePath = Paths.get("./temp/testFileUtils.txt");
        String expectedLineStr = "THIS is Test Last Line";
        try {
            Path parentDir = testFilePath.getParent();
            if ( !Files.exists(parentDir) ) {
                Files.createDirectories(parentDir);
            }
            Files.write(testFilePath, expectedLineStr.getBytes(StandardCharsets.UTF_8));
            String result = FileUtils.getLastLineOf(testFilePath);
            log.info("Result {}", result);
            Assertions.assertEquals(expectedLineStr, result);
            Files.deleteIfExists(testFilePath);
        } catch (IOException e) {
            log.error("Failed to execute test case ", e);
        }
    }



}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme