package com.sk.matching.exchange.order;

import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.pool.ObjectFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class OrderFactory implements ObjectFactory<GenOrder> {
    OrderFactory() {}

    private static final OrderFactory INSTANCE = new OrderFactory();

    public static OrderFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("EQOrderFactory is not cloneable");
    }

    public GenOrder createOrder(GenOrder.Builder builder) throws OrderCreationException {
        return builder.build();
    }

    @Override
    public GenOrder create() {
        log.warn("EQOrder object pooling is not yet supported, work is in progress");
        return null;
    }

    @Override
    public void destroy(GenOrder genOrder) {

    }

    @Override
    public boolean validate(GenOrder genOrder) {
        return false;
    }

}
