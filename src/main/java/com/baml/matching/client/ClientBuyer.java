package com.baml.matching.client;

import com.baml.matching.exception.OrderCreationException;
import com.baml.matching.exception.SymbolNotSupportedException;
import com.baml.matching.exchange.order.EQOrder;
import com.baml.matching.exchange.order.Order;
import com.baml.matching.types.OrderType;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

import static com.baml.matching.types.Side.BUY;
import static com.baml.matching.util.AppConstants.*;

@Log4j2
public class ClientBuyer implements EQClient {

   private final List<EQOrder> eqOrderArrayList = new ArrayList<>();
   private final StringBuilder clOrdIdBuilder = new StringBuilder();


    public void createAndSubmitOrder(int orderCount, double px, double qty, OrderType ot) {

        for (int i = 0; i < orderCount; i++) {
            clOrdIdBuilder.setLength(0);
            clOrdIdBuilder.append(CL_ORD_ID_PREFIX_BUY).append(System.nanoTime());
            EQOrder.Builder fxoBuilder = null;

            try {
                fxoBuilder = new EQOrder.Builder(clOrdIdBuilder.toString(), "BAC", BUY, ot);
                eqOrderArrayList.add(fxoBuilder.with(fxobj -> { fxobj.price = px; fxobj.qty = qty; fxobj.currency = USD; }) .build());
            } catch (OrderCreationException | SymbolNotSupportedException e) {
                log.error("Failed to build EQOrder using its builder {} \n{}", fxoBuilder, e);
            }
        }

        for(EQOrder eqOrder : eqOrderArrayList) {
            try {
                submitOrder(eqOrder);
            } catch (Exception e) {
                log.error("Failed to submit SELL order {}", ()-> eqOrder);
            }
        }

    }

    /**
     * Returns copy of orders to save the original copy
     * @return List<EQOrder>
     */
    @Override
    public List<EQOrder> getClientOrders() {
        final List<EQOrder> eqOrdersCopy = new ArrayList<>();
        for( EQOrder eqOrd: eqOrderArrayList) {
            eqOrdersCopy.add(eqOrd.copy());
        }
        return eqOrdersCopy;
    }

    @Override
    public void submitOrder(Order order) {
        EQOrder eqOrder = (EQOrder) order;
        try {
            log.info("Sending client(B) order id {}, side {}, px {}, qty {}",
                    eqOrder::getClientOrderId, eqOrder::getSide,
                    eqOrder::getOrdPx, eqOrder::getOrdQty);
            EQUITY_MATCHING_ENGINE.addOrder(eqOrder);
        } catch(Exception ex) {
            log.error("Failed to submit order for matching engine {}", ()-> eqOrder, ()-> ex );
        }
    }

    @Override
    public void replaceOrder(Order order) {
        throw new UnsupportedOperationException("Operation not yet implemented");
    }

    @Override
    public void cancelOrder(Order order) {
        throw new UnsupportedOperationException("Operation not yet implemented");
    }
}
