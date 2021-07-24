package com.baml.matching.exchange;

import com.baml.matching.exchange.order.EQOrder;
import com.baml.matching.symbols.EquitySymbol;
import com.baml.matching.types.Side;
import com.baml.matching.util.MEDateUtils;
import lombok.extern.log4j.Log4j2;

import javax.swing.text.html.parser.Entity;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.baml.matching.types.Side.BUY;
import static com.baml.matching.types.Side.SELL;

@Log4j2
public class EquityOrderBook implements OrderBook, Serializable {

    private static final Map<EquitySymbol, EquityOrderBook> orderBookCache = new ConcurrentHashMap<>();

    public static EquityOrderBook getBook(EquitySymbol equitySymbol) {//Flyweight and thread safe
        return orderBookCache.computeIfAbsent(equitySymbol, eqs -> new EquityOrderBook(eqs) );
    }

    private EquityOrderBook(EquitySymbol equitySymbol) {
        this.equitySymbol = equitySymbol;
    }

    private static final AtomicLong currentTradeId = new AtomicLong();

    private final transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public  final transient Lock writeLock = readWriteLock.writeLock();


    private final EquitySymbol equitySymbol;
    private final SortedMap<Double,List<EQOrder>> fxBidOrderSortedMap = new ConcurrentSkipListMap<>();
    private final SortedMap<Double,List<EQOrder>> fxAskOrderSortedMap = new ConcurrentSkipListMap<>();

    private final Map<Long, EQOrder> orderHistory = new ConcurrentHashMap<>();

    public EquitySymbol getSymbol() {
        return equitySymbol;
    }


    public long generateTradeId() {
        return  (MEDateUtils.getCurrentMillis() + currentTradeId.getAndIncrement()) % Long.MIN_VALUE;
    }

    @Override
    public boolean setOrder(EQOrder eqOrder) {
        orderHistory.put(eqOrder.getOrderId(), eqOrder);
        if (eqOrder.getSide() == BUY) {
            return setBid(eqOrder);
        } else if (eqOrder.getSide() == SELL) {
            return setAsk(eqOrder);
        }
        return false;
    }

    public List<EQOrder> getBestOppositeOrderList(Side ordSide) {
        List<EQOrder> bestOppositeOrderList;
        if( ordSide == BUY) {
            bestOppositeOrderList = this.getBestAsk();
        } else {
            bestOppositeOrderList = this.getBestBid();
        }
        return bestOppositeOrderList;
    }

    public double getBestOppositePrice(EQOrder eqOrder, Side ordSide) {
        double bestOppositePrice;
        if( ordSide == BUY) {
            bestOppositePrice = this.getBestAskPrice();
            if( eqOrder.getOrdPx() < bestOppositePrice ) {
                double finalBestOppositePrice = bestOppositePrice;
                log.debug("Price can't Match as Bid/BUY price {} is lower than best opposite price {}",
                        eqOrder::getOrdPx, ()-> finalBestOppositePrice);
                bestOppositePrice = Double.NaN;
            }
        } else {
            bestOppositePrice = this.getBestBidPrice();
            if( eqOrder.getOrdPx() > bestOppositePrice ) {
                double finalBestOppositePrice = bestOppositePrice;
                log.debug("Price can't Match as Ask/SELL price {} is higher than best opposite price {}",
                        eqOrder::getOrdPx, ()-> finalBestOppositePrice);
                bestOppositePrice = Double.NaN;
            }
        }
        return bestOppositePrice;
    }



    private boolean setAsk( EQOrder eqOrder) {
        if( eqOrder.getSide() != SELL ) {
            log.error("Wrong side, only SELL can be set for ask eqOrder = {}" , ()-> eqOrder);
            return false;
        }
        List<EQOrder> eqOrderList = fxAskOrderSortedMap.computeIfAbsent(eqOrder.getOrdPx(), k-> new ArrayList<>());
        return setOrderToList(eqOrderList, eqOrder);
    }

    private boolean setOrderToList(List<EQOrder> eqOrderList, EQOrder eqOrder) {

        if(eqOrderList.contains(eqOrder)) {
            log.error("Duplicate Ask order received {}" , ()-> eqOrder);
            return false;
        }
        eqOrder.setReceivedTS(MEDateUtils.getCurrentMillis());
        return eqOrderList.add(eqOrder);
    }

    private boolean setBid(EQOrder eqOrder) {
        if( eqOrder.getSide() != BUY ) {
            log.error("Wrong side, only BUY can be set for bid eqOrder = {}" , ()-> eqOrder);
            return false;
        }
        List<EQOrder> eqOrderList = fxBidOrderSortedMap.computeIfAbsent(eqOrder.getOrdPx(), k-> new ArrayList<>());
        return setOrderToList(eqOrderList, eqOrder);
    }

    public List<EQOrder> getBestBid() {
        if( fxBidOrderSortedMap.isEmpty() ) return new ArrayList<>();
        return fxBidOrderSortedMap.get(fxBidOrderSortedMap.lastKey());
    }

    public double getBestBidPrice() {
        if( fxBidOrderSortedMap.isEmpty() ) return Double.NaN;
        return (fxBidOrderSortedMap.lastKey());
    }

    public boolean removeBid(EQOrder eqOrder) {
        if( fxBidOrderSortedMap.isEmpty() ) {
            log.error(" fxBidOrderSortedMap is empty, potential indication of race condition bug, can't removed order {}" , ()-> eqOrder);
            return false;
        }
        List<EQOrder> fxoList = fxBidOrderSortedMap.computeIfPresent(eqOrder.getOrdPx(), (px, fxol)-> {
            fxol.removeIf(fxo -> fxo.equals(eqOrder));
            return fxol;
        });
        log.debug("List of Bids on price {}, {} " , eqOrder::getOrdPx, ()->fxBidOrderSortedMap.get(eqOrder.getOrdPx()));
        return null!=fxoList && !fxoList.contains(eqOrder);
    }

    public List<EQOrder> getBestAsk() {
        if( fxAskOrderSortedMap.isEmpty() ) return new ArrayList<>();
        return fxAskOrderSortedMap.get(fxAskOrderSortedMap.firstKey());
    }

    public double getBestAskPrice() {
        if( fxAskOrderSortedMap.isEmpty() ) return Double.NaN;
        return (fxAskOrderSortedMap.firstKey());
    }

    public boolean removeAsk(EQOrder eqOrder) {
        if( fxAskOrderSortedMap.isEmpty() ) {
            log.error(" fxAskOrderSortedMap is empty, potential indication of race condition bug, can't removed order {}" , ()-> eqOrder);
            return false;
        }
        List<EQOrder> fxoList = fxAskOrderSortedMap.computeIfPresent(eqOrder.getOrdPx(), (px, fxol)-> {
            fxol.removeIf(fxo -> fxo.equals(eqOrder));
                return fxol;
            });
        log.debug("List of Asks on price {}, {} " , eqOrder::getOrdPx, ()->fxAskOrderSortedMap.get(eqOrder.getOrdPx()));
        return null!=fxoList && !fxoList.contains(eqOrder);
    }

    @Override
    public boolean removeOrder(EQOrder eqOrder) {
        if( eqOrder.getSide() == BUY ) {
            return removeBid(eqOrder);
        } else if (eqOrder.getSide() == SELL ) {
            return removeAsk(eqOrder);
        }
        log.error("Un-identified side to find order to be removed order {}" , ()-> eqOrder);
        return false;
    }

    @Override
    public Collection<EQOrder> getOrderHistory() {
        return this.orderHistory.values();
    }

    @Override
    public EQOrder getOrder(Long orderId) {
        return this.orderHistory.get(orderId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EquityOrderBook that = (EquityOrderBook) o;

        return equitySymbol != null ? equitySymbol.equals(that.equitySymbol) : that.equitySymbol == null;
    }

    @Override
    public int hashCode() {
        return equitySymbol != null ? equitySymbol.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("\n=================== ORDER BOOK ===================\nEquityOrderBook{");
        sb.append("\nequitySymbol=").append(equitySymbol)
        .append("-hashCode=").append(equitySymbol.hashCode())
        .append("\n")
        .append("ID\tSide\tTime\t\tQty\t\tPrice\tQty\t\tTime\t\t\tSide")
        .append(formatAsk())
        .append(formatBid())
        .append("\n")
        .append("}\n=================== END of ORDER BOOK ===================");
        return sb.toString();
    }

    public String formatAsk() {
        StringBuilder sb = new StringBuilder("\n");
        for ( Map.Entry<Double, List<EQOrder>> entry : fxAskOrderSortedMap.entrySet() ) {
            List<EQOrder> eqOrderList = entry.getValue();
            for(EQOrder eqOrder: eqOrderList) {
                sb.append(eqOrder.getOrderId()).append("\t")
                .append("    \t    \t   \t\t\t")
                        .append(eqOrder.getOrdPx()).append("\t")
                        .append(eqOrder.getOrdQty()).append("\t")
                        .append(eqOrder.getReceivedTS()).append("\t")
                        .append(eqOrder.getSide());
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public String formatBid() {
        StringBuilder sb = new StringBuilder("\n");
        for ( Map.Entry<Double, List<EQOrder>> entry : fxBidOrderSortedMap.entrySet() ) {
            List<EQOrder> eqOrderList = entry.getValue();
            for(EQOrder eqOrder: eqOrderList) {
                sb.append(eqOrder.getOrderId()).append("\t")
                .append(eqOrder.getSide()).append("\t")
                .append(eqOrder.getReceivedTS()).append("\t")
                .append(eqOrder.getOrdQty()).append("\t")
                .append(eqOrder.getOrdPx()).append("\t")
                .append("  \t    \t    \t   \t");
                sb.append("\n");
            }
        }
        return sb.toString();
    }

}
