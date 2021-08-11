package com.sk.matching.exchange;

import com.sk.matching.exchange.order.EQOrder;

import java.util.Collection;

public interface OrderBook {
    Collection<EQOrder> getOrderHistory();
    EQOrder getOrder(Long orderId);
    boolean setOrder(EQOrder eqOrder);
    boolean removeOrder(EQOrder eqOrder);
}
