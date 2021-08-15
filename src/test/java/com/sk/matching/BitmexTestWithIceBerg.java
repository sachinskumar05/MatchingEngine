package com.sk.matching;

import com.sk.matching.client.ClientWorker;
import com.sk.matching.config.AppCfg;
import com.sk.matching.engine.BasicMatchingEngine;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.exchange.orderbook.OrderBook;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.symbols.SymbolCache;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import com.sk.matching.util.ThreadUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SpringBootTest
class BitmexTestWithIceBerg {

    @Mock
    AppCfg appCfg;

    @InjectMocks
    MatchingEngineApplication matchingEngineApplication;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private static final String symbolStr = "BAC";

    private SymbolCache symbolCache;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.lenient().when(appCfg.getDataDir()).thenReturn("./data");
        Mockito.lenient().when(appCfg.getSymbolFile()).thenReturn("Symbols.csv");
        Mockito.lenient().when(appCfg.getSymbolFileContentSeparator()).thenReturn(",");
        Mockito.lenient().when(appCfg.getOrderPrefixBuy()).thenReturn("B");
        Mockito.lenient().when(appCfg.getOrderPrefixBuy()).thenReturn("S");
        symbolCache = new SymbolCache(appCfg);
        symbolCache.init();
    }

    @AfterAll
    static void tearDown() {
        Symbol symbol = null;
        OrderBook orderBook = null;
        try {
            symbol = SymbolCache.get(symbolStr);
            orderBook = OrderBook.getBook(symbol);
            orderBook.reset();
        } catch (SymbolNotSupportedException e) {
            log.error("Failed to create order for {}", symbolStr, e );
        }
    }

    @Test
    void testMain() {
        String testInputFile = "./input-test-data/test_ice2.txt";
        List<String[]> lineListArr  = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(testInputFile));
            log.info("Reading Trade Input file {}", testInputFile);
            for(String line: lines) {
                log.info("Loaded Trade Line {} " , line);
                lineListArr.add(line.split(","));
            }
        } catch (IOException e) {
            log.error("Failed Test Case " , e);
            return;
        }

        BasicMatchingEngine basicMatchingEngine = BasicMatchingEngine.getInstance();
        ClientWorker clientA = new ClientWorker();

        final String BAC = "BAC";
        log.info("Trading simulation will start on {} using input file {}", BAC, testInputFile);
        for (String[] attributes : lineListArr ) {
            log.info("Creating order using file input {}", Arrays.toString(attributes));
            String clOrdId = attributes[0];
            Side side = Side.valueOf(attributes[1].charAt(0));
            Double px = Double.valueOf(attributes[2]);
            Double qty = Double.valueOf(attributes[3]);
            Double visibleQty = Double.NaN;
            if ( attributes.length > 4) {
                visibleQty = Double.valueOf(attributes[4]);
            }
            Double finalVisibleQty = visibleQty;
            executorService.submit(()-> clientA.createAndSubmitOrder(BAC,
                    side,
                    px,
                    qty,
                    OrderType.LIMIT,
                    clOrdId, finalVisibleQty));
        }

        Symbol symbol = null;

        try {
            symbol = SymbolCache.get(BAC);
        } catch (SymbolNotSupportedException e) {
            log.error("Failed to create order for {}", BAC, e );
        }

        ThreadUtils.pause(500);
        log.info( "Order {}" ,  basicMatchingEngine.getOrderBook(symbol));

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

    }
}

