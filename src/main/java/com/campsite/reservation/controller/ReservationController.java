package com.campsite.reservation.controller;

import java.net.URI;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.service.ReservationService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

	private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

	@Autowired
	ReservationService reservationService;

	@GetMapping
	public Mono<ResponseEntity<AvailabilityVO>> findAvailability(
			@RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
			@RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {
		LOG.info("findAvailability from: {}, to: {}", from, to);
		return reservationService.findAvailability(new DateRangeVO(from, to))
				.map(availability -> ResponseEntity.ok(availability));
	}

	@PostMapping
	public Mono<ResponseEntity<Void>> makeReservation(@RequestBody Booking booking) {
		LOG.info("makeReservation booking: {}", booking);
		return reservationService.makeReservation(booking).map(booked -> ResponseEntity
				.created(URI.create(String.format("/reservations/%s", booked.getId()))).build());
	}

	@GetMapping("/{bookingId}")
	public Mono<ResponseEntity<Booking>> getReservationInfo(@PathVariable String bookingId) {
		LOG.info("getReservationInfo bookingId: {}", bookingId);
		return reservationService.getReservationInfo(bookingId).map(booking -> ResponseEntity.ok(booking))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{bookingId}")
	public Mono<ResponseEntity<Void>> cancelReservation(@PathVariable String bookingId) {
		LOG.debug("cancelReservation bookingId: {}", bookingId);
		return reservationService.cancelReservation(bookingId).then(Mono.just(ResponseEntity.ok().<Void>build()));
	}

	@PutMapping("/{bookingId}")
	public Mono<ResponseEntity<Booking>> modifyReservation(@PathVariable String bookingId,
			@RequestBody DateRangeVO newDateRange) {
		LOG.info("modifyReservation bookingId: {}, newDateRange: {}", bookingId, newDateRange);
		return reservationService.modifyReservation(bookingId, newDateRange).map(booking -> ResponseEntity.ok(booking));
	}
}
