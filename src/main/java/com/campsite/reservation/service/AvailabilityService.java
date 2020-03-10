package com.campsite.reservation.service;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Mono;

public interface AvailabilityService {

	Mono<AvailabilityVO> calculateAvailability(DateRangeVO dateRange);

	Mono<Boolean> isCreationAllowed(Booking booking);

	Mono<Boolean> isModificationAllowed(Booking booking, DateRangeVO newDateRange);
}
