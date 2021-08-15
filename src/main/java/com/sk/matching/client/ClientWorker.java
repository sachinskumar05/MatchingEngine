package com.sk.matching.client;

import com.sk.matching.engine.BasicMatchingEngine;
import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.order.Order;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import com.sk.matching.util.DateUtils;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.sk.matching.util.AppConstants.*;

@Log4j2
public class ClientWorker implements Client {

    private static final BasicMatchingEngine EQUITY_MATCHING_ENGINE = BasicMatchingEngine.getInstance();

    private static final AtomicInteger incrementallyUnique = new AtomicInteger(0);

    private final List<GenOrder> genOrderList = new ArrayList<>();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public  final Lock writeLock = readWriteLock.writeLock();

    public void createAndSubmitOrder(String symbol, Side side, double px, double qty, OrderType ot, String clOrdId, Double visibleQty) {
        GenOrder.Builder ordBuilder = null;
        try {
            ordBuilder = new GenOrder.Builder(clOrdId, symbol, side, ot);
            GenOrder genOrder = ordBuilder.with(builder -> {
                builder.price = px ;
                builder.qty = qty;
                builder.currency = USD;
                builder.visibleQty = visibleQty;
            }) .build();
            genOrderList.add(genOrder);
            submitOrder(genOrder);
        } catch (OrderCreationException | SymbolNotSupportedException e) {
            log.error("Failed to build EQOrder using its builder {} ", ordBuilder, e);
        }
    }

    public void createAndSubmitOrder(String symbol, Side side, double px, double qty, OrderType ot, String clOrdId) {
        this.createAndSubmitOrder(symbol, side, px, qty, ot, clOrdId, Double.NaN);
    }

    public void createAndSubmitOrder(String symbol, Side side, double px, double qty, OrderType ot) {
            String clOrdId = String.format("%s%sC%s",
                    side, DateUtils.getCurrentNanos(), incrementallyUnique.getAndIncrement());
        this.createAndSubmitOrder( symbol,  side,  px,  qty, ot, clOrdId );
    }

    /**
     * Returns copy of orders to save the original copy from external mutations
     * @return
     */
    @Override
    public List<GenOrder> getClientOrders() {
        final List<GenOrder> orders = new ArrayList<>();
        for( GenOrder eqOrd: genOrderList) {
            orders.add(eqOrd.copy());
        }
        return orders;
    }

    @Override
    public void submitOrder(Order order) throws OrderCreationException {
        GenOrder genOrder = (GenOrder) order;
        log.info("Sending Client Order Id {}, Side {}, px {}, qty {}",
                genOrder::getClientOrderId, genOrder::getSide,
                genOrder::getOrdPx, genOrder::getOrdQty);
        EQUITY_MATCHING_ENGINE.addOrder(genOrder);
    }

    @Override
    public void replaceOrder(Order order) {
        throw new UnsupportedOperationException("Service is Not yet implemented");
    }

    @Override
    public void cancelOrder(Order order) {
        throw new UnsupportedOperationException("Service is Not yet implemented");
    }
}
