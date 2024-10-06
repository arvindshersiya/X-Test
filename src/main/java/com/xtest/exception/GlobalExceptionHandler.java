package com.xtest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(XTestException.class)
    public ResponseEntity<Map<String,Object>> XTestExceptionHandler(XTestException e){
        return ResponseEntity.status(HttpStatus.OK).body(new HashMap<>(Map.of("code",e.getCode(),"message",e.getErrMessage())));
    }

}
