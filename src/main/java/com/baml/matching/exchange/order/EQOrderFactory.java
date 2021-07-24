package com.baml.matching.exchange.order;

import com.baml.matching.exception.OrderCreationException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EQOrderFactory implements OrderFactory {
    private EQOrderFactory() {}

    private static final EQOrderFactory INSTANCE = new EQOrderFactory();

    public static EQOrderFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("EQOrderFactory is not cloneable");
    }

    public EQOrder createOrder(EQOrder.Builder builder) throws OrderCreationException {
        return builder.build();
    }

}
