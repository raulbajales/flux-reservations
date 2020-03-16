package com.campsite.reservation.repository;

import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingRepositoryCustom {

	/**
	 * Look for all bookings where: booking.dateRange.to <= to OR
	 * booking.dateRange.from >= from Returns all bookings ordered by dateRange.from
	 */
	Flux<Booking> findByDateRange(DateRangeVO dateRange);

	/**
	 * Same as findByDateRange but excluding a specific bookingId, useful when
	 * looking for availability to modify an existing booking. Returns all bookings
	 * ordered by dateRange.from
	 */
	Flux<Booking> findByDateRangeExcluding(DateRangeVO dateRange, String bookingId);

	/**
	 * Need a custom implementation for deleteById to eventually throw BookingNotFoundException.
	 */
	Mono<Void> customDeleteById(String bookingId);

	/**
	 * Need a custom implementation findById to eventually throw BookingNotFoundException.
	 */
	Mono<Booking> customFindById(String bookingId);
}
