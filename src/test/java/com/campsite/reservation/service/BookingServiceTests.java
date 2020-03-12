package com.campsite.reservation.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.service.impl.BookingServiceImpl;

import reactor.core.publisher.Mono;

@SpringBootTest
public class BookingServiceTests {

	@Mock
	AvailabilityService availabilityService;

	@InjectMocks
	BookingService service = new BookingServiceImpl();

	@SuppressWarnings("serial")
	@Test
	public void testIsBookingCreationAllowed_fullAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1), now.plusDays(3));
		when(availabilityService.calculateAvailability(eq(dateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(dateRange);
					}
				}, dateRange)));
		//
		// When
		//
		Boolean isAllowed = service.isBookingCreationAllowed(new Booking("email", "fullName", dateRange)).block();

		//
		// Then
		//
		assertTrue(isAllowed);
	}

	@SuppressWarnings("serial")
	@Test
	public void testIsBookingCreationAllowed_someBookings_someAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1), now.plusDays(4));
		when(availabilityService.calculateAvailability(eq(dateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(new DateRangeVO(now.plusDays(1), now.plusDays(2)));
					}
				}, dateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingCreationAllowed(new Booking("email", "fullName", dateRange)).block();

		//
		// Then
		//
		assertFalse(isAllowed);
	}

	@Test
	public void testIsBookingCreationAllowed_allBooked_noAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1), now.plusDays(4));
		when(availabilityService.calculateAvailability(eq(dateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>(), dateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingCreationAllowed(new Booking("email", "fullName", dateRange)).block();

		//
		// Then
		//
		assertFalse(isAllowed);
	}

	@SuppressWarnings("serial")
	@Test
	public void testIsBookingModificationAllowed_fullAvailability() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusDays(3));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10), now.plusDays(10).plusDays(3));
		when(availabilityService.calculateAvailabilityExcluding(eq(bookingId), eq(newDateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(newDateRange);
					}
				}, newDateRange)));

		//
		// When
		//
		Boolean isAllowed = service
				.isBookingModificationAllowed(new Booking(bookingId, "email", "fullName", dateRange), newDateRange)
				.block();

		//
		// Then
		//
		assertTrue(isAllowed);
	}

	@SuppressWarnings("serial")
	@Test
	public void testIsBookingModificationAllowed_someBookings_someAvailability() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusDays(3));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(1), now.plusDays(4));
		when(availabilityService.calculateAvailabilityExcluding(eq(bookingId), eq(newDateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(new DateRangeVO(now, now.plusDays(2)));
					}
				}, newDateRange)));

		//
		// When
		//
		Boolean isAllowed = service
				.isBookingModificationAllowed(new Booking(bookingId, "email", "fullName", dateRange), newDateRange)
				.block();

		//
		// Then
		//
		assertFalse(isAllowed);
	}

	@Test
	public void testIsBookingModificationAllowed_allBooked_noAvailability() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1), now.plusDays(2));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10), now.plusDays(11));
		when(availabilityService.calculateAvailabilityExcluding(eq(bookingId), eq(newDateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>(), newDateRange)));

		//
		// When
		//
		Boolean isAllowed = service
				.isBookingModificationAllowed(new Booking(bookingId, "email", "fullName", dateRange), newDateRange)
				.block();

		//
		// Then
		//
		assertFalse(isAllowed);
	}

	@Test
	public void testIsBookingCreationAllowed_cannotBookMoreThanMaxDays() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusDays(4));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(new Booking("email", "fullName", dateRange)).block();
		});
	}

	@Test
	public void testIsBookingModificationAllowed_cannotBookMoreThanMaxDays() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusDays(1));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10), now.plusDays(14));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(new Booking(bookingId, "email", "fullName", dateRange), newDateRange)
					.block();
		});
	}
	
	@Test
	public void testIsBookingCreationAllowed_cannotBookInThePast() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.minusDays(1), now.plusDays(1));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(new Booking("email", "fullName", dateRange)).block();
		});
	}

	@Test
	public void testIsBookingModificationAllowed_cannotBookInThePast() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusDays(1));
		DateRangeVO newDateRange = new DateRangeVO(now.minusDays(1), now.plusDays(1));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(new Booking(bookingId, "email", "fullName", dateRange), newDateRange)
					.block();
		});
	}

	@Test
	public void testIsBookingCreationAllowed_checkMinDaysAhead() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusDays(1));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(new Booking("email", "fullName", dateRange)).block();
		});
	}

	@Test
	public void testIsBookingModificationAllowed_checkMinDaysAhead() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1), now.plusDays(3));
		DateRangeVO newDateRange = new DateRangeVO(now, now.plusDays(1));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(new Booking(bookingId, "email", "fullName", dateRange), newDateRange)
					.block();
		});
	}

	@Test
	public void testIsBookingCreationAllowed_checkMaxDaysAhead() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1), now.plusDays(33));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(new Booking("email", "fullName", dateRange)).block();
		});
	}

	@Test
	public void testIsBookingModificationAllowed_checkMaxDaysAhead() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1), now.plusDays(3));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(31), now.plusDays(33));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(new Booking(bookingId, "email", "fullName", dateRange), newDateRange)
					.block();
		});
	}
}

