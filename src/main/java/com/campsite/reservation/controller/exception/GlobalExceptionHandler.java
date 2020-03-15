package com.campsite.reservation.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.campsite.reservation.exception.BookingNotFoundException;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(BookingNotFoundException.class)
	public final Mono<ResponseEntity<String>> handleBookingNotFoundException(BookingNotFoundException ex) {
		return Mono.<ResponseEntity<String>>just(new ResponseEntity<String>(ex.getMessage(), HttpStatus.NOT_FOUND));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public final Mono<ResponseEntity<String>> handleAIllegalArgumentException(IllegalArgumentException ex) {
		return Mono.<ResponseEntity<String>>just(new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST));
	}
}
