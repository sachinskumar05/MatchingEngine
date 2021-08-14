package com.sk.matching.exchange.order;

import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exception.SymbolNotSupportedException;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.symbols.EquitySymbolCache;
import com.sk.matching.types.OrderType;
import com.sk.matching.types.Side;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@Log4j2
public class EQOrder implements Order {

    // Order is serializable for persistence / network (not featured as of today)

    private final String clOrdId;
    private long orderId = Long.MIN_VALUE;
    private final Symbol symbol;
    private final Side side;
    private final OrderType orderType;

    private double ordPx = 0.0d;
    private double avgPx = 0.0d;     //Average Execution Price
    private double lastPrice = 0.0d;    //Last Executed Price

    private double ordQty = Double.NaN;   //Order Qty
    private double cumQty = 0.0d;       // Cumulative executed Qty
    private double leavesQty = 0.0d;     //Remaining Qty
    private double lastQty = 0.0d;      //Last Executed Qty

    private String currency;

    @Getter @Setter
    private long receivedTS;
    @Getter @Setter
    private long executionTS;

    private final Map<Long,Trade> tradeMap = new ConcurrentHashMap<>();

    /**
     * Locking is always an overhead on performance
     * therefore, instead of using Locks here on transaction I preferred Queuing on Executors which make it way faster compared to lock
     * And whenever we need explicit locking, all I have to use below write lock in execute method within try finally block
     * e.g. acquire lock at START EXECUTING and release lock when END EXECUTING
     */
    private transient ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private transient Lock writeLock = rwLock.writeLock();

    private AtomicBoolean isOpen = new AtomicBoolean(true);

    private EQOrder(String clOrdId, Symbol symbol, Side side, OrderType orderType) {
        this.clOrdId = clOrdId;
        this.symbol = symbol;
        this.side = side;
        this.orderType = orderType;
    }

    @Override
    public Trade execute(long execId, double fillPx, double fillQty, String ctrbClOrdId) {
        Trade trade = null;
        try {
            log.debug( "START EXECUTING execId,fillPx,fillQty=[{},{},{}] for clOrdId {} leavesQty is {} ",
                                execId, fillPx, fillQty, clOrdId, leavesQty);
            if (leavesQty == 0) {
                log.info("Order is fully filled clOrdId={}, orderId={}", clOrdId, orderId);
                this.isOpen.set(false);
                return null;
            } else if (leavesQty < 0) {
                log.error(()->"Order is over filled, please check");
                this.isOpen.set(false);
                return null;
            }

            double avgPxComputed = ((this.avgPx * this.cumQty) + fillPx) / (this.cumQty + fillQty);
            double leavesQtyComputed = this.leavesQty - fillQty;

            this.lastPrice = fillPx;
            this.lastQty = fillQty;
            this.cumQty += fillQty;
            //Post all computation updating the order values with successful execution
            this.avgPx = avgPxComputed;
            log.debug("Before leaves qty {}, clOrdId {}, orderId {}",
                    this::getLeavesQty, this::getClientOrderId, this::getOrderId );
            this.leavesQty = leavesQtyComputed;
            log.debug("After leaves qty {}, clOrdId {}, orderId {}",
                    this::getLeavesQty, this::getClientOrderId, this::getOrderId );
            trade = new Trade(getOrderId(), getSymbol(), fillPx, fillQty, getSide(), execId, ctrbClOrdId);
            addTrade( trade );
            if( this.leavesQty <= 0 )
                isOpen.set(false);
            else if( this.leavesQty > 0 )
                isOpen.set(true);
        } catch (Exception e) {
            log.error(this.toString(), e);
        } finally {
            log.debug( "END EXECUTING execId,fillPx,fillQty=[{},{},{}] for clOrdId {} leavesQty is {} ",
                    execId, fillPx, fillQty, clOrdId, leavesQty);

        }
        return trade;
    }


    @Override
    public Trade rollback( long execId, double fillPx, double fillQty, String ctrbClOrdId ) {
        Trade trade = null;
        try {
            log.debug( "START ROLLBACK execId,fillPx,fillQty=[{},{},{}] for clOrdId {} leavesQty is {} ",
                    execId, fillPx, fillQty, clOrdId, leavesQty);

            double avgPxComputed = ((this.avgPx / this.cumQty) - fillPx) / (this.cumQty - fillQty);
            double leavesQtyComputed = this.leavesQty + fillQty;

            this.lastPrice = fillPx;
            this.lastQty = fillQty;
            this.cumQty -= fillQty;
            //Post all computation updating the order values with successful execution
            this.avgPx = avgPxComputed;
            this.leavesQty = leavesQtyComputed;
            trade = new Trade(getOrderId(), getSymbol(), fillPx, fillQty, getSide(), execId, ctrbClOrdId);
            addTrade( trade );
        } catch (Exception e) {
            log.error(this.toString(), e);
        } finally {
            log.debug( "END ROLLBACK execId,fillPx,fillQty=[{},{},{}] for clOrdId {} leavesQty is {} ",
                    execId, fillPx, fillQty, clOrdId, leavesQty);
        }
        return trade;
    }

    @Override
    public boolean isOpen() {
        return isOpen.get();
    }

    public boolean isClosed() {
        return !isOpen();
    }

    public void setOrderId(long orderId) {
        if (Long.MIN_VALUE != this.orderId) return; //Already initialized
        this.orderId = orderId;
    }

    public long getOrderId() {
        return orderId;
    }

    public String getClientOrderId() {
        return clOrdId;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public double getOrdPx() {
        return ordPx;
    }

    public double getAvgPx() {
        return avgPx;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public double getOrdQty() {
        return ordQty;
    }

    public double getCumQty() {
        return cumQty;
    }

    public double getLeavesQty() {
        return leavesQty;
    }

    public double getLastQty() {
        return lastQty;
    }

    public String getCurrency() {
        return currency;
    }

    public void addTrade(Trade trade) {
        tradeMap.put(trade.tradeId, trade);
    }
    public void removeTrade(Long tradeId) {
        tradeMap.remove(tradeId);
    }
    public void removeTrade(Trade trade) {
        tradeMap.remove(trade.tradeId);
    }

    public int getTradeCount() {
        return tradeMap.size();
    }

    public Collection<Trade> getTrades() {
        return (tradeMap.values());//Make the Trade objects immutable
    }

    public AtomicBoolean getIsOpen() {
        return isOpen;
    }

    public static class Builder {

        private final String clOrdId;
        private final Symbol instrument;
        private final Side side;
        private final OrderType ordTyp;

        public String currency;
        public double price = Double.NaN;
        public double qty;
        public double cumQty;
        public double leavesQty;

        public Builder(String clOrdId, String symbolStr, Side side, OrderType ordTyp) throws SymbolNotSupportedException {
            this.clOrdId  = clOrdId;
            this.instrument = EquitySymbolCache.get(symbolStr);
            this.side = side;
            this.ordTyp = ordTyp;
        }

        /**
         * Use sample
         * EQOrder fxOrder = new Builder(O001, "BTC/USD", Side.BUY, OrderType.MARKET)
         *         .with(fxOrderBuilder -> {
         *             fxOrderBuilder.price = 20000.00;
         *             fxOrderBuilder.qty = 10.0;
         *             fxOrderBuilder.currency = "USD";
         *         })
         *         .build();
         * @param builderConsumer
         * @return
         */
        public Builder with(Consumer<Builder> builderConsumer) {
            builderConsumer.accept(this);
            return this;
        }

        public EQOrder build() throws OrderCreationException {
            EQOrder eqOrder = new EQOrder(this.clOrdId, this.instrument, this.side, this.ordTyp);
            if (this.ordTyp == OrderType.LIMIT) {
                if(Double.isNaN(this.price)) {
                    throw new OrderCreationException("Limit order must have some price");
                }
                eqOrder.ordPx = this.price;
            }
            if(this.qty <= 0.0d) {
                throw new OrderCreationException("Invalid order Quantity " + qty + " for clOrdId = " + clOrdId );
            }
            eqOrder.leavesQty = this.qty;
            eqOrder.ordQty = this.qty;
            eqOrder.currency = this.currency;
            return eqOrder;
        }

    }

    public EQOrder copy() {
        EQOrder eqOrder = new EQOrder(this.clOrdId, this.symbol, this.side, this.orderType);
        eqOrder.orderId = this.orderId;
        eqOrder.ordPx = this.ordPx;
        eqOrder.avgPx = this.avgPx;                 //Average Execution Price
        eqOrder.lastPrice = this.lastPrice;         //Last Executed Price
        eqOrder.ordQty = this.ordQty;               //Order Qty
        eqOrder.cumQty = this.cumQty;               // Cumulative executed Qty
        eqOrder.leavesQty = this.leavesQty;         //Remaining Qty
        eqOrder.lastQty = this.lastQty;             //Last Executed Qty
        eqOrder.currency = this.currency;
        eqOrder.tradeMap.putAll(this.tradeMap);     //Trade is immutable class
        eqOrder.isOpen.set(this.isOpen.get());
        return eqOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EQOrder)) return false;

        EQOrder eqOrder = (EQOrder) o;

        if (!clOrdId.equals(eqOrder.clOrdId)) return false;
        if (!symbol.equals(eqOrder.symbol)) return false;
        return side == eqOrder.side;

    }

    @Override
    public int hashCode() {
        int result = clOrdId.hashCode();
        result = 31 * result + symbol.hashCode();
        result = 31 * result + side.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "\nEQOrder{" +
                " clOrdId='" + clOrdId + '\'' +
                (orderId != Long.MIN_VALUE ? (", orderId=" + orderId) : ("")) +
                ", equitySymbol=" + symbol +
                ", side=" + side +
                ", orderType=" + orderType +
                ", ordPx=" + ordPx +
                ", avgPx=" + avgPx +
                ", lastPrice=" + lastPrice +
                ", ordQty=" + ordQty +
                ", cumQty=" + cumQty +
                ", leavesQty=" + leavesQty +
                ", lastQty=" + lastQty +
                ", currency='" + currency + '\'' +
                ", isOpen=" + isOpen +
                ",\n Trade History " + tradeMap +
                '}';
    }
}
