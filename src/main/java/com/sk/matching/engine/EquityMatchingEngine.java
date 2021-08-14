package com.sk.matching.engine;

import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exchange.orderbook.OrderBook;
import com.sk.matching.exchange.order.EQOrder;
import com.sk.matching.exchange.order.Order;
import com.sk.matching.exchange.order.Trade;
import com.sk.matching.symbols.Symbol;
import com.sk.matching.symbols.EquitySymbolCache;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.sk.matching.types.OrderType.LIMIT;
import static com.sk.matching.types.OrderType.MARKET;
import static com.sk.matching.types.Side.BUY;
import static com.sk.matching.types.Side.SELL;

@Log4j2
public class EquityMatchingEngine implements MatchingEngine {

    private static final AtomicLong atomicOrderId = new AtomicLong();

    private static final ExecutorService executorForMatching = Executors.newFixedThreadPool(20 );

    private EquityMatchingEngine() {
         for(Symbol symbol : EquitySymbolCache.getAllSymbols() ) {
            OrderBook.getBook(symbol);//Pre initialization
        }
    }

    private static final EquityMatchingEngine INSTANCE = new EquityMatchingEngine();
    public static EquityMatchingEngine getInstance() {
        return INSTANCE;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("EquityMatchingEngine prohibited to be cloned");
    }

    @Override
    public OrderBook getOrderBook(Symbol symbol) {
        if (symbol instanceof Symbol) {
            return OrderBook.getBook((Symbol) symbol);
        } else {
            log.error("Equity Matching Engine is expecting only Equity Symbol");
        }
        return null;
    }

    @Override
    public List<Trade> getTrades(Symbol symbol) {
        List<Trade> tradeList = new ArrayList<>();
        OrderBook ordBook = null;
        if (symbol instanceof Symbol) {
            ordBook = OrderBook.getBook((Symbol) symbol);
            ordBook.getOrderHistory();
        } else {
            log.error("Equity Matching Engine is expecting only Equity Symbol");
        }
        return tradeList;
    }

    @Override
    public void addOrder(Order order) throws OrderCreationException {
        EQOrder eqOrder = (EQOrder) order;

        if(eqOrder.getSide() != BUY && eqOrder.getSide() != SELL) {
            log.error("Invalid SIDE {} for clOrdId {} ",
                    eqOrder::getSide, eqOrder::getClientOrderId);
            return ;
        }
        if(eqOrder.getOrderType() != LIMIT && eqOrder.getOrderType() != MARKET) {
            log.error("Invalid ORDER TYPE {} for clOrdId {} ",
                    eqOrder::getOrderType, eqOrder::getClientOrderId);
            return ;
        }
        //Locate the order book
        OrderBook orderBook = (OrderBook) getOrderBook(eqOrder.getSymbol());
        if(null == orderBook ) {
            throw new OrderCreationException("Unknown security/security received symbol in order " + eqOrder.getSymbol());
        }
        eqOrder.setOrderId(atomicOrderId.incrementAndGet());
        log.info("Received to add clOrdId {}, side {}, price {}, qty {}, order id {} ",
                eqOrder::getClientOrderId, eqOrder::getSide, eqOrder::getOrdPx, eqOrder::getOrdQty, eqOrder::getOrderId);

        executorForMatching.submit( ()-> orderBook.processOrder(eqOrder));//Submitted for possible execution

    }


    @Override
    public void cancelOrder(Order order) {
        throw new UnsupportedOperationException("Order Cancellation is not supported. This project is for execution demo only");
    }

    @Override
    public void amendOrder(Order order) {
        throw new UnsupportedOperationException("Order Modification is not supported. This project is for execution demo only");

    }

}
