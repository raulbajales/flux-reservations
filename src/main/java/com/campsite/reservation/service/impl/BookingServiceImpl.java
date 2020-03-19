package com.campsite.reservation.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.service.AvailabilityService;
import com.campsite.reservation.service.BookingService;

import reactor.core.publisher.Mono;

@Service
@PropertySource("classpath:application.properties")
public class BookingServiceImpl implements BookingService {

	@Autowired
	AvailabilityService availabilityService;

	@Autowired
	Environment env;

	@Override
	public Mono<Boolean> isBookingCreationAllowed(Booking booking) {
		Assert.notNull(booking, "booking needs to be set");
		DateRangeVO dateRange = booking.getDateRange();
		checkPreconditions(dateRange);
		return availabilityService.calculateAvailability(dateRange)
				.flatMap(availability -> isDateRangeInsideAvailability(dateRange, availability));
	}

	@Override
	public Mono<Boolean> isBookingModificationAllowed(String bookingId, DateRangeVO newDateRange) {
		Assert.notNull(bookingId, "bookingId needs to be set");
		Assert.notNull(newDateRange, "newDateRange needs to be set");
		checkPreconditions(newDateRange);
		return availabilityService.calculateAvailabilityExcluding(bookingId, newDateRange)
				.flatMap(availability -> isDateRangeInsideAvailability(newDateRange, availability));
	}

	private void checkPreconditions(DateRangeVO dateRange) {
		Integer maxBookingDays = env.getProperty("reservation.max-booking-days", Integer.class);
		Integer minDaysAhead = env.getProperty("reservation.min-days-ahead", Integer.class);
		Integer maxDaysAhead = env.getProperty("reservation.max-days-ahead", Integer.class);
		LocalDate now = LocalDate.now();
		if (dateRange.getFrom().isBefore(LocalDate.now()))
			throw new IllegalArgumentException(String.format("Cannot book in the past, for %s", dateRange));
		if (dateRange.totalDays() > maxBookingDays)
			throw new IllegalArgumentException(
					String.format("Cannot book for more than %d days, for %s", maxBookingDays, dateRange));
		long daysAhead = ChronoUnit.DAYS.between(now, dateRange.getFrom());
		if (!(daysAhead >= minDaysAhead && (maxDaysAhead == -1 || daysAhead <= maxDaysAhead)))
			throw new IllegalArgumentException(
					String.format("Minimum %d day(s) ahead of arrival and up to %d days in advance, for %s",
							minDaysAhead, maxDaysAhead, dateRange));
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
