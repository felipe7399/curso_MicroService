package br.com.microservices.orchestrated.productvalidationservice.config.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ExceptionGlobalHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleException (ValidationException validationException){

        //ExceptionDetails exceptionDetails = new ExceptionDetails(1,"a");
        var exceptionDetails = new ExceptionDetails(HttpStatus.BAD_REQUEST.value(),validationException.getMessage());
        return new ResponseEntity<>(exceptionDetails, HttpStatus.BAD_REQUEST);


    }


}
