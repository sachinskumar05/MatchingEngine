package com.sk.matching.exception;

public class OrderCreationException extends Exception{

    public OrderCreationException(){
        this("Failed to create Order ");
    }

    public OrderCreationException(String message){
        super(message);
    }

}
