package com.campsite.reservation.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;

import reactor.core.publisher.Mono;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

	@Autowired
	BookingRepository bookingRepository;

	@Override
	public Mono<AvailabilityVO> calculateAvailability(DateRangeVO inThisDateRange) {
		return bookingRepository.findByDateRange(inThisDateRange).collectList().map(bookings -> {
			return calculateFor(inThisDateRange, bookings);
		});
	}

	public Mono<AvailabilityVO> calculateAvailabilityExcluding(Booking booking, DateRangeVO inThisDateRange) {
		return bookingRepository.findByDateRangeExcluding(inThisDateRange, booking.getId()).collectList()
				.map(bookings -> {
					return calculateFor(inThisDateRange, bookings);
				});
	}

	@Override
	public Mono<Boolean> isCreationAllowed(Booking booking) {
		return calculateAvailability(booking.getDateRange()).flatMap(availability -> {
			return checkBooking(booking, availability);
		});
	}

	@Override
	public Mono<Boolean> isModificationAllowed(Booking booking, DateRangeVO newDateRange) {
		return calculateAvailabilityExcluding(booking, booking.getDateRange()).flatMap(availability -> {
			return checkBooking(booking, availability);
		});
	}

	private Mono<Boolean> checkBooking(Booking booking, AvailabilityVO availability) {
		for (DateRangeVO d : availability.getDatesAvailable()) {
			if (booking.getDateRange().isInsideRange(d))
				return Mono.just(Boolean.TRUE);
		}
		return Mono.just(Boolean.FALSE);
	}

	private AvailabilityVO calculateFor(DateRangeVO inThisDateRange, List<Booking> bookings) {
		AvailabilityVO.Builder builder = AvailabilityVO.builder(inThisDateRange);
		DateRangeVO dateRangeToProcess = inThisDateRange;
		for (Booking b : bookings) {
			Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> pair = dateRangeToProcess.minus(b.getDateRange());
			if (pair.getFirst() != null)
				builder.addRange(pair.getFirst().get());
			dateRangeToProcess = pair.getSecond().get();
		}
		if (dateRangeToProcess != null)
			builder.addRange(dateRangeToProcess);
		return builder.build();
	}

}
