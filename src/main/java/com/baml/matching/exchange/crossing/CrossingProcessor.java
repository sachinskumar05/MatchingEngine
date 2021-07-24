package com.baml.matching.exchange.crossing;

import com.baml.matching.exchange.EquityOrderBook;
import com.baml.matching.exchange.order.EQOrder;
import com.baml.matching.types.Side;
import com.baml.matching.util.MEDateUtils;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.ListIterator;

import static com.baml.matching.types.OrderType.LIMIT;
import static com.baml.matching.types.OrderType.MARKET;
import static com.baml.matching.types.Side.BUY;
import static com.baml.matching.types.Side.SELL;

@Log4j2
public class CrossingProcessor {

    private CrossingProcessor(){}
    private static final CrossingProcessor INSTANCE = new CrossingProcessor();
    public static CrossingProcessor getInstance(){
        return INSTANCE;
    }

    public void processOrder(EQOrder eqOrder) {
        try {
            EquityOrderBook equityOrderBook = EquityOrderBook.getBook(eqOrder.getSymbol());
            equityOrderBook.setOrder(eqOrder);

            Side side = eqOrder.getSide();
            String clOrdId = eqOrder.getClientOrderId();
            log.debug(()-> clOrdId +", side "+ side + " order received... will try to match with opposite side for best price.");

            List<EQOrder> bestOppositeOrderList = equityOrderBook.getBestOppositeOrderList(side);

            if ( null == bestOppositeOrderList || bestOppositeOrderList.isEmpty() ) {
                log.info( ()->"No Opposite Order Exists for side = " + side );
                return ;
            }

            while (eqOrder.getLeavesQty() > 0 && !bestOppositeOrderList.isEmpty()) {

                log.debug("Started Processing --- {}, {}" , eqOrder::getClientOrderId, eqOrder::getLeavesQty);
                if (checkIfBestOppositeExists(eqOrder, equityOrderBook, side, bestOppositeOrderList)) break;

                List<EQOrder> finalList = bestOppositeOrderList;
                log.debug( "--- clOrdId {}, Opposite Orders {}" , eqOrder::getClientOrderId, ()-> finalList);

                bestOppositeOrderList = getEqOrders(eqOrder, equityOrderBook, side, clOrdId, bestOppositeOrderList);
                if (bestOppositeOrderList == null) break;

            }

        }catch (Exception e) {
            log.error("Exception while order matching ", ()-> e );//Exception is set param required to log stacktrace
        }

    }

    private List<EQOrder> getEqOrders(EQOrder eqOrder, EquityOrderBook equityOrderBook, Side side, String clOrdId, List<EQOrder> bestOppositeOrderList) {
        ListIterator<EQOrder> listIterator = bestOppositeOrderList.listIterator();
        while (listIterator.hasNext()) { //Iterate based on receiving sequence
            EQOrder bestOppositeOrder = listIterator.next();
            if ( eqOrder.getOrderType() == MARKET &&
                    (bestOppositeOrder.getOrderType() == MARKET) ) {
                log.debug(() -> "Matching can't be done as BUY and SELL both orders are MARKET Order");
                continue;
            }
            if (matchingTransaction(eqOrder, equityOrderBook, side, clOrdId, listIterator, bestOppositeOrder)) continue;

        }
        if (eqOrder.getLeavesQty() > 0 && bestOppositeOrderList.isEmpty()) {
            log.debug(()->"Check for the next best price opposite side of order " + eqOrder);
            bestOppositeOrderList = equityOrderBook.getBestOppositeOrderList(side);
            if( null == bestOppositeOrderList || bestOppositeOrderList.isEmpty()) return null;
        }
        return bestOppositeOrderList;
    }

    private boolean checkIfBestOppositeExists(EQOrder eqOrder, EquityOrderBook equityOrderBook,
                                              Side side,
                                              List<EQOrder> bestOppositeOrderList) {
        if( eqOrder.getLeavesQty() <= 0 || eqOrder.isClosed() || bestOppositeOrderList.isEmpty() ) {
            return true;
        }
        double bestOppositePrice = equityOrderBook.getBestOppositePrice(eqOrder, side);
        if(Double.isNaN(bestOppositePrice) ) return true;
        return false;
    }

    private boolean matchingTransaction(EQOrder eqOrder, EquityOrderBook equityOrderBook,
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
            double matchPx = bestOppositeOrder.getOrdPx();
            if (eqOrder.getOrderType() == MARKET || bestOppositeOrder.getOrderType() == LIMIT) {
                matchPx = bestOppositeOrder.getOrdPx();
            } else if (eqOrder.getOrderType() == LIMIT || bestOppositeOrder.getOrderType() == MARKET) {
                matchPx = eqOrder.getOrdPx();
            }

            double finalMatchPx = matchPx;
            log.debug("Match price {} for side {} and clOrdId {} with opposite side {} and clOrdId {}",
                    () -> finalMatchPx, () -> side, () -> clOrdId, bestOppositeOrder::getSide,
                    bestOppositeOrder::getClientOrderId);

            try {
                equityOrderBook.writeLock.lock();
                log.debug(() -> "Acquiring Transaction Lock for matching on OrderBook of symbol = " + equityOrderBook.getSymbol());
                //Generate aggressive trade
                eqOrder.execute(equityOrderBook.generateTradeId(), matchPx, matchQty, bestOppositeOrder.getClientOrderId());

                //# Generate the passive executions
                bestOppositeOrder.execute(equityOrderBook.generateTradeId(), matchPx, matchQty, eqOrder.getClientOrderId());

                long transactionTime = MEDateUtils.getCurrentMillis();
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
                    boolean isRemoved = equityOrderBook.removeOrder(eqOrder);
                    log.debug("Removed from matching book? {}, clOrdId={}, orderId={}",
                            isRemoved, eqOrder.getClientOrderId(), eqOrder.getOrderId());
                } else if (eqOrder.getLeavesQty() < 0) {
                    log.warn("Order over executed [Check fill logic if happened ] eqOrder {}", eqOrder);
                    boolean isRemoved = equityOrderBook.removeOrder(eqOrder);
                    log.debug(() -> "Overfilled but is Removed bestOppositeOrder " + isRemoved);
                }

            } finally {
                log.debug(() -> "Releasing Transaction Lock for matching");
                equityOrderBook.writeLock.unlock();
            }
        }
        return false;
    }


}
