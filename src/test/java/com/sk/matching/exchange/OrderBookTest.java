package com.sk.matching.exchange;

import com.sk.matching.config.AppCfg;
import com.sk.matching.exchange.crossing.CrossingProcessor;
import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.orderbook.OrderBook;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.symbols.EquitySymbolCache;
import com.sk.matching.types.Side;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import static org.mockito.Mockito.*;

class OrderBookTest {
    @Mock
    Map<Symbol, OrderBook> orderBookCache;
    @Mock
    DecimalFormat DECIMAL_FORMAT;
    @Mock
    DecimalFormat DECIMAL_TO_INT_FORMAT;
    @Mock
    AtomicLong currentTradeId;
    @Mock
    ExecutorService executorForCrossing;
    @Mock
    CrossingProcessor crossingProcessor;
    @Mock
    ReadWriteLock readWriteLock;
    @Mock
    Lock writeLock;
    @Mock
    Symbol symbol;
    @Mock
    SortedMap<Double, List<GenOrder>> fxBidOrderSortedMap;
    @Mock
    SortedMap<Double, List<GenOrder>> fxAskOrderSortedMap;
    @Mock
    Map<Long, GenOrder> orderHistory;
    @Mock
    AppCfg appCfg;
    @InjectMocks
    OrderBook orderBook;

    EquitySymbolCache equitySymbolCache;
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
    }

    @Test
    void testGetBook() {
        OrderBook result = OrderBook.getBook(new Symbol("name", Double.valueOf(0)));
        Assertions.assertEquals(null, result);
    }

    @Test
    void testGenerateTradeId() {
        long result = orderBook.generateTradeId();
        Assertions.assertEquals(0L, result);
    }

    @Test
    void testSetOrder() {
        boolean result = orderBook.setOrder(null);
        Assertions.assertEquals(true, result);
    }

    @Test
    void testGetBestOppositeOrderList() {
        List<GenOrder> result = orderBook.getBestOppositeOrderList(Side.BUY);
        Assertions.assertEquals(Arrays.<GenOrder>asList(null), result);
    }

    @Test
    void testGetBestOppositePrice() {
        double result = orderBook.getBestOppositePrice(null, Side.BUY);
        Assertions.assertEquals(0d, result);
    }

    @Test
    void testGetBestBid() {
        List<GenOrder> result = orderBook.getBestBid();
        Assertions.assertEquals(Arrays.<GenOrder>asList(null), result);
    }

    @Test
    void testGetBestBidPrice() {
        double result = orderBook.getBestBidPrice();
        Assertions.assertEquals(0d, result);
    }

    @Test
    void testRemoveBid() {
        boolean result = orderBook.removeBid(null);
        Assertions.assertEquals(true, result);
    }

    @Test
    void testGetBestAsk() {
        List<GenOrder> result = orderBook.getBestAsk();
        Assertions.assertEquals(Arrays.<GenOrder>asList(null), result);
    }

    @Test
    void testGetBestAskPrice() {
        double result = orderBook.getBestAskPrice();
        Assertions.assertEquals(0d, result);
    }

    @Test
    void testRemoveAsk() {
        boolean result = orderBook.removeAsk(null);
        Assertions.assertEquals(true, result);
    }

    @Test
    void testProcessOrder() {
        orderBook.processOrder(null);
    }

    @Test
    void testRemoveOrder() {
        boolean result = orderBook.removeOrder(null);
        Assertions.assertEquals(true, result);
    }

    @Test
    void testGetOrderHistory() {
        Collection<GenOrder> result = orderBook.getOrderHistory();
        Assertions.assertEquals(Arrays.<GenOrder>asList(null), result);
    }

    @Test
    void testGetOrder() {
        GenOrder result = orderBook.getOrder(Long.valueOf(1));
        Assertions.assertNull(result);

        result = orderBook.getOrder(Long.valueOf(1));
        Assertions.assertNull(result);
    }

    @Test
    void testEquals() {
        when(symbol.equals(any())).thenReturn(true);

        boolean result = orderBook.equals("o");
        Assertions.assertEquals(true, result);
    }

    @Test
    void testHashCode() {
        when(symbol.hashCode()).thenReturn(0);

        int result = orderBook.hashCode();
        Assertions.assertEquals(0, result);
    }

    @Test
    void testToString() {
        when(symbol.hashCode()).thenReturn(0);

        String result = orderBook.toString();
        Assertions.assertEquals("replaceMeWithExpectedResult", result);
    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme