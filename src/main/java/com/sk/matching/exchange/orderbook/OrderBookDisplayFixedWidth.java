package com.sk.matching.exchange.orderbook;

import com.sk.matching.exchange.order.GenOrder;
import lombok.extern.log4j.Log4j2;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The order book output should be formatted to a fixed width using the following template:
 * Buyers               Sellers
 * 000,000,000 000000 | 000000 000,000,000
 * Please note
 */
@Log4j2
public class OrderBookDisplayFixedWidth implements OrderBookDisplay {

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
        .append("\t\tBids (buying)\t\t\t\t\t\tAsks (selling)\t\t\t")
        .append("Volume\t\tPrice\t\t\tPrice\t\t\tVolume")
        .append(formatAsk(orderBook))
        .append(formatBid(orderBook))
        .append("\n")
        .append("\n=================== END of ORDER BOOK ===================");
        return sb.toString();

    }

    private String formatAsk(OrderBook orderBook) {
        StringBuilder sb = new StringBuilder("\n");
        List<GenOrder> res = new ArrayList<>();
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getAskOrderSortedMap().entrySet() ) {
            List<GenOrder> genOrderList = entry.getValue();
            res.addAll(genOrderList);
        }
        Collections.reverse(res);
        for(GenOrder genOrder : res) {
            sb.append(genOrder.getOrderId()).append("\t")
                    .append("    \t    \t   \t\t\t\t")
                    .append(DECIMAL_FORMAT.format(genOrder.getOrdPx())).append("\t")
                    .append(DECIMAL_TO_INT_FORMAT.format(genOrder.getLeavesQty())).append("\t")
                    .append(genOrder.getReceivedTS()).append("\t\t")
                    .append(genOrder.getSide());
            sb.append("\n");
        }
        return sb.toString();
    }

    private String formatBid(OrderBook orderBook) {
        StringBuilder sb = new StringBuilder();
        List<GenOrder> res = new ArrayList<>();
        for ( Map.Entry<Double, List<GenOrder>> entry : orderBook.getBidOrderSortedMap().entrySet() ) {
            List<GenOrder> genOrderList = new ArrayList<>(entry.getValue());
            Collections.reverse(genOrderList);
            res.addAll(genOrderList);
        }
        Collections.reverse(res);
        for(GenOrder genOrder : res) {
            sb.append(genOrder.getOrderId()).append("\t")
                    .append(genOrder.getSide()).append("\t")
                    .append(genOrder.getReceivedTS()).append("\t")
                    .append(DECIMAL_TO_INT_FORMAT.format(genOrder.getLeavesQty())).append("\t\t")
                    .append(DECIMAL_FORMAT.format(genOrder.getOrdPx())).append("\t")
                    .append("  \t    \t    \t   \t");
            sb.append("\n");
        }

        return sb.toString();
    }

}
