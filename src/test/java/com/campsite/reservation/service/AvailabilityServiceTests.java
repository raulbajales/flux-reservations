package com.campsite.reservation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;
import com.campsite.reservation.service.impl.AvailabilityServiceImpl;

import reactor.core.publisher.Flux;

@SpringBootTest
public class AvailabilityServiceTests {

	@Mock
	BookingRepository bookingRepository;

	@InjectMocks
	AvailabilityService service = new AvailabilityServiceImpl();

	@Test
	public void testCalculateAvailability_someBookigs_someAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusMonths(3));
		when(bookingRepository.findByDateRange(eq(dateRange))).thenReturn(Flux.<Booking>create(emitter -> {
			emitter.next(new Booking("email1", "fullname1", new DateRangeVO(now.plusDays(5), now.plusDays(10))));
			emitter.next(new Booking("email2", "fullname2",
					new DateRangeVO(now.plusMonths(1), now.plusMonths(1).plusDays(10))));
			emitter.complete();
		}));

		//
		// When
		//
		AvailabilityVO availability = service.calculateAvailability(dateRange).block();

		//
		// Then
		//
		assertNotNull(availability.getInThisDateRange());
		assertTrue(availability.getInThisDateRange().equals(dateRange));
		assertNotNull(availability.getDatesAvailable());
		assertEquals(availability.getDatesAvailable().size(), 3);
		assertTrue(availability.getDatesAvailable()
				.containsAll(Arrays.asList(new DateRangeVO(now, now.plusDays(5)),
						new DateRangeVO(now.plusDays(10), now.plusMonths(1)),
						new DateRangeVO(now.plusMonths(1).plusDays(10), now.plusMonths(3)))));
	}

	@Test
	public void testCalculateAvailability_noBookigs_fullAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusMonths(3));
		when(bookingRepository.findByDateRange(eq(dateRange)))
				.thenReturn(Flux.<Booking>create(emitter -> emitter.complete()));

		//
		// When
		//
		AvailabilityVO availability = service.calculateAvailability(dateRange).block();

		//
		// Then
		//
		assertNotNull(availability.getInThisDateRange());
		assertTrue(availability.getInThisDateRange().equals(dateRange));
		assertNotNull(availability.getDatesAvailable());
		assertEquals(availability.getDatesAvailable().size(), 1);
		assertTrue(availability.getDatesAvailable().contains(dateRange));
	}

	@Test
	public void testCalculateAvailability_allBooked_noAvailability_case1_fitting() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusMonths(3));
		when(bookingRepository.findByDateRange(eq(dateRange))).thenReturn(Flux.<Booking>create(emitter -> {
			emitter.next(new Booking("email1", "fullname1", new DateRangeVO(now, now.plusMonths(1))));
			emitter.next(new Booking("email2", "fullname2", new DateRangeVO(now.plusMonths(1), now.plusMonths(3))));
			emitter.complete();
		}));

		//
		// When
		//
		AvailabilityVO availability = service.calculateAvailability(dateRange).block();

		//
		// Then
		//
		assertNotNull(availability.getInThisDateRange());
		assertTrue(availability.getInThisDateRange().equals(dateRange));
		assertNotNull(availability.getDatesAvailable());
		assertEquals(availability.getDatesAvailable().size(), 0);
	}

	@Test
	public void testCalculateAvailability_allBooked_noAvailability_exceeding() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusMonths(3));
		when(bookingRepository.findByDateRange(eq(dateRange))).thenReturn(Flux.<Booking>create(emitter -> {
			emitter.next(new Booking("email1", "fullname1", new DateRangeVO(now.minusDays(10), now.plusMonths(1))));
			emitter.next(new Booking("email2", "fullname2", new DateRangeVO(now.plusMonths(1), now.plusMonths(4))));
			emitter.complete();
		}));

		//
		// When
		//
		AvailabilityVO availability = service.calculateAvailability(dateRange).block();

		//
		// Then
		//
		assertNotNull(availability.getInThisDateRange());
		assertTrue(availability.getInThisDateRange().equals(dateRange));
		assertNotNull(availability.getDatesAvailable());
		assertEquals(availability.getDatesAvailable().size(), 0);
	}
}
