package com.sk.matching.exchange.orderbook.display;

import com.sk.matching.exchange.order.GenOrder;
import com.sk.matching.exchange.orderbook.OrderBook;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * The order book output should be formatted to a fixed width using the following template:
 * Buyers               Sellers
 * 000,000,000 000000 | 000000 000,000,000
 * Please note
 */
@Log4j2
public class OrderBookDisplayFixedWidthMatchSorted implements OrderBookDisplay {


    private static final OrderBookDisplayFixedWidthMatchSorted DISPLAY_MATCH_ORDER = new OrderBookDisplayFixedWidthMatchSorted();
    public static OrderBookDisplayFixedWidthMatchSorted getInstance() {
        return DISPLAY_MATCH_ORDER;
    }


    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###########.00##########");
    private static final DecimalFormat DECIMAL_TO_INT_FORMAT = new DecimalFormat("###########");

    @Override
    public String printFormat(OrderBook orderBook) {
        final StringBuilder sb = new StringBuilder(
                "\n=================== ORDER BOOK of " + orderBook.getSymbol() + " ===================\n"
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
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getAskOrderSortedMap().entrySet() ) {

            for(GenOrder order : entry.getValue()) {
                displayList.add(new PxVol(order.getOrdPx(), order.getLeavesQty()));
            }
        }
        displayList.sort((o1, o2) -> Double.compare(o2.getPx(), o1.getPx()));
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
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getBidOrderSortedMap().entrySet() ) {

            for(GenOrder order : entry.getValue()) {
                displayList.add(new PxVol(order.getOrdPx(), order.getLeavesQty()));
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

}
