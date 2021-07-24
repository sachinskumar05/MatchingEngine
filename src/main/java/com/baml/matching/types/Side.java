package com.baml.matching.types;

public enum Side {
    BUY(1), SELL(2);
    private final int fixValue;
    Side(int fixSide) {
        this.fixValue = fixSide;
    }
    public int getFixValue(){ return fixValue;}
}
