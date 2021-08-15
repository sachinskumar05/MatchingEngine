package com.sk.matching.exchange.orderbook;

import com.sk.matching.exchange.order.GenOrder;
import lombok.Data;
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

        List<PxVol> displayList = new ArrayList<>();
        int count = 0 ;
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getAskOrderSortedMap().entrySet() ) {
//            if( count++ > displayDepth ) break;
            for(GenOrder order : entry.getValue()) {
                displayList.add( new PxVol(order.getOrdPx(), displayVol(order) ));;
            }
        }
        Collections.sort(displayList, (o1, o2) -> Double.compare(o2.getPx() , o1.getPx()));
        for ( PxVol pxVol : displayList ) {
            sb.append("\t\t\t\t\t\t\t")
              .append(DECIMAL_FORMAT.format(pxVol.getPx())).append("\t\t\t")
              .append(DECIMAL_TO_INT_FORMAT.format(pxVol.getVol())).append("\t")
              .append("\n");
        }
        return sb.toString();
    }

    private String formatBid(OrderBook orderBook) {
        StringBuilder sb = new StringBuilder();
        List<PxVol> displayList = new ArrayList<>();
        int count = 0 ;
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getBidOrderSortedMap().entrySet() ) {
//            if( count++ > displayDepth ) break;
            for(GenOrder order : entry.getValue()) {
                displayList.add( new PxVol(order.getOrdPx(), displayVol(order) ));;
            }
        }

        for ( PxVol pxVol : displayList ) {
            sb.append(DECIMAL_TO_INT_FORMAT.format(pxVol.getVol())).append("\t\t")
            .append(DECIMAL_FORMAT.format(pxVol.getPx())).append("\t")
            .append("\n");
        }
        return sb.toString();

    }

    @Data
    private class PxVol {
        final Double px;
        final Double vol;
        public PxVol(Double px, Double vol){
            this.vol = vol;
            this. px = px;
        }
    }

    public double displayVol(GenOrder order) {
        if( Double.isNaN(order.getVisibleQty()) ) {
            return order.getLeavesQty();
        } else {
            return Math.min(order.getVisibleQty(), order.getLeavesQty());
        }
    }

}
