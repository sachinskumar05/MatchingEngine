package com.sk.matching.exchange.crossing;

import com.sk.matching.exchange.orderbook.OrderBook;
import com.sk.matching.exchange.order.EQOrder;
import com.sk.matching.types.Side;
import com.sk.matching.util.MEDateUtils;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import static com.sk.matching.types.OrderType.LIMIT;
import static com.sk.matching.types.OrderType.MARKET;
import static com.sk.matching.types.Side.BUY;
import static com.sk.matching.types.Side.SELL;

@Log4j2
public class CrossingProcessor {

    public void processOrder(EQOrder eqOrder) {
        try {
            OrderBook orderBook = OrderBook.getBook(eqOrder.getSymbol());
            orderBook.setOrder(eqOrder);

            Side side = eqOrder.getSide();
            String clOrdId = eqOrder.getClientOrderId();
            log.debug(()-> clOrdId +", side "+ side + " order received... will try to match with opposite side for best price.");

            List<EQOrder> bestOppositeOrderList = orderBook.getBestOppositeOrderList(side);

            if ( bestOppositeOrderList.isEmpty() ) {
                log.info( ()->"No Opposite Order Exists for side = " + side );
                return ;
            }

            while (eqOrder.getLeavesQty() > 0 && (!bestOppositeOrderList.isEmpty()) ) {

                log.debug("Started Processing --- {}, {}" , eqOrder::getClientOrderId, eqOrder::getLeavesQty);
                if (checkIfBestOppositeExists(eqOrder, orderBook, side, bestOppositeOrderList)) break;

                List<EQOrder> finalList = bestOppositeOrderList;
                log.debug( "--- clOrdId {}, Opposite Orders {}" , eqOrder::getClientOrderId, ()-> finalList);

                bestOppositeOrderList = executeOrders(eqOrder, orderBook, side, clOrdId, bestOppositeOrderList);

            }

        }catch (Exception e) {
            log.error("Exception while order matching ", ()-> e );//Exception is set param required to log stacktrace
        }

    }

    private List<EQOrder> executeOrders(EQOrder eqOrder, OrderBook orderBook, Side side, String clOrdId, List<EQOrder> bestOppositeOrderList) {
        ListIterator<EQOrder> listIterator = bestOppositeOrderList.listIterator();
        while (listIterator.hasNext()) { //Iterate based on receiving sequence
            EQOrder bestOppositeOrder = listIterator.next();
            if ( eqOrder.getOrderType() == MARKET &&
                    (bestOppositeOrder.getOrderType() == MARKET) ) {
                log.debug(() -> "Matching can't be done as BUY and SELL both orders are MARKET Order");
                continue;
            }

            matchingTransaction(eqOrder, orderBook, side, clOrdId, listIterator, bestOppositeOrder);

        }
        if (eqOrder.getLeavesQty() > 0 && bestOppositeOrderList.isEmpty()) {
            log.debug(()->"Check for the next best price opposite side of order " + eqOrder);
            bestOppositeOrderList = orderBook.getBestOppositeOrderList(side);
            if( null == bestOppositeOrderList || bestOppositeOrderList.isEmpty()) return new ArrayList<>();
        }
        return bestOppositeOrderList;
    }

    private boolean checkIfBestOppositeExists(EQOrder eqOrder, OrderBook orderBook,
                                              Side side,
                                              List<EQOrder> bestOppositeOrderList) {
        if( eqOrder.getLeavesQty() <= 0 || eqOrder.isClosed() || bestOppositeOrderList.isEmpty() ) {
            return true;
        }
        double bestOppositePrice = orderBook.getBestOppositePrice(eqOrder, side);
        return (Double.isNaN(bestOppositePrice) ) ;
    }

    private boolean matchingTransaction(EQOrder eqOrder, OrderBook orderBook,
                                        Side side, String clOrdId,
                                        ListIterator<EQOrder> listIterator,
                                        EQOrder bestOppositeOrder) {

        if ( (eqOrder.getOrderType() == MARKET || bestOppositeOrder.getOrderType() == MARKET
                || eqOrder.getOrdPx() == 0.0d || //0.0d => MKT order
                ( (side == BUY && eqOrder.getOrdPx() >= bestOppositeOrder.getOrdPx()) ||
                        (side == SELL && eqOrder.getOrdPx() <= bestOppositeOrder.getOrdPx())
                )
        )
        ) {

            double matchQty = Math.min(eqOrder.getLeavesQty(), bestOppositeOrder.getLeavesQty());
            log.debug("Match qty {} for side {} and clOrdId {} with opposite side {} and clOrdId {}",
                    () -> matchQty, () -> side, () -> clOrdId, bestOppositeOrder::getSide,
                    bestOppositeOrder::getClientOrderId);

            if (matchQty <= 0.0d) {
                log.warn(() -> "Match qty should be larger than 0, no matching found");
                return true;
            }
            double matchPx = getMatchPx(eqOrder, bestOppositeOrder);

            log.debug("Match price {} for side {} and clOrdId {} with opposite side {} and clOrdId {}",
                    () -> matchPx, () -> side, () -> clOrdId, bestOppositeOrder::getSide,
                    bestOppositeOrder::getClientOrderId);

            try {
                log.debug("TRANSACTION STARTS on Symbol {} between {} and {}",
                        orderBook.getSymbol(), eqOrder.getClientOrderId(), bestOppositeOrder.getClientOrderId());
                //Generate aggressive trade
                eqOrder.execute(orderBook.generateTradeId(), matchPx, matchQty, bestOppositeOrder.getClientOrderId());

                //# Generate the passive executions
                bestOppositeOrder.execute(orderBook.generateTradeId(), matchPx, matchQty, eqOrder.getClientOrderId());

                long transactionTime = MEDateUtils.getCurrentNanos();
                eqOrder.setExecutionTS(transactionTime);
                bestOppositeOrder.setExecutionTS(transactionTime);

                if (bestOppositeOrder.getLeavesQty() == 0) {
                    listIterator.remove();
                    log.debug("Removed from matching book as bestOppositeOrder {}, bestOppositeOrderId {}",
                            bestOppositeOrder.getClientOrderId(), bestOppositeOrder.getOrderId());
                } else if (bestOppositeOrder.getLeavesQty() < 0) {
                    log.error(() -> "Order over executed [Check fill logic if happened ] eqOrder = " + bestOppositeOrder);
                    listIterator.remove();
                }

                if (eqOrder.getLeavesQty() == 0) {
                    boolean isRemoved = orderBook.removeOrder(eqOrder);
                    log.debug("Removed from matching book? {}, clOrdId={}, orderId={}",
                            isRemoved, eqOrder.getClientOrderId(), eqOrder.getOrderId());
                } else if (eqOrder.getLeavesQty() < 0) {
                    log.warn("Order over executed [Check fill logic if happened ] eqOrder {}", eqOrder);
                    boolean isRemoved = orderBook.removeOrder(eqOrder);
                    log.debug(() -> "Overfilled but is Removed bestOppositeOrder " + isRemoved);
                }

            } finally {
                log.debug("TRANSACTION ENDS on Symbol {} between {} and {}",
                        orderBook.getSymbol(), eqOrder.getClientOrderId(), bestOppositeOrder.getClientOrderId());

            }
        }
        return false;
    }

    private double getMatchPx(EQOrder eqOrder, EQOrder bestOppositeOrder) {
        double matchPx = bestOppositeOrder.getOrdPx();
        if (eqOrder.getOrderType() == MARKET || bestOppositeOrder.getOrderType() == LIMIT) {
            matchPx = bestOppositeOrder.getOrdPx();
        } else if (eqOrder.getOrderType() == LIMIT || bestOppositeOrder.getOrderType() == MARKET) {
            matchPx = eqOrder.getOrdPx();
        }
        return matchPx;
    }


}
