package com.baml.matching.engine;

import com.baml.matching.exception.OrderCreationException;
import com.baml.matching.exchange.OrderBook;
import com.baml.matching.exchange.order.Order;
import com.baml.matching.exchange.order.Trade;
import com.baml.matching.symbols.Symbol;

import java.util.List;

public interface MatchingEngine {

    OrderBook getOrderBook(Symbol symbol);
    List<Trade> getTrades(Symbol symbol);
    void addOrder(Order order) throws OrderCreationException;
    void cancelOrder(Order order);
    void amendOrder(Order order);

}
