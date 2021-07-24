package com.baml.matching.types;

public enum OrderType {//FixTag#40

    MARKET(1), LIMIT(2);
    private final int fixValue;
    OrderType(int ordTyp) {
        fixValue = ordTyp;
    }

    public int getFixValue(){return fixValue;}

}
