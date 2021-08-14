package com.sk.matching.exchange.order;

import com.sk.matching.symbols.Symbol;
import com.sk.matching.types.Side;
import lombok.extern.log4j.Log4j2;

/**
 * KISS -> keeping final immutable variables public to avoid getter/setter creation..
 * Disclaimer Its not a standard for prod env any delivery however, just ok for small demo
 */

@Log4j2
public class Trade {
    public final long orderId;
    public final Symbol symbol;
    public final double tradePrice;
    public final double tradeQty;
    public final Side tradeSide;
    public final long tradeId;
    public final String counterClOrdIdId;

    public Trade(long orderId, Symbol symbol,
                 double tradePrice, double tradeQty,
                 Side tradeSide, long tradeId, String counterClOrdIdId){
        this.orderId = orderId;
        this.symbol = symbol;
        this.tradePrice = tradePrice;
        this.tradeQty = tradeQty;
        this.tradeSide = tradeSide;
        this.tradeId = tradeId;
        this.counterClOrdIdId = counterClOrdIdId;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "orderId=" + orderId +
                ", equitySymbol=" + symbol +
                ", tradePrice=" + tradePrice +
                ", tradeQty=" + tradeQty +
                ", tradeSide=" + tradeSide +
                ", tradeId=" + tradeId +
                ", counterClOrdIdId=" + counterClOrdIdId +
                '}';
    }
}
