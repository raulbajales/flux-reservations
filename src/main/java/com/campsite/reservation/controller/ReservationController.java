package com.campsite.reservation.controller;

import java.net.URI;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	ReservationService reservationService;

	@GetMapping
	public Mono<ResponseEntity<AvailabilityVO>> findAvailability(
			@RequestParam(value = "from", required = false) LocalDate from,
			@RequestParam(value = "to", required = false) LocalDate to) {
		return reservationService
				.findAvailability(new DateRangeVO(from != null ? from : LocalDate.now(),
						to != null ? to : LocalDate.now().plusMonths(1)))
				.map(availability -> ResponseEntity.ok(availability)).defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Mono<ResponseEntity<URI>> makeReservation(@RequestBody Booking booking) {
		return reservationService.makeReservation(booking)
				.map(bookingId -> ResponseEntity.created(URI.create("/reservations/" + bookingId)).build());
	}

	@GetMapping("/{bookingId}")
	public Mono<ResponseEntity<Booking>> getReservationInfo(@PathVariable String bookingId) {
		return reservationService.getReservationInfo(bookingId).map(booking -> ResponseEntity.ok(booking))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{bookingId}")
	public Mono<ResponseEntity<Void>> cancelReservation(@PathVariable String bookingId) {
		return reservationService.cancelReservation(bookingId).then(Mono.just(ResponseEntity.ok().<Void>build()));
	}

	@PutMapping("/{bookingId}")
	public Mono<ResponseEntity<Booking>> modifyReservation(@PathVariable String bookingId,
			@RequestBody DateRangeVO newDateRange) {
		return reservationService.modifyReservation(bookingId, newDateRange).map(booking -> ResponseEntity.ok(booking))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
