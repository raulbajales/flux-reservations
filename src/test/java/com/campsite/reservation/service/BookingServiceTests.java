package com.campsite.reservation.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.service.impl.BookingServiceImpl;

import reactor.core.publisher.Mono;

@SpringBootTest
@TestPropertySource("classpath:test.properties")
public class BookingServiceTests {

	@Autowired
	Environment env;

	@Mock
	AvailabilityService availabilityService;

	@Autowired
	@InjectMocks
	BookingService service = new BookingServiceImpl();

	Integer maxBookingDays;
	Integer minDaysAhead;
	Integer maxDaysAhead;

	@BeforeEach
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		maxBookingDays = env.getProperty("reservation.max-booking-days", Integer.class);
		minDaysAhead = env.getProperty("reservation.min-days-ahead", Integer.class);
		maxDaysAhead = env.getProperty("reservation.max-days-ahead", Integer.class);
	}

	@SuppressWarnings("serial")
	@Test
	public void testIsBookingCreationAllowed_fullAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + maxBookingDays));
		Booking booking = new Booking("email", "fullName", dateRange);
		when(availabilityService.calculateAvailability(eq(dateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(dateRange);
					}
				}, dateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingCreationAllowed(booking).block();

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
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + maxBookingDays));
		Booking booking = new Booking("email", "fullName", dateRange);
		when(availabilityService.calculateAvailability(eq(dateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + 2)));
					}
				}, dateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingCreationAllowed(booking).block();

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
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + maxBookingDays));
		Booking booking = new Booking("email", "fullName", dateRange);
		when(availabilityService.calculateAvailability(eq(dateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>(), dateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingCreationAllowed(booking).block();

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
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + maxBookingDays));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10 + minDaysAhead), now.plusDays(10 + minDaysAhead + maxBookingDays));
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);
		when(availabilityService.calculateAvailabilityExcluding(eq(bookingId), eq(newDateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(newDateRange);
					}
				}, newDateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingModificationAllowed(booking, newDateRange).block();

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
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + maxBookingDays));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(minDaysAhead + 1), now.plusDays(minDaysAhead + 1 + maxBookingDays));
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);
		when(availabilityService.calculateAvailabilityExcluding(eq(bookingId), eq(newDateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
					{
						add(new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + 2)));
					}
				}, newDateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingModificationAllowed(booking, newDateRange).block();

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
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(1 + minDaysAhead));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(minDaysAhead + 10), now.plusDays(minDaysAhead + 11));
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);
		when(availabilityService.calculateAvailabilityExcluding(eq(bookingId), eq(newDateRange)))
				.thenReturn(Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>(), newDateRange)));

		//
		// When
		//
		Boolean isAllowed = service.isBookingModificationAllowed(booking, newDateRange).block();

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
		DateRangeVO dateRange = new DateRangeVO(now, now.plusDays(maxBookingDays + 1));
		Booking booking = new Booking("email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(booking).block();
		});
	}

	@Test
	public void testIsBookingModificationAllowed_cannotBookMoreThanMaxDays() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + 1));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10), now.plusDays(10 + maxBookingDays + 1));
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(booking, newDateRange).block();
		});
	}

	@Test
	public void testIsBookingCreationAllowed_cannotBookInThePast() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.minusDays(1), now.plusDays(1));
		Booking booking = new Booking("email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(booking).block();
		});
	}

	@Test
	public void testIsBookingModificationAllowed_cannotBookInThePast() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + 2));
		DateRangeVO newDateRange = new DateRangeVO(now.minusDays(1), now.plusDays(minDaysAhead + 1));
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(booking, newDateRange).block();
		});
	}

	@Test
	public void testIsBookingCreationAllowed_checkMinDaysAhead() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead - 1), now.plusDays(minDaysAhead + 2));
		Booking booking = new Booking("email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(booking).block();
		});
	}

	@Test
	public void testIsBookingModificationAllowed_checkMinDaysAhead() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead + 1));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(minDaysAhead - 1), now.plusDays(minDaysAhead + 2));
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(booking, newDateRange).block();
		});
	}

	@Test
	public void testIsBookingCreationAllowed_checkMaxDaysAhead() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(1 + maxDaysAhead), now.plusDays(1 + maxDaysAhead + 1));
		Booking booking = new Booking("email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingCreationAllowed(booking).block();
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
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(1 + maxDaysAhead), now.plusDays(1 + maxDaysAhead + 1));
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> {
			service.isBookingModificationAllowed(booking, newDateRange).block();
		});
	}
}
