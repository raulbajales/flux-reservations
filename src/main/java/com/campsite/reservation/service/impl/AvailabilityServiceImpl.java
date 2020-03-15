package com.campsite.reservation.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;
import com.campsite.reservation.service.AvailabilityService;

import reactor.core.publisher.Mono;

@Service
public class AvailabilityServiceImpl implements AvailabilityService {

	@Autowired
	BookingRepository bookingRepository;

	@Override
	public Mono<AvailabilityVO> calculateAvailability(DateRangeVO inThisDateRange) {
		Assert.notNull(inThisDateRange, "inThisDateRange needs to be set");
		return bookingRepository.findByDateRange(inThisDateRange).collectList().map(bookings -> {
			return calculateFor(inThisDateRange, bookings);
		});
	}

	@Override
	public Mono<AvailabilityVO> calculateAvailabilityExcluding(String bookingId, DateRangeVO inThisDateRange) {
		Assert.notNull(bookingId, "bookingId needs to be set");
		Assert.notNull(inThisDateRange, "inThisDateRange needs to be set");
		return bookingRepository.findByDateRangeExcluding(inThisDateRange, bookingId).collectList().map(bookings -> {
			return calculateFor(inThisDateRange, bookings);
		});
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

}
