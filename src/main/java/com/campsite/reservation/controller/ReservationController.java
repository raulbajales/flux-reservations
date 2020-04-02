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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import reactor.core.publisher.Mono;

@Api(value = "Reservations system")
@RestController
@RequestMapping("/reservations")
public class ReservationController {

	private static final Logger LOG = LoggerFactory.getLogger(ReservationController.class);

	@Autowired
	ReservationService reservationService;

	@ApiOperation(value = "Checks availability for a given date range. By default 'from' is set to today, 'to' to today + 1 month", response = AvailabilityVO.class)
	@GetMapping("/find-availability")
	public Mono<ResponseEntity<AvailabilityVO>> findAvailability(
			@ApiParam(value = "From what date to check for availability?, in 'yyyy-MM-dd' format.", required = false) @RequestParam(value = "from", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
			@ApiParam(value = "To what date to check for availability?, in 'yyyy-MM-dd' format.", required = false) @RequestParam(value = "to", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {
		LOG.info("findAvailability from: {}, to: {}", from, to);
		return reservationService.findAvailability(new DateRangeVO(from, to))
				.map(availability -> ResponseEntity.ok(availability));
	}

	@ApiOperation(value = "Makes a reservation. Reservations should be done minimum 1 day ahead of arrival and up to 30 days in advance, for up to 3 days.")
	@PostMapping("")
	public Mono<ResponseEntity<Void>> makeReservation(@RequestBody Booking booking) {
		LOG.info("makeReservation booking: {}", booking);
		return reservationService.makeReservation(booking).map(booked -> ResponseEntity
				.created(URI.create(String.format("/reservations/%s", booked.getId()))).build());
	}

	@ApiOperation(value = "Shows info for a given reservation, by id.", response = Booking.class)
	@GetMapping("/{bookingId}")
	public Mono<ResponseEntity<Booking>> getReservationInfo(
			@ApiParam(value = "Booking id to look for.", required = true) @PathVariable String bookingId) {
		LOG.info("getReservationInfo bookingId: {}", bookingId);
		return reservationService.getReservationInfo(bookingId).map(booking -> ResponseEntity.ok(booking))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@ApiOperation(value = "Cancels a reservation, by id.")
	@DeleteMapping("/{bookingId}")
	public Mono<ResponseEntity<Void>> cancelReservation(
			@ApiParam(value = "Booking id to cancel.", required = true) @PathVariable String bookingId) {
		LOG.debug("cancelReservation bookingId: {}", bookingId);
		return reservationService.cancelReservation(bookingId).then(Mono.just(ResponseEntity.ok().<Void>build()));
	}

	@ApiOperation(value = "Modifies a reservation by giving a new date range.", response = Booking.class)
	@PutMapping("/{bookingId}")
	public Mono<ResponseEntity<Booking>> modifyReservation(
			@ApiParam(value = "Booking id to look for.", required = true) @PathVariable String bookingId,
			@RequestBody DateRangeVO newDateRange) {
		LOG.info("modifyReservation bookingId: {}, newDateRange: {}", bookingId, newDateRange);
		return reservationService.modifyReservation(bookingId, newDateRange).map(booking -> ResponseEntity.ok(booking));
	}
}
