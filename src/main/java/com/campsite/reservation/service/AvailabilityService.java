package com.campsite.reservation.service;

import javax.validation.constraints.NotNull;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Mono;

public interface AvailabilityService {

	Mono<AvailabilityVO> calculateAvailability(@NotNull DateRangeVO inThisDateRange);

	Mono<AvailabilityVO> calculateAvailabilityExcluding(@NotNull String bookingId,
			@NotNull DateRangeVO inThisDateRange);
}
