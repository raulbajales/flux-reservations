package com.campsite.reservation.service;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Mono;

public interface ReservationService {

	Mono<AvailabilityVO> findAvailability(DateRangeVO dateRange);

	Mono<Booking> makeReservation(Booking booking);

	Mono<Booking> modifyReservation(String bookingId, DateRangeVO newDateRange);

	Mono<Booking> getReservationInfo(String bookingId);

	Mono<Boolean> cancelReservation(String bookingId);
}
