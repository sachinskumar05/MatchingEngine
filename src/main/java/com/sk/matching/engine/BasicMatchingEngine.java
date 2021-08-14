package com.sk.matching.engine;

import com.sk.matching.exception.OrderCreationException;
import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.orderbook.OrderBook;
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
public class BasicMatchingEngine implements MatchingEngine {

    private static final AtomicLong atomicOrderId = new AtomicLong();

    private static final ExecutorService executorForMatching = Executors.newFixedThreadPool(20 );

    private BasicMatchingEngine() {
         for(Symbol symbol : EquitySymbolCache.getAllSymbols() ) {
            OrderBook.getBook(symbol);//Pre initialization
        }
    }

    private static final BasicMatchingEngine INSTANCE = new BasicMatchingEngine();
    public static BasicMatchingEngine getInstance() {
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
        OrderBook ordBook = OrderBook.getBook(symbol);
        ordBook.getOrderHistory();

        return tradeList;
    }

    @Override
    public void addOrder(Order order) throws OrderCreationException {
        GenOrder genOrder = (GenOrder) order;

        if(genOrder.getSide() != BUY && genOrder.getSide() != SELL) {
            log.error("Invalid SIDE {} for clOrdId {} ",
                    genOrder::getSide, genOrder::getClientOrderId);
            return ;
        }
        if(genOrder.getOrderType() != LIMIT && genOrder.getOrderType() != MARKET) {
            log.error("Invalid ORDER TYPE {} for clOrdId {} ",
                    genOrder::getOrderType, genOrder::getClientOrderId);
            return ;
        }
        //Locate the order book
        OrderBook orderBook = getOrderBook(genOrder.getSymbol());
        if(null == orderBook ) {
            throw new OrderCreationException("Unknown security/security received symbol in order " + genOrder.getSymbol());
        }
        genOrder.setOrderId(atomicOrderId.incrementAndGet());
        log.info("Received to add clOrdId {}, side {}, price {}, qty {}, order id {} ",
                genOrder::getClientOrderId, genOrder::getSide, genOrder::getOrdPx, genOrder::getOrdQty, genOrder::getOrderId);

        executorForMatching.submit( ()-> orderBook.processOrder(genOrder));//Submitted for possible execution

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
