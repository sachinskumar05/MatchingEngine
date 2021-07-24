package com.baml.matching.client;

import com.baml.matching.engine.EquityMatchingEngine;
import com.baml.matching.exception.OrderCreationException;
import com.baml.matching.exception.SymbolNotSupportedException;
import com.baml.matching.exchange.order.EQOrder;
import com.baml.matching.exchange.order.Order;
import com.baml.matching.types.OrderType;
import com.baml.matching.types.Side;
import com.baml.matching.util.MEDateUtils;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.baml.matching.types.Side.SELL;
import static com.baml.matching.util.AppConstants.*;

@Log4j2
public class ClientWorker implements Client {

    private static final EquityMatchingEngine EQUITY_MATCHING_ENGINE = EquityMatchingEngine.getInstance();

    private final List<EQOrder> eqOrderList = new ArrayList<>();
    private final StringBuilder clOrdIdBuilder = new StringBuilder();

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public  final Lock writeLock = readWriteLock.writeLock();

    public void createAndSubmitOrder(String symbol, Side side, double px, double qty, OrderType ot) {
        this.createAndSubmitOrder(symbol,side,px, qty, ot, 1);
    }
    private void createAndSubmitOrder(String symbol, Side side, double px, double qty, OrderType ot, int orderSliceCount) {
        for (int i = 0; i < orderSliceCount; i++) {
            clOrdIdBuilder.setLength(0);
            clOrdIdBuilder.append(side).append(MEDateUtils.getCurrentMillis());

            EQOrder.Builder ordBuilder = null;
            try {
                ordBuilder = new EQOrder.Builder(clOrdIdBuilder.toString(), symbol, side, ot);
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
