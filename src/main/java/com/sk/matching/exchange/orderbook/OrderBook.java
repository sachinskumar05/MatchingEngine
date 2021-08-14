package com.sk.matching.exchange.orderbook;

import com.sk.matching.exchange.crossing.CrossingProcessor;
import com.sk.matching.exchange.order.EQOrder;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.types.Side;
import com.sk.matching.util.MEDateUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.sk.matching.types.Side.BUY;
import static com.sk.matching.types.Side.SELL;

@Log4j2
public class OrderBook implements Serializable {

    private final OrderBookDisplay orderBookDisplayMatchOrder = OrderBookDisplayMatchOrder.getInstance();

    private static final Map<Symbol, OrderBook> orderBookCache = new ConcurrentHashMap<>();

    public static OrderBook getBook(Symbol symbol) {//Flyweight and thread safe
        return orderBookCache.computeIfAbsent(symbol, eqs -> new OrderBook(eqs) );
    }

    private OrderBook(Symbol symbol) {
        this.symbol = symbol;
    }

    private static final AtomicLong currentTradeId = new AtomicLong();

    private final transient ExecutorService executorForCrossing = Executors.newSingleThreadExecutor();
    private final transient CrossingProcessor crossingProcessor = new CrossingProcessor();

    private final transient ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    public  final transient Lock writeLock = readWriteLock.writeLock();


    private final Symbol symbol;
    //Using Red-Black Tree Implementation for auto sorting on insertion O(log N)
    //Comparatively faster than having two phase operation i.e. Keep price in Priority Queue and orders in Price Map of Order List.
    //Moreover, ConcurrentSkipListMap is thread safe
    @Getter
    private final SortedMap<Double,List<EQOrder>> bidOrderSortedMap = new ConcurrentSkipListMap<>();
    @Getter
    private final SortedMap<Double,List<EQOrder>> askOrderSortedMap = new ConcurrentSkipListMap<>();

    private final Map<Long, EQOrder> orderHistory = new ConcurrentHashMap<>();

    public Symbol getSymbol() {
        return symbol;
    }


    public long generateTradeId() {
        return  (MEDateUtils.getCurrentNanos() + currentTradeId.getAndIncrement()) % Long.MIN_VALUE;
    }


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
        List<EQOrder> eqOrderList = askOrderSortedMap.computeIfAbsent(eqOrder.getOrdPx(), k-> new ArrayList<>());
        return setOrderToList(eqOrderList, eqOrder);
    }

    private boolean setOrderToList(List<EQOrder> eqOrderList, EQOrder eqOrder) {

        if(eqOrderList.contains(eqOrder)) {
            log.error("Duplicate Ask order received {}" , ()-> eqOrder);
            return false;
        }
        eqOrder.setReceivedTS(MEDateUtils.getCurrentNanos());
        return eqOrderList.add(eqOrder);
    }

    private boolean setBid(EQOrder eqOrder) {
        if( eqOrder.getSide() != BUY ) {
            log.error("Wrong side, only BUY can be set for bid eqOrder = {}" , ()-> eqOrder);
            return false;
        }
        List<EQOrder> eqOrderList = bidOrderSortedMap.computeIfAbsent(eqOrder.getOrdPx(), k-> new ArrayList<>());
        return setOrderToList(eqOrderList, eqOrder);
    }

    public List<EQOrder> getBestBid() {
        if( bidOrderSortedMap.isEmpty() ) return new ArrayList<>();
        List<EQOrder> bestBid = bidOrderSortedMap.get(bidOrderSortedMap.lastKey());
        if(bestBid != null && !bestBid.isEmpty())
            return bestBid;

        while( !bidOrderSortedMap.isEmpty() ) {
            bidOrderSortedMap.remove(bidOrderSortedMap.lastKey());
            bestBid = bidOrderSortedMap.get(bidOrderSortedMap.lastKey());
            if(bestBid != null && !bestBid.isEmpty())
                return bestBid;
        }

        return new ArrayList<>();
    }

    public double getBestBidPrice() {
        if( bidOrderSortedMap.isEmpty() ) return Double.NaN;
        return (bidOrderSortedMap.lastKey());
    }

    public boolean removeBid(EQOrder eqOrder) {
        if( bidOrderSortedMap.isEmpty() ) {
            log.error(" fxBidOrderSortedMap is empty, potential indication of race condition bug, can't removed order {}" , ()-> eqOrder);
            return false;
        }
        List<EQOrder> fxoList = bidOrderSortedMap.computeIfPresent(eqOrder.getOrdPx(), (px, fxol)-> {
            fxol.removeIf(fxo -> fxo.equals(eqOrder));
            return fxol;
        });
        Double ordPxKey = eqOrder.getOrdPx();
        List<EQOrder> eqOrderList = bidOrderSortedMap.get(ordPxKey);
        if(eqOrderList.isEmpty())
            bidOrderSortedMap.remove(ordPxKey);
        log.debug("After Removal, List of Bids on price {}, {} " , ordPxKey, eqOrderList);
        return null!=fxoList && !fxoList.contains(eqOrder);
    }

    public List<EQOrder> getBestAsk() {
        if( askOrderSortedMap.isEmpty() ) return new ArrayList<>();
        List<EQOrder> bestAsk = askOrderSortedMap.get(askOrderSortedMap.firstKey());
        if(bestAsk != null && !bestAsk.isEmpty())
            return bestAsk;

        while( !askOrderSortedMap.isEmpty() ) {
            askOrderSortedMap.remove(askOrderSortedMap.firstKey());
            bestAsk = askOrderSortedMap.get(askOrderSortedMap.firstKey());
            if(bestAsk != null && !bestAsk.isEmpty())
                return bestAsk;
        }

        return new ArrayList<>();
    }

    public double getBestAskPrice() {
        if( askOrderSortedMap.isEmpty() ) return Double.NaN;
        return (askOrderSortedMap.firstKey());
    }

    public boolean removeAsk(EQOrder eqOrder) {
        if( askOrderSortedMap.isEmpty() ) {
            log.error(" fxAskOrderSortedMap is empty, potential indication of race condition bug, can't removed order {}" , ()-> eqOrder);
            return false;
        }
        List<EQOrder> fxoList = askOrderSortedMap.computeIfPresent(eqOrder.getOrdPx(), (px, fxol)-> {
            fxol.removeIf(fxo -> fxo.equals(eqOrder));
                return fxol;
            });
        Double ordPxKey = eqOrder.getOrdPx();
        List<EQOrder> eqOrderList = askOrderSortedMap.get(ordPxKey);
        if(eqOrderList.isEmpty()) {
            askOrderSortedMap.remove(ordPxKey);
        }
        log.debug("After Removal, List of Asks on price {}, {} " , ordPxKey, eqOrderList);
        return null!=fxoList && !fxoList.contains(eqOrder);
    }

    public void processOrder(EQOrder eqOrder) {
        executorForCrossing.execute(()->crossingProcessor.processOrder(eqOrder));
    }

    public boolean removeOrder(EQOrder eqOrder) {
        if( eqOrder.getSide() == BUY ) {
            return removeBid(eqOrder);
        } else if (eqOrder.getSide() == SELL ) {
            return removeAsk(eqOrder);
        }
        log.error("Un-identified side to find order to be removed order {}" , ()-> eqOrder);
        return false;
    }

    public Collection<EQOrder> getOrderHistory() {
        return this.orderHistory.values();
    }

    public EQOrder getOrder(Long orderId) {
        return this.orderHistory.get(orderId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderBook that = (OrderBook) o;

        return symbol != null ? symbol.equals(that.symbol) : that.symbol == null;
    }

    @Override
    public int hashCode() {
        return symbol != null ? symbol.hashCode() : 0;
    }

    @Override
    public String toString() {
        return orderBookDisplayMatchOrder.printFormat(this);
    }



}
