package com.campsite.reservation.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.service.AvailabilityService;
import com.campsite.reservation.service.BookingService;

import reactor.core.publisher.Mono;

@Service
public class BookingServiceImpl implements BookingService {

	@Autowired
	AvailabilityService availabilityService;

	@Override
	public Mono<Boolean> isBookingCreationAllowed(@NotNull Booking booking) {
		DateRangeVO dateRange = booking.getDateRange();
		checkPreconditions(dateRange);
		return availabilityService.calculateAvailability(dateRange)
				.flatMap(availability -> isDateRangeInsideAvailability(dateRange, availability));
	}

	@Override
	public Mono<Boolean> isBookingModificationAllowed(@NotNull Booking booking, @NotNull DateRangeVO newDateRange) {
		checkPreconditions(newDateRange);
		return availabilityService.calculateAvailabilityExcluding(booking.getId(), newDateRange)
				.flatMap(availability -> isDateRangeInsideAvailability(newDateRange, availability));
	}

	private void checkPreconditions(DateRangeVO dateRange) {
		LocalDate now = LocalDate.now();
		if (dateRange.getFrom().isBefore(LocalDate.now()))
			throw new IllegalArgumentException(String.format("Cannot book in the past, for %s", dateRange));
		if (dateRange.totalDays() > 3)
			throw new IllegalArgumentException(String.format("Cannot book for more than 3 days, for %s", dateRange));
		long daysAhead = ChronoUnit.DAYS.between(now, dateRange.getFrom());
		if (!(daysAhead >= 1 && daysAhead <= 30))
			throw new IllegalArgumentException(
					String.format("Minimum 1 day(s) ahead of arrival and up to 1 month in advance, for %s", dateRange));
	}

	private Mono<Boolean> isDateRangeInsideAvailability(DateRangeVO dateRange, AvailabilityVO availability) {
		if (availability.getDatesAvailable() != null)
			for (DateRangeVO dr : availability.getDatesAvailable()) {
				if (dateRange.isInsideRange(dr))
					return Mono.just(Boolean.TRUE);
			}
		return Mono.just(Boolean.FALSE);
	}
}
