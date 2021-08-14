package com.sk.matching.client;

import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.order.Order;

import java.util.List;

public interface Client {
    void submitOrder(Order order) throws OrderCreationException;
    void replaceOrder(Order order) throws OrderCreationException;
    void cancelOrder(Order order) throws OrderCreationException;
    List<GenOrder> getClientOrders();
}
