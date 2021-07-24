package com.baml.matching.client;

import com.baml.matching.exception.OrderCreationException;
import com.baml.matching.exception.SymbolNotSupportedException;
import com.baml.matching.exchange.order.EQOrder;
import com.baml.matching.exchange.order.Order;
import com.baml.matching.types.OrderType;
import com.baml.matching.util.MEDateUtils;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import static com.baml.matching.types.Side.SELL;
import static com.baml.matching.util.AppConstants.*;

@Log4j2
public class ClientSeller implements EQClient {

    private final List<EQOrder> eqOrderList = new ArrayList<>();
    private final StringBuilder clOrdIdBuilder = new StringBuilder();


    public void createAndSubmitOrder(int orderCount, double px, double qty, OrderType ot) {

        for (int i = 0; i < orderCount; i++) {
            clOrdIdBuilder.setLength(0);
            clOrdIdBuilder.append(CL_ORD_ID_PREFIX_SELL).append(MEDateUtils.getCurrentMillis());

            EQOrder.Builder fxoBuilder = null;
            try {
                fxoBuilder = new EQOrder.Builder(clOrdIdBuilder.toString(), "BAC", SELL, ot);
                eqOrderList.add(fxoBuilder.with(fxob -> { fxob.price = px ; fxob.qty = qty; fxob.currency = USD; }) .build());
            } catch (OrderCreationException | SymbolNotSupportedException e) {
                log.error("Failed to build EQOrder using its builder {} \n{}", fxoBuilder, e);
            }
        }

        for(EQOrder eqOrder : eqOrderList) {
            try {
                submitOrder(eqOrder);
            } catch (Exception e) {
                log.error("Failed to submit SELL order {}", eqOrder, e);
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
        log.info("Sending Client(S) order id {}, side {}, px {}, qty {}",
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
