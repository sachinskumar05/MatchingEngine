package com.sk.matching.exchange.orderbook;

import com.sk.matching.exchange.order.GenOrder;
import lombok.extern.log4j.Log4j2;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;


/**
 * The order book output should be formatted to a fixed width using the following template:
 * Buyers               Sellers
 * 000,000,000 000000 | 000000 000,000,000
 * Please note
 */
@Log4j2
public class OrderBookDisplayFixedWidth implements OrderBookDisplay {

    private final int displayDepth = 10;

    private static final OrderBookDisplayFixedWidth DISPLAY_MATCH_ORDER = new OrderBookDisplayFixedWidth();
    public static OrderBookDisplayFixedWidth getInstance() {
        return DISPLAY_MATCH_ORDER;
    }
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("Prohibited to be cloned");
    }

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###########.00##########");
    private static final DecimalFormat DECIMAL_TO_INT_FORMAT = new DecimalFormat("###########");

    @Override
    public String printFormat(OrderBook orderBook) {
        final StringBuilder sb = new StringBuilder(
                String.format("\n=================== ORDER BOOK of %s ===================\n", orderBook.getSymbol())
        );
        sb.append("\n")
        .append("\tBids (buying)\t\t\t\tAsks (selling)\t\n")
        .append("Volume\t\tPrice\t\t\tPrice\t\tVolume")
        .append(formatAsk(orderBook))
        .append(formatBid(orderBook))
        .append("\n")
        .append("\n=================== END of ORDER BOOK ===================");
        return sb.toString();

    }

    private String formatAsk(OrderBook orderBook) {
        StringBuilder sb = new StringBuilder("\n");

        final SortedMap<Double,Integer> askOrderSortedMap = new ConcurrentSkipListMap<>((o1, o2) -> Double.compare(o2,o1));
        int count = 0 ;
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getAskOrderSortedMap().entrySet() ) {
            if( count++ > displayDepth ) break;
            Integer volume = entry.getValue().stream().map(value -> (int)value.getLeavesQty()).reduce(0, Integer::sum);
            askOrderSortedMap.put(entry.getKey(), volume);
        }

        for ( Map.Entry<Double, Integer> entry : askOrderSortedMap.entrySet() ) {
            sb.append("\t\t\t\t\t\t\t")
              .append(entry.getValue()).append("\t\t\t")
              .append(DECIMAL_FORMAT.format(entry.getKey())).append("\t")
              .append("\n");
        }
        return sb.toString();
    }

    private String formatBid(OrderBook orderBook) {
        StringBuilder sb = new StringBuilder();
        final SortedMap<Double,Integer> bidOrderSortedMap = new ConcurrentSkipListMap<>((o1, o2) -> Double.compare(o2,o1));
        int count = 0 ;
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getBidOrderSortedMap().entrySet() ) {
            if( count++ > displayDepth ) break;
            Integer volume = entry.getValue().stream().map(value -> (int)value.getLeavesQty()).reduce(0, Integer::sum);
            bidOrderSortedMap.put(entry.getKey(), volume);
        }

        for ( Map.Entry<Double, Integer> entry : bidOrderSortedMap.entrySet() ) {
            sb.append(DECIMAL_FORMAT.format(entry.getKey())).append("\t\t")
            .append(entry.getValue()).append("\t")
            .append("\n");
        }
        return sb.toString();

    }

}
