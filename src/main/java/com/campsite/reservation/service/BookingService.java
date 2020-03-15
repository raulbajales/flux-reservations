package com.campsite.reservation.service;

import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Mono;

public interface BookingService {

	Mono<Boolean> isBookingCreationAllowed(Booking booking);

	Mono<Boolean> isBookingModificationAllowed(String bookingId, DateRangeVO newDateRange);
}
