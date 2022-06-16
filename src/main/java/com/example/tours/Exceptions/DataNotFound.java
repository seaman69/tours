package com.example.tours.Exceptions;

public class DataNotFound extends RuntimeException{
    public DataNotFound(String msg){
        super(msg);
    }
}
