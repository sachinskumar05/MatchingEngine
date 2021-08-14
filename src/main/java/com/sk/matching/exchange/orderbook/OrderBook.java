package com.sk.matching.exchange.orderbook;

import com.sk.matching.exchange.crossing.CrossingProcessor;
import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.types.Side;
import com.sk.matching.util.MEDateUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

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

    private final OrderBookDisplay orderBookDisplay = OrderBookDisplayFixedWidth.getInstance();

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
    private final SortedMap<Double,List<GenOrder>> bidOrderSortedMap = new ConcurrentSkipListMap<>();
    @Getter
    private final SortedMap<Double,List<GenOrder>> askOrderSortedMap = new ConcurrentSkipListMap<>();

    private final Map<Long, GenOrder> orderHistory = new ConcurrentHashMap<>();

    public Symbol getSymbol() {
        return symbol;
    }


    public long generateTradeId() {
        return  (MEDateUtils.getCurrentNanos() + currentTradeId.getAndIncrement()) % Long.MIN_VALUE;
    }


    public boolean setOrder(GenOrder genOrder) {
        Objects.requireNonNull(genOrder);
        orderHistory.put(genOrder.getOrderId(), genOrder);
        if (genOrder.getSide() == BUY) {
            return setBid(genOrder);
        } else if (genOrder.getSide() == SELL) {
            return setAsk(genOrder);
        }
        return false;
    }

    public List<GenOrder> getBestOppositeOrderList(Side ordSide) {
        List<GenOrder> bestOppositeOrderList;
        if( ordSide == BUY) {
            bestOppositeOrderList = this.getBestAsk();
        } else {
            bestOppositeOrderList = this.getBestBid();
        }
        return bestOppositeOrderList;
    }

    public double getBestOppositePrice(GenOrder genOrder, Side ordSide) {
        double bestOppositePrice;
        if( ordSide == BUY) {
            bestOppositePrice = this.getBestAskPrice();
            if( genOrder.getOrdPx() < bestOppositePrice ) {
                double finalBestOppositePrice = bestOppositePrice;
                log.debug("Price can't Match as Bid/BUY price {} is lower than best opposite price {}",
                        genOrder::getOrdPx, ()-> finalBestOppositePrice);
                bestOppositePrice = Double.NaN;
            }
        } else {
            bestOppositePrice = this.getBestBidPrice();
            if( genOrder.getOrdPx() > bestOppositePrice ) {
                double finalBestOppositePrice = bestOppositePrice;
                log.debug("Price can't Match as Ask/SELL price {} is higher than best opposite price {}",
                        genOrder::getOrdPx, ()-> finalBestOppositePrice);
                bestOppositePrice = Double.NaN;
            }
        }
        return bestOppositePrice;
    }



    private boolean setAsk( GenOrder genOrder) {
        if( genOrder.getSide() != SELL ) {
            log.error("Wrong side, only SELL can be set for ask eqOrder = {}" , ()-> genOrder);
            return false;
        }
        List<GenOrder> genOrderList = askOrderSortedMap.computeIfAbsent(genOrder.getOrdPx(), k-> new ArrayList<>());
        return setOrderToList(genOrderList, genOrder);
    }

    private boolean setOrderToList(List<GenOrder> genOrderList, GenOrder genOrder) {

        if(genOrderList.contains(genOrder)) {
            log.error("Duplicate Ask order received {}" , ()-> genOrder);
            return false;
        }
        genOrder.setReceivedTS(MEDateUtils.getCurrentNanos());
        return genOrderList.add(genOrder);
    }

    private boolean setBid(GenOrder genOrder) {
        if( genOrder.getSide() != BUY ) {
            log.error("Wrong side, only BUY can be set for bid eqOrder = {}" , ()-> genOrder);
            return false;
        }
        List<GenOrder> genOrderList = bidOrderSortedMap.computeIfAbsent(genOrder.getOrdPx(), k-> new ArrayList<>());
        return setOrderToList(genOrderList, genOrder);
    }

    public List<GenOrder> getBestBid() {
        if( bidOrderSortedMap.isEmpty() ) return new ArrayList<>();
        List<GenOrder> bestBid = bidOrderSortedMap.get(bidOrderSortedMap.lastKey());
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

    public boolean removeBid(GenOrder genOrder) {
        Objects.requireNonNull(genOrder);
        if( bidOrderSortedMap.isEmpty() ) {
            log.error(" fxBidOrderSortedMap is empty, potential indication of race condition bug, can't removed order {}" , ()-> genOrder);
            return false;
        }
        List<GenOrder> fxoList = bidOrderSortedMap.computeIfPresent(genOrder.getOrdPx(), (px, fxol)-> {
            fxol.removeIf(fxo -> fxo.equals(genOrder));
            return fxol;
        });
        Double ordPxKey = genOrder.getOrdPx();
        List<GenOrder> genOrderList = bidOrderSortedMap.get(ordPxKey);
        if(genOrderList.isEmpty())
            bidOrderSortedMap.remove(ordPxKey);
        log.debug("After Removal, List of Bids on price {}, {} " , ordPxKey, genOrderList);
        return null!=fxoList && !fxoList.contains(genOrder);
    }

    public List<GenOrder> getBestAsk() {
        if( askOrderSortedMap.isEmpty() ) return new ArrayList<>();
        List<GenOrder> bestAsk = askOrderSortedMap.get(askOrderSortedMap.firstKey());
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

    public boolean removeAsk(GenOrder genOrder) {
        Objects.requireNonNull(genOrder);
        if( askOrderSortedMap.isEmpty() ) {
            log.error(" fxAskOrderSortedMap is empty, potential indication of race condition bug, can't removed order {}" , ()-> genOrder);
            return false;
        }
        List<GenOrder> fxoList = askOrderSortedMap.computeIfPresent(genOrder.getOrdPx(), (px, fxol)-> {
            fxol.removeIf(fxo -> fxo.equals(genOrder));
                return fxol;
            });
        Double ordPxKey = genOrder.getOrdPx();
        List<GenOrder> genOrderList = askOrderSortedMap.get(ordPxKey);
        if(genOrderList.isEmpty()) {
            askOrderSortedMap.remove(ordPxKey);
        }
        log.debug("After Removal, List of Asks on price {}, {} " , ordPxKey, genOrderList);
        return null!=fxoList && !fxoList.contains(genOrder);
    }

    public void processOrder(GenOrder genOrder) {
        executorForCrossing.execute(()->crossingProcessor.processOrder(genOrder));
    }

    public boolean removeOrder(GenOrder genOrder) {
        if( genOrder.getSide() == BUY ) {
            return removeBid(genOrder);
        } else if (genOrder.getSide() == SELL ) {
            return removeAsk(genOrder);
        }
        log.error("Un-identified side to find order to be removed order {}" , ()-> genOrder);
        return false;
    }

    public Collection<GenOrder> getOrderHistory() {
        return this.orderHistory.values();
    }

    public GenOrder getOrder(Long orderId) {
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
        return orderBookDisplay.printFormat(this);
    }



}
