package com.campsite.reservation.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;

import reactor.core.publisher.Mono;

@Service
public class ReservationServiceImpl implements ReservationService {

	@Autowired
	BookingRepository bookingRepository;

	@Autowired
	AvailabilityService availabilityService;

	public Mono<AvailabilityVO> findAvailability(DateRangeVO dateRange) {
		return availabilityService.calculateAvailability(dateRange.getTo() != null ? dateRange
				: new DateRangeVO(dateRange.getFrom(), dateRange.getFrom().plusMonths(1)));
	}

	public Mono<String> makeReservation(Booking booking) {
		return availabilityService.isCreationAllowed(booking).flatMap(isAllowed -> {
			if (isAllowed)
				return bookingRepository.save(booking).map(Booking::getId);
			else
				throw new IllegalArgumentException("No availability");
		});
	}

	public Mono<Booking> modifyReservation(String bookingId, DateRangeVO newDateRange) {
		return bookingRepository.findById(bookingId).flatMap(booking -> {
			return availabilityService.isModificationAllowed(booking, newDateRange).flatMap(isAllowed -> {
				if (isAllowed)
					return bookingRepository.save(Booking.from(booking, newDateRange));
				else
					throw new IllegalArgumentException("No availability");
			});
		});
	}

	public Mono<Booking> getReservationInfo(String bookingId) {
		return bookingRepository.findById(bookingId);
	}

	public Mono<Void> cancelReservation(String bookingId) {
		return bookingRepository.deleteById(bookingId);
	}

}
