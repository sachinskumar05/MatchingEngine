package com.sk.matching.client;

import com.sk.matching.engine.EquityMatchingEngine;
import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.exchange.order.EQOrder;
import com.sk.matching.exchange.order.Order;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import com.sk.matching.util.MEDateUtils;
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

    private static final EquityMatchingEngine EQUITY_MATCHING_ENGINE = EquityMatchingEngine.getInstance();

    private static final AtomicInteger incrementallyUnique = new AtomicInteger(0);

    private final List<EQOrder> eqOrderList = new ArrayList<>();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public  final Lock writeLock = readWriteLock.writeLock();

    public void createAndSubmitOrder(String symbol, Side side, double px, double qty, OrderType ot) {
        this.createAndSubmitOrder(symbol,side,px, qty, ot, 1);
    }
    private void createAndSubmitOrder(String symbol, Side side, double px, double qty, OrderType ot, int orderSliceCount) {
        for (int i = 0; i < orderSliceCount; i++) {

            String clOrdId = String.format("%s%sC%s",
                    side, MEDateUtils.getCurrentNanos(), incrementallyUnique.getAndIncrement());

            EQOrder.Builder ordBuilder = null;
            try {
                ordBuilder = new EQOrder.Builder(clOrdId, symbol, side, ot);
                EQOrder eqOrder = ordBuilder.with(builder -> {
                    builder.price = px ;
                    builder.qty = qty;
                    builder.currency = USD;
                }) .build();
                eqOrderList.add(eqOrder);
                submitOrder(eqOrder);
            } catch (OrderCreationException | SymbolNotSupportedException e) {
                log.error("Failed to build EQOrder using its builder {} ", ordBuilder, e);
            }
        }
    }

    /**
     * Returns copy of orders to save the original copy from external mutations
     * @return
     */
    @Override
    public List<EQOrder> getClientOrders() {
        final List<EQOrder> orders = new ArrayList<>();
        for( EQOrder eqOrd: eqOrderList) {
            orders.add(eqOrd.copy());
        }
        return orders;
    }

    @Override
    public void submitOrder(Order order) throws OrderCreationException {
        EQOrder eqOrder = (EQOrder) order;
        log.info("Sending Client Order Id {}, Side {}, px {}, qty {}",
                eqOrder::getClientOrderId, eqOrder::getSide,
                eqOrder::getOrdPx, eqOrder::getOrdQty);
        EQUITY_MATCHING_ENGINE.addOrder(eqOrder);
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
