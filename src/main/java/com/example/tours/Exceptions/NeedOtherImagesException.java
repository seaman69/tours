package com.example.tours.Exceptions;

public class NeedOtherImagesException extends Exception{
    public NeedOtherImagesException(String msg, Throwable cse){
        super(msg,cse);
    }
}
