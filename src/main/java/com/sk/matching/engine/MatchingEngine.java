package com.sk.matching.engine;

import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exchange.orderbook.OrderBook;
import com.sk.matching.exchange.order.Order;
import com.sk.matching.exchange.order.Trade;
import com.sk.matching.symbols.Symbol;

import java.util.List;

public interface MatchingEngine {

    OrderBook getOrderBook(Symbol symbol);
    List<Trade> getTrades(Symbol symbol);
    void addOrder(Order order) throws OrderCreationException;
    void cancelOrder(Order order);
    void amendOrder(Order order);

}
