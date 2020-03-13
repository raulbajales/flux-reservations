package com.campsite.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public final Mono<ResponseEntity<String>> handleException(IllegalArgumentException ex) {
		ResponseEntity<String> data = new ResponseEntity<String>(ex.getMessage(), HttpStatus.BAD_REQUEST);
		return Mono.<ResponseEntity<String>>just(data);
	}
}
