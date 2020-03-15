package com.campsite.reservation.service;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Mono;

public interface AvailabilityService {

	Mono<AvailabilityVO> calculateAvailability(DateRangeVO inThisDateRange);

	Mono<AvailabilityVO> calculateAvailabilityExcluding(String bookingId, DateRangeVO inThisDateRange);
}
