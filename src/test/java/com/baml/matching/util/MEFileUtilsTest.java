package com.baml.matching.util;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.*;

@Log4j2
class MEFileUtilsTest {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetLastLineOf() {
        Path testFilePath = Paths.get("./temp/testFileUtils.txt");
        String lastLine = "THIS is Test Last Line";
        try {
            Files.write(testFilePath, lastLine.getBytes(StandardCharsets.UTF_8));
            String result = MEFileUtils.getLastLineOf(testFilePath);
            log.info("Result {}", result);
            Assertions.assertEquals(lastLine, result);
            Files.deleteIfExists(testFilePath);
        } catch (IOException e) {
            log.error("Failed to execute test case ", e);
        }
    }



}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme