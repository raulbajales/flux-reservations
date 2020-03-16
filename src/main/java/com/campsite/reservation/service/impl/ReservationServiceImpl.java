package com.campsite.reservation.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;
import com.campsite.reservation.service.AvailabilityService;
import com.campsite.reservation.service.BookingService;
import com.campsite.reservation.service.ReservationService;

import reactor.core.publisher.Mono;

@Service
public class ReservationServiceImpl implements ReservationService {

	@Autowired
	BookingService bookingService;

	@Autowired
	AvailabilityService availabilityService;

	@Autowired
	BookingRepository bookingRepository;

	public Mono<AvailabilityVO> findAvailability(DateRangeVO dateRange) {
		Assert.notNull(dateRange, "dateRange needs to be set");
		return availabilityService.calculateAvailability(!dateRange.isOpen() ? dateRange
				: new DateRangeVO(dateRange.getFrom(), dateRange.getFrom().plusMonths(1)));
	}

	public Mono<Booking> makeReservation(Booking booking) {
		Assert.notNull(booking, "booking needs to be set");
		return bookingService.isBookingCreationAllowed(booking).flatMap(isAllowed -> {
			if (isAllowed)
				return bookingRepository.save(booking);
			else
				throw new IllegalArgumentException("No availability");
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
