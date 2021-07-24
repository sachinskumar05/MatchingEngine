package com.baml.matching.client;

import com.baml.matching.exception.OrderCreationException;
import com.baml.matching.exchange.order.EQOrder;
import com.baml.matching.exchange.order.Order;

import java.util.List;

public interface Client {
    void submitOrder(Order order) throws OrderCreationException;
    void replaceOrder(Order order) throws OrderCreationException;
    void cancelOrder(Order order) throws OrderCreationException;
    List<EQOrder> getClientOrders();
}
