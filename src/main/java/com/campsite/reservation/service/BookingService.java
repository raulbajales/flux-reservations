package com.campsite.reservation.service;

import javax.validation.constraints.NotNull;

import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Mono;

public interface BookingService {

	Mono<Boolean> isBookingCreationAllowed(@NotNull Booking booking);

	Mono<Boolean> isBookingModificationAllowed(@NotNull Booking booking, @NotNull DateRangeVO newDateRange);
}
