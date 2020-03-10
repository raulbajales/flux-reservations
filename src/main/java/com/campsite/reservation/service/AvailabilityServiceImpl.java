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

	protected Mono<AvailabilityVO> calculateAvailabilityExcluding(String bookingId, DateRangeVO inThisDateRange) {
		return bookingRepository.findByDateRangeExcluding(inThisDateRange, bookingId).collectList().map(bookings -> {
			return calculateFor(inThisDateRange, bookings);
		});
	}

	@Override
	public Mono<Boolean> isCreationAllowed(Booking booking) {
		DateRangeVO dateRange = booking.getDateRange();
		return calculateAvailability(dateRange)
				.flatMap(availability -> isDateRangeInsideAvailability(dateRange, availability));
	}

	@Override
	public Mono<Boolean> isModificationAllowed(Booking booking, DateRangeVO newDateRange) {
		return calculateAvailabilityExcluding(booking.getId(), newDateRange)
				.flatMap(availability -> isDateRangeInsideAvailability(newDateRange, availability));
	}

	private AvailabilityVO calculateFor(DateRangeVO inThisDateRange, List<Booking> bookings) {
		AvailabilityVO.Builder builder = AvailabilityVO.builder(inThisDateRange);
		Optional<DateRangeVO> dateRangeToProcess = Optional.of(inThisDateRange);
		for (Booking b : bookings) {
			if (dateRangeToProcess.isPresent()) {
				Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> pair = dateRangeToProcess.get()
						.minus(b.getDateRange());
				if (pair.getFirst().isPresent())
					builder.addRange(pair.getFirst().get());
				dateRangeToProcess = pair.getSecond();
			}
		}
		if (dateRangeToProcess.isPresent())
			builder.addRange(dateRangeToProcess.get());
		return builder.build();
	}

	private Mono<Boolean> isDateRangeInsideAvailability(DateRangeVO dateRange, AvailabilityVO availability) {
		if (availability.getDatesAvailable() != null)
			for (DateRangeVO d : availability.getDatesAvailable()) {
				if (dateRange.isInsideRange(d))
					return Mono.just(Boolean.TRUE);
			}
		return Mono.just(Boolean.FALSE);
	}
}
