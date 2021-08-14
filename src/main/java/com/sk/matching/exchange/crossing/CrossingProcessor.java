package com.sk.matching.exchange.crossing;

import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.orderbook.OrderBook;
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

    public void processOrder(GenOrder genOrder) {
        try {
            OrderBook orderBook = OrderBook.getBook(genOrder.getSymbol());
            orderBook.setOrder(genOrder);

            Side side = genOrder.getSide();
            String clOrdId = genOrder.getClientOrderId();
            log.debug(()-> clOrdId +", side "+ side + " order received... will try to match with opposite side for best price.");

            List<GenOrder> bestOppositeOrderList = orderBook.getBestOppositeOrderList(side);

            if ( bestOppositeOrderList.isEmpty() ) {
                log.info( ()->"No Opposite Order Exists for side = " + side );
                return ;
            }

            while (genOrder.getLeavesQty() > 0 && (!bestOppositeOrderList.isEmpty()) ) {

                log.debug("Started Processing --- {}, {}" , genOrder::getClientOrderId, genOrder::getLeavesQty);
                if (checkIfBestOppositeExists(genOrder, orderBook, side, bestOppositeOrderList)) break;

                List<GenOrder> finalList = bestOppositeOrderList;
                log.debug( "--- clOrdId {}, Opposite Orders {}" , genOrder::getClientOrderId, ()-> finalList);

                bestOppositeOrderList = executeOrders(genOrder, orderBook, side, clOrdId, bestOppositeOrderList);

            }

        }catch (Exception e) {
            log.error("Exception while order matching ", ()-> e );//Exception is set param required to log stacktrace
        }

    }

    private List<GenOrder> executeOrders(GenOrder genOrder, OrderBook orderBook, Side side, String clOrdId, List<GenOrder> bestOppositeOrderList) {
        ListIterator<GenOrder> listIterator = bestOppositeOrderList.listIterator();
        while (listIterator.hasNext()) { //Iterate based on receiving sequence
            GenOrder bestOppositeOrder = listIterator.next();
            if ( genOrder.getOrderType() == MARKET &&
                    (bestOppositeOrder.getOrderType() == MARKET) ) {
                log.debug(() -> "Matching can't be done as BUY and SELL both orders are MARKET Order");
                continue;
            }

            matchingTransaction(genOrder, orderBook, side, clOrdId, listIterator, bestOppositeOrder);

        }
        if (genOrder.getLeavesQty() > 0 && bestOppositeOrderList.isEmpty()) {
            log.debug(()->"Check for the next best price opposite side of order " + genOrder);
            bestOppositeOrderList = orderBook.getBestOppositeOrderList(side);
            if( null == bestOppositeOrderList || bestOppositeOrderList.isEmpty()) return new ArrayList<>();
        }
        return bestOppositeOrderList;
    }

    private boolean checkIfBestOppositeExists(GenOrder genOrder, OrderBook orderBook,
                                              Side side,
                                              List<GenOrder> bestOppositeOrderList) {
        if( genOrder.getLeavesQty() <= 0 || genOrder.isClosed() || bestOppositeOrderList.isEmpty() ) {
            return true;
        }
        double bestOppositePrice = orderBook.getBestOppositePrice(genOrder, side);
        return (Double.isNaN(bestOppositePrice) ) ;
    }

    private boolean matchingTransaction(GenOrder genOrder, OrderBook orderBook,
                                        Side side, String clOrdId,
                                        ListIterator<GenOrder> listIterator,
                                        GenOrder bestOppositeOrder) {

        if ( (genOrder.getOrderType() == MARKET || bestOppositeOrder.getOrderType() == MARKET
                || genOrder.getOrdPx() == 0.0d || //0.0d => MKT order
                ( (side == BUY && genOrder.getOrdPx() >= bestOppositeOrder.getOrdPx()) ||
                        (side == SELL && genOrder.getOrdPx() <= bestOppositeOrder.getOrdPx())
                )
        )
        ) {

            double matchQty = Math.min(genOrder.getLeavesQty(), bestOppositeOrder.getLeavesQty());
            log.debug("Match qty {} for side {} and clOrdId {} with opposite side {} and clOrdId {}",
                    () -> matchQty, () -> side, () -> clOrdId, bestOppositeOrder::getSide,
                    bestOppositeOrder::getClientOrderId);

            if (matchQty <= 0.0d) {
                log.warn(() -> "Match qty should be larger than 0, no matching found");
                return true;
            }
            double matchPx = getMatchPx(genOrder, bestOppositeOrder);

            log.debug("Match price {} for side {} and clOrdId {} with opposite side {} and clOrdId {}",
                    () -> matchPx, () -> side, () -> clOrdId, bestOppositeOrder::getSide,
                    bestOppositeOrder::getClientOrderId);

            try {
                log.debug("TRANSACTION STARTS on Symbol {} between {} and {}",
                        orderBook.getSymbol(), genOrder.getClientOrderId(), bestOppositeOrder.getClientOrderId());
                //Generate aggressive trade
                genOrder.execute(orderBook.generateTradeId(), matchPx, matchQty, bestOppositeOrder.getClientOrderId());

                //# Generate the passive executions
                bestOppositeOrder.execute(orderBook.generateTradeId(), matchPx, matchQty, genOrder.getClientOrderId());

                long transactionTime = MEDateUtils.getCurrentNanos();
                genOrder.setExecutionTS(transactionTime);
                bestOppositeOrder.setExecutionTS(transactionTime);

                if (bestOppositeOrder.getLeavesQty() == 0) {
                    listIterator.remove();
                    log.debug("Removed from matching book as bestOppositeOrder {}, bestOppositeOrderId {}",
                            bestOppositeOrder.getClientOrderId(), bestOppositeOrder.getOrderId());
                } else if (bestOppositeOrder.getLeavesQty() < 0) {
                    log.error(() -> "Order over executed [Check fill logic if happened ] eqOrder = " + bestOppositeOrder);
                    listIterator.remove();
                }

                if (genOrder.getLeavesQty() == 0) {
                    boolean isRemoved = orderBook.removeOrder(genOrder);
                    log.debug("Removed from matching book? {}, clOrdId={}, orderId={}",
                            isRemoved, genOrder.getClientOrderId(), genOrder.getOrderId());
                } else if (genOrder.getLeavesQty() < 0) {
                    log.warn("Order over executed [Check fill logic if happened ] eqOrder {}", genOrder);
                    boolean isRemoved = orderBook.removeOrder(genOrder);
                    log.debug(() -> "Overfilled but is Removed bestOppositeOrder " + isRemoved);
                }

            } finally {
                log.debug("TRANSACTION ENDS on Symbol {} between {} and {}",
                        orderBook.getSymbol(), genOrder.getClientOrderId(), bestOppositeOrder.getClientOrderId());

            }
        }
        return false;
    }

    private double getMatchPx(GenOrder genOrder, GenOrder bestOppositeOrder) {
        double matchPx = bestOppositeOrder.getOrdPx();
        if (genOrder.getOrderType() == MARKET || bestOppositeOrder.getOrderType() == LIMIT) {
            matchPx = bestOppositeOrder.getOrdPx();
        } else if (genOrder.getOrderType() == LIMIT || bestOppositeOrder.getOrderType() == MARKET) {
            matchPx = genOrder.getOrdPx();
        }
        return matchPx;
    }


}
