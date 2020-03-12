package com.campsite.reservation.service;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepository;
import com.campsite.reservation.service.impl.ReservationServiceImpl;

@SpringBootTest
public class ReservationServiceTests {

	@Mock
	BookingRepository bookingRepository;

	@Mock
	BookingService bookingService;

	@Mock
	AvailabilityService availabilityService;

	@InjectMocks
	ReservationService service = new ReservationServiceImpl();

	@Test
	public void testFindAvailability_dateRangeIsClosed() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusMonths(3));

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
		DateRangeVO dateRange = new DateRangeVO(now, null);

		//
		// When
		//
		service.findAvailability(dateRange);

		//
		// Then
		//
		verify(availabilityService).calculateAvailability(new DateRangeVO(now, now.plusMonths(1)));

	}
	
	// WIP
}
