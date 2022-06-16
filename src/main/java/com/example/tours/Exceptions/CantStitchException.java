package com.example.tours.Exceptions;



public class CantStitchException extends Exception{

    public CantStitchException(String msg, Throwable cse){
        super(msg,cse);

    }
}
