package com.agi.aesl.erpscm.exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.xml.bind.ValidationException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
/**
 * Class documentation Comments to be added
 * */
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final String INVALID_PATH = "Invalid Path";
    public static final String MESSAGE_KEY = "message";

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<Object> handleMethodArgumentException(MethodArgumentNotValidException me) {

        Map<String,Object> response = new HashMap<>();
        for (FieldError fe : me.getFieldErrors()) {
            response.put(fe.getField(), fe.getDefaultMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException me) {

        Map<String,Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, INVALID_PATH);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({MissingServletRequestParameterException.class})
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MissingServletRequestParameterException me) {

        Map<String,Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, me.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({ValidationException.class})
    public ResponseEntity<Object> handleValidationExceptions(ValidationException ve) {
        Map<String,Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, ve.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({ExpiredJwtException.class})
    public ResponseEntity<Object> handleValidationExceptions(ExpiredJwtException ve) {
        Map<String,Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, ve.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleValidationExceptions(AccessDeniedException ve) {
        Map<String,Object> response = new HashMap<>();
        response.put(MESSAGE_KEY, ve.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AesException.class, JwtException.class, RuntimeException.class})
    public ResponseEntity<Object> handleAesExceptions(RuntimeException re) {
        Map<String,Object> response = new HashMap<>();
        response.put(MESSAGE_KEY,re.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleAesExceptions(DataIntegrityViolationException re) {
        Map<String,Object> response = new HashMap<>();
        if( re.getRootCause() !=null) {
            response.put(MESSAGE_KEY, re.getRootCause().getLocalizedMessage());
        }
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
