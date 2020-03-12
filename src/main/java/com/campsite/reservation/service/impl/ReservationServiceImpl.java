package com.campsite.reservation.service.impl;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	public Mono<AvailabilityVO> findAvailability(@NotNull DateRangeVO dateRange) {
		return availabilityService.calculateAvailability(!dateRange.isOpen() ? dateRange
				: new DateRangeVO(dateRange.getFrom(), dateRange.getFrom().plusMonths(1)));
	}

	public Mono<String> makeReservation(@NotNull Booking booking) {
		return bookingService.isBookingCreationAllowed(booking).flatMap(isAllowed -> {
			if (isAllowed)
				return bookingRepository.save(booking).map(Booking::getId);
			else
				throw new IllegalArgumentException("No availability");
		});
	}

	public Mono<Booking> modifyReservation(@NotNull String bookingId, @NotNull DateRangeVO newDateRange) {
		return bookingRepository.findById(bookingId).flatMap(booking -> {
			return bookingService.isBookingModificationAllowed(booking, newDateRange).flatMap(isAllowed -> {
				if (isAllowed)
					return bookingRepository.save(Booking.from(booking, newDateRange));
				else
					throw new IllegalArgumentException("No availability");
			});
		});
	}

	public Mono<Booking> getReservationInfo(@NotNull String bookingId) {
		return bookingRepository.findById(bookingId);
	}

	public Mono<Void> cancelReservation(@NotNull String bookingId) {
		return bookingRepository.deleteById(bookingId);
	}

}
