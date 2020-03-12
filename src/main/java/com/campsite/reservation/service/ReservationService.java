package com.campsite.reservation.service;

import javax.validation.constraints.NotNull;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Mono;

public interface ReservationService {

	Mono<AvailabilityVO> findAvailability(@NotNull DateRangeVO dateRange);

	Mono<Booking> makeReservation(@NotNull Booking booking);

	Mono<Booking> modifyReservation(@NotNull String bookingId, @NotNull DateRangeVO newDateRange);

	Mono<Booking> getReservationInfo(@NotNull String bookingId);

	Mono<Void> cancelReservation(@NotNull String bookingId);
}
