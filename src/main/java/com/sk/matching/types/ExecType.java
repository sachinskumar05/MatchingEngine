package com.sk.matching.types;

public enum ExecType {//FixTag#150
    NEW("0"), DFD("3"), CANCEL("4"), REPLACE("5"), PENDING_CAN("6"), REJECTED("8"), FILL("F");
    private String fixValue;
    ExecType(String et) {
        fixValue = et;
    }
    public String getFixValue(){
        return fixValue;
    }
}
