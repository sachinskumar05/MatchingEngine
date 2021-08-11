package com.sk.matching.exchange.order;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EQOrderRestorePoint {
    public double lastPrice = Double.NaN;
    public double lastQty = Double.NaN;
    public double cumQty = Double.NaN;
    public double leavesQty = Double.NaN;
    public double avgPx = Double.NaN;

    public EQOrderRestorePoint(EQOrder EQOrder) {
        this( EQOrder.getLastPrice(), EQOrder.getLastQty(), EQOrder.getCumQty(),
                EQOrder.getLeavesQty(), EQOrder.getAvgPx());
    }

    private EQOrderRestorePoint(double lastPx, double lastQty, double cumQty, double avgPx, double leavesQty ) {
        this.lastPrice = lastPx;
        this.lastQty = lastQty;
        this.cumQty = cumQty;
        this.avgPx = avgPx;
        this.leavesQty = leavesQty;
    }

    public void reset() {
        this.lastPrice = Double.NaN;
        this.lastQty = Double.NaN;
        this.cumQty = Double.NaN;
        this.avgPx = Double.NaN;
        this.leavesQty = Double.NaN;
    }

    public boolean isReady() {
        return  ( !Double.isNaN(this.lastPrice) ||
                !Double.isNaN(this.lastQty ) ||
                !Double.isNaN(this.cumQty) ||
                !Double.isNaN(this.avgPx) ||
                !Double.isNaN(this.leavesQty) );
    }

}
