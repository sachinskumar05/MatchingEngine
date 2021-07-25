package com.baml.matching.exchange.order;

import com.baml.matching.exception.OrderCreationException;
import com.baml.matching.pool.ObjectFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EQOrderFactory implements ObjectFactory<EQOrder> {
    EQOrderFactory() {}

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

    @Override
    public EQOrder create() {
        log.warn("EQOrder object pooling is not yet supported, work is in progress");
        return null;
    }

    @Override
    public void destroy(EQOrder eqOrder) {

    }

    @Override
    public boolean validate(EQOrder eqOrder) {
        return false;
    }

}
