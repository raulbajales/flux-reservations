package com.campsite.reservation.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;
import com.campsite.reservation.service.impl.ReservationServiceImpl;

import reactor.core.publisher.Mono;

@SpringBootTest
@TestPropertySource("classpath:test.properties")
public class ReservationServiceTests {

	@Autowired
	Environment env;

	@Mock
	BookingRepository bookingRepository;

	@Mock
	BookingService bookingService;

	@Mock
	AvailabilityService availabilityService;

	@InjectMocks
	ReservationService service = new ReservationServiceImpl();

	Integer maxBookingDays;
	Integer minDaysAhead;
	Integer maxDaysAhead;

	@BeforeEach
	public void beforeEach() {
		maxBookingDays = env.getProperty("reservation.max-booking-days", Integer.class);
		minDaysAhead = env.getProperty("reservation.min-days-ahead", Integer.class);
		maxDaysAhead = env.getProperty("reservation.max-days-ahead", Integer.class);
	}

	@Test
	public void testFindAvailability_dateRangeIsClosed() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead),
				now.plusDays(minDaysAhead + maxBookingDays));

		//
		// When
		//
		service.findAvailability(dateRange);

		//
		// Then
		//
		verify(availabilityService).calculateAvailability(dateRange);
	}

	@Test
	public void testFindAvailability_dateRangeIsOpen() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead), null);

		//
		// When
		//
		service.findAvailability(dateRange);

		//
		// Then
		//
		verify(availabilityService).calculateAvailability(
				new DateRangeVO(now.plusDays(minDaysAhead), now.plusDays(minDaysAhead).plusMonths(1)));
	}

	@Test
	public void testMakeReservation_allowed() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead),
				now.plusDays(minDaysAhead + maxBookingDays));
		Booking booking = new Booking("id", "email", "fullName", dateRange);
		doReturn(Mono.<Boolean>just(Boolean.TRUE)).when(bookingService).isBookingCreationAllowed(eq(booking));
		doReturn(Mono.<Booking>just(booking)).when(bookingRepository).save(eq(booking));

		//
		// When
		//
		service.makeReservation(booking).block();

		//
		// Then
		//
		verify(bookingService).isBookingCreationAllowed(booking);
		verify(bookingRepository).save(booking);
	}

	@Test
	public void testMakeReservation_notAllowed() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead),
				now.plusDays(minDaysAhead + maxBookingDays));
		Booking booking = new Booking("id", "email", "fullName", dateRange);
		doReturn(Mono.<Boolean>just(Boolean.FALSE)).when(bookingService).isBookingCreationAllowed(eq(booking));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> service.makeReservation(booking).block());
		verify(bookingService).isBookingCreationAllowed(booking);
		verifyNoInteractions(bookingRepository);
	}

	@Test
	public void testModifyReservation_allowed() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead),
				now.plusDays(minDaysAhead + maxBookingDays));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(minDaysAhead + 5),
				now.plusDays(minDaysAhead + maxBookingDays + 5));
		String bookingId = "someBookingId";
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);
		Booking newBooking = Booking.from(booking, newDateRange);
		doReturn(Mono.<Booking>just(booking)).when(bookingRepository).findById(eq(bookingId));
		doReturn(Mono.<Boolean>just(Boolean.TRUE)).when(bookingService).isBookingModificationAllowed(eq(booking),
				eq(newDateRange));
		doReturn(Mono.<Booking>just(newBooking)).when(bookingRepository).save(eq(newBooking));

		//
		// When
		//
		service.modifyReservation(bookingId, newDateRange).block();

		//
		// Then
		//
		verify(bookingRepository).findById(bookingId);
		verify(bookingService).isBookingModificationAllowed(booking, newDateRange);
		verify(bookingRepository).save(booking);
	}

	@Test
	public void testModifyReservation_notAllowed() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead),
				now.plusDays(minDaysAhead + maxBookingDays));
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(minDaysAhead + 5),
				now.plusDays(minDaysAhead + maxBookingDays + 5));
		String bookingId = "someBookingId";
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);
		doReturn(Mono.<Booking>just(booking)).when(bookingRepository).findById(eq(bookingId));
		doReturn(Mono.<Boolean>just(Boolean.FALSE)).when(bookingService).isBookingModificationAllowed(eq(booking),
				eq(newDateRange));

		//
		// When / Then
		//
		assertThrows(IllegalArgumentException.class, () -> service.modifyReservation(bookingId, newDateRange).block());
		verify(bookingRepository).findById(bookingId);
		verify(bookingService).isBookingModificationAllowed(booking, newDateRange);
		verifyNoMoreInteractions(bookingRepository);
	}

	@Test
	public void testGetReservationInfo() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(minDaysAhead),
				now.plusDays(minDaysAhead + maxBookingDays));
		String bookingId = "someBookingId";
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);
		doReturn(Mono.<Booking>just(booking)).when(bookingRepository).findById(eq(bookingId));

		//
		// When
		//
		service.getReservationInfo(bookingId).block();

		//
		// Then
		//
		verify(bookingRepository).findById(bookingId);
	}

	@Test
	public void testCancelReservation() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		doReturn(Mono.<Void>empty()).when(bookingRepository).deleteById(eq(bookingId));

		//
		// When
		//
		service.cancelReservation(bookingId).block();

		//
		// Then
		//
		verify(bookingRepository).deleteById(bookingId);
	}
}
