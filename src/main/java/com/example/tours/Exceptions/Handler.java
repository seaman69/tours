package com.example.tours.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class Handler {
    @ExceptionHandler(DataNotFound.class)
    public ResponseEntity<Object> handleDataNotFound(DataNotFound ex, WebRequest request){
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message","Data not Found");
        body.put("code",69);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataAlraedyExist.class)
    public ResponseEntity<Object> handleDataAlreadyExist(DataAlraedyExist ex, WebRequest request){
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("message","Data already exist");
        body.put("code",177013);
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

}
