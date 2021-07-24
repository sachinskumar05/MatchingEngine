package com.baml.matching.exception;

public class SymbolNotSupportedException extends Exception{

    public SymbolNotSupportedException(){
        this("Failed to create Order ");
    }

    public SymbolNotSupportedException(String message){
        super(message);
    }

}
