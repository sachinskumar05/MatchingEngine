package com.sk.matching.types;

public enum Side {
    BUY(1, 'B', "buy"), SELL(2, 'S', "sell");
    private final int fixSide;
    private final char sideChar;
    private final String sideStr;
    Side(int fixSide, char sideChar, String sideStr) {
        this.fixSide = fixSide;
        this.sideChar = sideChar;
        this.sideStr = sideStr;
    }
    public int getFixSide(){ return fixSide;}

    public static Side valueOf(char side) {
        switch (side) {
            case 'b':
            case 'B':
                return BUY;
            case 's':
            case 'S':
                return SELL;
        }
        throw new UnsupportedOperationException("Unsupported SIDE " + side);
    }

    public static Side valueOf(int side) {
        switch (side) {
            case 1:return BUY;
            case 2:return SELL;
        }
        throw new UnsupportedOperationException("Unsupported SIDE " + side);
    }

}
