package com.wanted.clone.oneport.core.common;

import com.wanted.clone.oneport.payments.domain.exception.UnsupportedPgCorpException;
import com.wanted.clone.oneport.payments.domain.exception.PaymentRuleViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@ConfigurationProperties("error-trace")
@RestControllerAdvice
@Slf4j
public class GlobalException extends ResponseEntityExceptionHandler {

    private boolean stackTrace;

    @Override
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("BAD_REQUEST ::: ", ex);
        return ResponseEntity.status(status)
                .body(new ErrorResponse(null, ex.getBindingResult().getAllErrors().get(0).getDefaultMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(UnsupportedPgCorpException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public final ErrorResponse handleUnsupportedPgCorpException(UnsupportedPgCorpException ex, WebRequest request) {
        logger.error("BAD_REQUEST ::: [UnsupportedPgCorpException] ", ex);
        return new ErrorResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentRuleViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public final ErrorResponse handlePaymentRuleViolationException(PaymentRuleViolationException ex, WebRequest request) {
        logger.error("BAD_REQUEST ::: [PaymentRuleViolationException] ", ex);
        return new ErrorResponse(null, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final ErrorResponse handleIOExceptions(Exception ex, WebRequest request) {
        List<StackTraceElement> stackTraces = null;
        if (stackTrace) {
            stackTraces = Arrays.asList(ex.getStackTrace());
        }
        logger.error("ERROR ::: [IOException] ", ex);
        return new ErrorResponse(stackTraces, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public final ErrorResponse handleAllExceptions(Exception ex, WebRequest request) {
        List<StackTraceElement> stackTraces = null;
        if (stackTrace) {
            stackTraces = Arrays.asList(ex.getStackTrace());
        }
        logger.error("ERROR ::: [AllException] ", ex);
        return new ErrorResponse(stackTraces, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
