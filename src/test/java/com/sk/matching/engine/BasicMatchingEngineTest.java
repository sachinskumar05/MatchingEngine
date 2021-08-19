package com.sk.matching.engine;

import com.sk.matching.config.AppCfg;
import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.exchange.order.Order;
import com.sk.matching.exchange.orderbook.OrderBook;
import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.order.Trade;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.symbols.SymbolCache;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

@Log4j2
class BasicMatchingEngineTest {

    @Mock
    AppCfg appCfg;

    @InjectMocks
    BasicMatchingEngine basicMatchingEngine;

    SymbolCache symbolCache;
    private static final String BAC = "BAC";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        /**
         *        Path dataPathDir = Paths.get(appCfg.getDataDir());
         *         String symbolFilename = appCfg.getSymbolFile();
         *         String separator = appCfg.getSymbolFileContentSeparator();
         */
        Mockito.lenient().when(appCfg.getDataDir()).thenReturn("./data");
        Mockito.lenient().when(appCfg.getSymbolFile()).thenReturn("Symbols.csv");
        Mockito.lenient().when(appCfg.getSymbolFileContentSeparator()).thenReturn(",");
        Mockito.lenient().when(appCfg.getOrderPrefixBuy()).thenReturn("B");
        Mockito.lenient().when(appCfg.getOrderPrefixBuy()).thenReturn("S");
        symbolCache = new SymbolCache(appCfg);
        symbolCache.init();
    }

    @Test
    void testGetOrderBook() {
        Symbol symbol = new Symbol("name", Double.valueOf(0));
        OrderBook result = basicMatchingEngine.getOrderBook(symbol);
        log.info(" Order book result {}" , result);
        Assertions.assertNotNull(result);
    }

    @Test
    void testGetNoTrades() {
        List<Trade> result = basicMatchingEngine.getTrades(new Symbol("name", Double.valueOf(0)));
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void testGetTrades() {

        try {

            GenOrder.Builder eqOrderBuy = new GenOrder.Builder("CLOrdId1", BAC, Side.BUY, OrderType.LIMIT);
            eqOrderBuy.setPrice(30.00);
            eqOrderBuy.setQty(100);
            basicMatchingEngine.addOrder(eqOrderBuy.build());
            GenOrder.Builder eqOrderSell = new GenOrder.Builder("CLOrdId2", BAC, Side.SELL, OrderType.LIMIT);
            eqOrderSell.setPrice(30.00);
            eqOrderSell.setQty(100);
            basicMatchingEngine.addOrder(eqOrderSell.build());
            List<Trade> result = basicMatchingEngine.getTrades(new Symbol("name", Double.valueOf(0)));
            Assertions.assertTrue(result.isEmpty());
        } catch (SymbolNotSupportedException | OrderCreationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddOrder() {
        Assertions.assertThrows(NullPointerException.class, ()-> basicMatchingEngine.addOrder(null));
        Assertions.assertThrows(SymbolNotSupportedException.class,
                ()-> new GenOrder.Builder("CLOrdId1",  "FAKE", Side.BUY, OrderType.LIMIT));
        try {
            GenOrder.Builder eqOrder = new GenOrder.Builder("CLOrdId1", BAC, Side.BUY, OrderType.LIMIT);
            eqOrder.setPrice(30.00);
            eqOrder.setQty(100);
            basicMatchingEngine.addOrder(eqOrder.build());
        } catch (SymbolNotSupportedException | OrderCreationException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testAddLimitOrderWithoutPrice() {
        try {
            GenOrder.Builder eqOrder = new GenOrder.Builder("CLOrdId1", BAC, Side.BUY, OrderType.LIMIT);
            Assertions.assertThrows(OrderCreationException.class, eqOrder::build);
            Order order = eqOrder.build();
            Assertions.assertThrows(OrderCreationException.class, ()-> basicMatchingEngine.addOrder(order));
        } catch (SymbolNotSupportedException | OrderCreationException e) {
            log.error("Failed Test case ", e);
        }
    }

    @Test
    void testAddLimitOrderWithoutQty() {
        try {
            GenOrder.Builder ordBuilder = new GenOrder.Builder("CLOrdId1", BAC, Side.BUY, OrderType.LIMIT);
            ordBuilder.setPrice(30.00);
//            Assertions.assertThrows(OrderCreationException.class, ()-> basicMatchingEngine.addOrder(ordBuilder.build()));
            ordBuilder.setQty(100);
            Assertions.assertDoesNotThrow(()-> basicMatchingEngine.addOrder(ordBuilder.build()));
        } catch (SymbolNotSupportedException e) {
            log.error("Failed Test case ", e);
        }
    }

    @Test
    void testCancelOrder() {
        Assertions.assertThrows(UnsupportedOperationException.class, ()-> basicMatchingEngine.cancelOrder(null));
    }

    @Test
    void testAmendOrder() {
        Assertions.assertThrows(UnsupportedOperationException.class, ()-> basicMatchingEngine.amendOrder(null) );
    }
}

