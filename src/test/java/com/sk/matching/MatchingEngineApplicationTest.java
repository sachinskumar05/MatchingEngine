package com.sk.matching;

import com.sk.matching.client.ClientWorker;
import com.sk.matching.config.AppCfg;
import com.sk.matching.engine.BasicMatchingEngine;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.symbols.SymbolCache;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import com.sk.matching.util.ThreadUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@SpringBootTest
class MatchingEngineApplicationTest {

    @Mock
    AppCfg appCfg;

    @InjectMocks
    MatchingEngineApplication matchingEngineApplication;

    private static final ExecutorService executorService = Executors.newFixedThreadPool(10);

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

    @Test
    void testMain() {
        MatchingEngineApplication.main(new String[]{"args"});
        BasicMatchingEngine basicMatchingEngine = BasicMatchingEngine.getInstance();
        ClientWorker clientA = new ClientWorker();
        ClientWorker clientB = new ClientWorker();


        final String BAC = "BAC";
        log.info("Trading simulation will start on {}", BAC);
        executorService.submit(()-> clientA.createAndSubmitOrder(BAC, Side.SELL, 20.30, 100, OrderType.LIMIT));
//        ThreadUtils.pause(1000);
        executorService.submit(()-> clientA.createAndSubmitOrder(BAC, Side.SELL, 20.25, 100, OrderType.LIMIT));
//        ThreadUtils.pause(1000);
        executorService.submit(()-> clientA.createAndSubmitOrder(BAC, Side.SELL, 20.30, 200, OrderType.LIMIT));

//        ThreadUtils.pause(1000);
        executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.15, 100, OrderType.LIMIT));
//        ThreadUtils.pause(1000);
        executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.20, 200, OrderType.LIMIT));
//        ThreadUtils.pause(1000);
        executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.15, 200, OrderType.LIMIT));

        Symbol symbolBAC = null;

        try {
            symbolBAC = SymbolCache.get(BAC);
        } catch (SymbolNotSupportedException e) {
            log.error("Failed to create order for {}", BAC, e );
        }

        ThreadUtils.pause(1000);
        log.info( "Order {}" ,  basicMatchingEngine.getOrderBook(symbolBAC));

        executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.25, 100, OrderType.LIMIT));
        executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.30, 100, OrderType.LIMIT));
        executorService.submit(()-> clientB.createAndSubmitOrder(BAC, Side.BUY, 20.30, 50, OrderType.LIMIT));

        ThreadUtils.pause(1000);
        log.info( "Order {}" ,  basicMatchingEngine.getOrderBook(symbolBAC));

        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown));

    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme