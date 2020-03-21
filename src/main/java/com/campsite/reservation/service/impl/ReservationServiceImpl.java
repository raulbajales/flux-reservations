package com.campsite.reservation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;
import com.campsite.reservation.service.AvailabilityService;
import com.campsite.reservation.service.BookingService;
import com.campsite.reservation.service.ReservationService;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import reactor.core.publisher.Mono;

@Service
@PropertySource("classpath:application.properties")
public class ReservationServiceImpl implements ReservationService {

	@Autowired
	Environment env;

	@Autowired
	BookingService bookingService;

	@Autowired
	AvailabilityService availabilityService;

	@Autowired
	BookingRepository bookingRepository;

	Counter bookingsNotAllowedCounter;
	
	public ReservationServiceImpl(MeterRegistry meterRegistry) {
		bookingsNotAllowedCounter = meterRegistry.counter("reservation.bookings-not-allowed");
	}

	public Mono<AvailabilityVO> findAvailability(DateRangeVO dateRange) {
		Assert.notNull(dateRange, "dateRange needs to be set");
		Integer defaultMonthsForAvailabilityRequest = env
				.getProperty("reservation.default-months-for-availability-request", Integer.class);
		return availabilityService.calculateAvailability(!dateRange.isOpen() ? dateRange
				: new DateRangeVO(dateRange.getFrom(), dateRange.getFrom().plusMonths(defaultMonthsForAvailabilityRequest)));
	}

	public Mono<Booking> makeReservation(Booking booking) {
		Assert.notNull(booking, "booking needs to be set");
		return bookingService.isBookingCreationAllowed(booking).flatMap(isAllowed -> {
			if (isAllowed)
				return bookingRepository.save(booking);
			else {
				bookingsNotAllowedCounter.increment();
				throw new IllegalArgumentException("No availability");
			}
		});
	}

	public Mono<Booking> modifyReservation(String bookingId, DateRangeVO newDateRange) {
		Assert.notNull(bookingId, "bookingId needs to be set");
		Assert.notNull(newDateRange, "newDateRange needs to be set");
		Assert.isTrue(!newDateRange.isOpen(), "newDateRange cannot be open");
		return bookingRepository.customFindById(bookingId).flatMap(booking -> {
			return bookingService.isBookingModificationAllowed(booking.getId(), newDateRange).flatMap(isAllowed -> {
				if (isAllowed)
					return bookingRepository.save(Booking.from(booking, newDateRange));
				else
					throw new IllegalArgumentException("No availability");
			});
		});
	}

	public Mono<Booking> getReservationInfo(String bookingId) {
		Assert.notNull(bookingId, "bookingId needs to be set");
		return bookingRepository.customFindById(bookingId);
	}

	public Mono<Void> cancelReservation(String bookingId) {
		Assert.notNull(bookingId, "bookingId needs to be set");
		return bookingRepository.customDeleteById(bookingId);
	}

}
