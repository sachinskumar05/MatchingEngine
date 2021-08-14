package com.sk.matching.exchange;

import com.sk.matching.config.AppCfg;
import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.orderbook.OrderBook;
import com.sk.matching.symbols.EquitySymbolCache;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;

@Log4j2
class OrderBookTest {

    @Mock
    AppCfg appCfg;

    @InjectMocks
    OrderBook orderBook;

    private static String symbolStr = "BAC";

    GenOrder buyOrder = null;
    GenOrder sellOrder = null;
    {
        GenOrder.Builder buyOrdBuilder = null;
        try {
            buyOrdBuilder = new GenOrder.Builder("CLOrdId1", symbolStr, Side.BUY, OrderType.LIMIT);
            buyOrdBuilder.price=30.00;
            buyOrdBuilder.qty=100;
            buyOrder = buyOrdBuilder.build();
        } catch (SymbolNotSupportedException | OrderCreationException e) {
            log.error("Failed test case ", e);
        }

        GenOrder.Builder sellOrdBuilder = null;
        try {
            sellOrdBuilder = new GenOrder.Builder("CLOrdId1", symbolStr, Side.SELL, OrderType.LIMIT);
            sellOrdBuilder.price=30.00;
            sellOrdBuilder.qty=100;
            sellOrder = sellOrdBuilder.build();
        } catch (SymbolNotSupportedException | OrderCreationException e) {
            log.error("Failed test case ", e);
        }

    }

    private EquitySymbolCache equitySymbolCache;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Mockito.lenient().when(appCfg.getDataDir()).thenReturn("./data");
        Mockito.lenient().when(appCfg.getSymbolFile()).thenReturn("Symbols.csv");
        Mockito.lenient().when(appCfg.getSymbolFileContentSeparator()).thenReturn(",");
        Mockito.lenient().when(appCfg.getOrderPrefixBuy()).thenReturn("B");
        Mockito.lenient().when(appCfg.getOrderPrefixBuy()).thenReturn("S");
        equitySymbolCache = new EquitySymbolCache(appCfg);
        equitySymbolCache.init();
        try {
            orderBook = OrderBook.getBook(EquitySymbolCache.get(symbolStr));
        } catch (SymbolNotSupportedException e) {
            log.error("Failed test case as symbol not found {}", symbolStr, e);
        }
    }

    @AfterEach
    void tearDown() {
        try {
            orderBook = OrderBook.getBook(EquitySymbolCache.get(symbolStr));
            orderBook.removeOrder(buyOrder);
            orderBook.removeOrder(sellOrder);
        } catch (SymbolNotSupportedException e) {
            log.error("Failed test case as symbol not found {}", symbolStr, e);
        }
    }

    @Test
    void testGetBook() {
        OrderBook result = null;
        try {
            result = OrderBook.getBook(EquitySymbolCache.get(symbolStr));
        } catch (SymbolNotSupportedException e) {
            log.error("Failed to execute test case ", e);
        }
        Assertions.assertNotNull(result);
        Assertions.assertEquals(symbolStr, result.getSymbol().getName());
        Assertions.assertTrue(result.getAskOrderSortedMap().isEmpty());
        Assertions.assertTrue(result.getBidOrderSortedMap().isEmpty());
    }

    @Test
    void testGenerateTradeId() {
        long result = orderBook.generateTradeId();
        Assertions.assertTrue(result > 162898250000000000L);
    }

    @Test
    void testSetOrder() {
        Assertions.assertThrows(NullPointerException.class, ()->orderBook.setOrder(null));
        Assertions.assertTrue(orderBook.setOrder(buyOrder));
    }

    @Test
    void testGetBestOppositeOrderList() {
        List<GenOrder> result = orderBook.getBestOppositeOrderList(Side.BUY);
        Assertions.assertTrue( result.isEmpty() );
    }

    @Test
    void testGetBestOppositePrice() {
        orderBook.setOrder(buyOrder);
        orderBook.setOrder(sellOrder);
        double result = orderBook.getBestOppositePrice(buyOrder, Side.BUY);
        Assertions.assertEquals(sellOrder.getOrdPx(), result);
    }

    @Test
    void testGetBestBid() {
        orderBook.setOrder(buyOrder);
        List<GenOrder> result = orderBook.getBestBid();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(buyOrder, result.get(0));
    }

    @Test
    void testGetBestBidPrice() {
        orderBook.setOrder(buyOrder);
        double result = orderBook.getBestBidPrice();
        Assertions.assertEquals(buyOrder.getOrdPx(), result);
    }

    @Test
    void testRemoveBid() {
        Assertions.assertThrows(NullPointerException.class, ()-> orderBook.removeBid(null) );
        orderBook.setOrder(buyOrder);
        boolean result = orderBook.removeBid(buyOrder);
        Assertions.assertEquals(true, result);
    }

    @Test
    void testGetBestAsk() {
        orderBook.setOrder(sellOrder);
        List<GenOrder> result = orderBook.getBestAsk();
        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(Side.SELL, result.get(0).getSide());
    }

    @Test
    void testGetBestAskPrice() {
        double result = orderBook.getBestAskPrice();
        Assertions.assertEquals(Double.NaN, result);
        orderBook.setOrder(sellOrder);
        result = orderBook.getBestAskPrice();
        Assertions.assertEquals(buyOrder.getOrdPx(), result);
    }

    @Test
    void testRemoveAsk() {
        Assertions.assertThrows(NullPointerException.class, ()->orderBook.removeAsk(null));
        orderBook.setOrder(sellOrder);
        boolean result = orderBook.removeAsk(sellOrder);
        Assertions.assertTrue(result);
    }

    @Test
    void testProcessOrder() {
        orderBook.processOrder(null);
    }

    @Test
    void testRemoveOrder() {
        Assertions.assertThrows(NullPointerException.class, ()-> orderBook.removeOrder(null));
        orderBook.setOrder(buyOrder);
        boolean result = orderBook.removeOrder(buyOrder);
        Assertions.assertEquals(true, result);
    }

    @Test
    void testGetOrderHistory() {
        Collection<GenOrder> result = orderBook.getOrderHistory();
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void testGetOrder() {
        GenOrder result = orderBook.getOrder(Long.valueOf(1));
        Assertions.assertNotNull(result);

        result = orderBook.getOrder(Long.valueOf(100));
        Assertions.assertNull(result);
    }

    @Test
    void testToString() {
        String result = orderBook.toString();
        Assertions.assertTrue( result.contains(symbolStr));
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme