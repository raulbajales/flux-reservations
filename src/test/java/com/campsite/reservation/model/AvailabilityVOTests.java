package com.campsite.reservation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class AvailabilityVOTests {

	@Test
	public void testBuildValidAvailability() {
		LocalDate now = LocalDate.now();
		AvailabilityVO.Builder builder = AvailabilityVO.builder(new DateRangeVO());
		builder.addRange(now, now.plusDays(3));
		builder.addRange(now.plusDays(5), now.plusDays(10));
		builder.addRange(now.plusDays(15), now.plusDays(20));
		AvailabilityVO availability = builder.build();

		assertTrue(availability.getInThisDateRange().getFrom().isEqual(now),
				"Availability 'from' must be set today as default");
		assertEquals(availability.getDatesAvailable().size(), 3, "Availability must contain 3 date ranges");
	}

	@Test
	public void testCannotAddInvalidRange_outOfRange() {
		LocalDate now = LocalDate.now();
		AvailabilityVO.Builder builder = AvailabilityVO.builder(new DateRangeVO(now, now.plusMonths(1)));
		assertThrows(IllegalArgumentException.class, () -> {
			builder.addRange(now.plusYears(1), now.plusYears(1).plusMonths(1));
		});
	}

	@Test
	public void testCannotAddInvalidRange_lastDateRangeIsOpen() {
		LocalDate now = LocalDate.now();
		AvailabilityVO.Builder builder = AvailabilityVO.builder(new DateRangeVO(now, null));
		builder.addRange(now, now.plusDays(2));
		builder.addRange(now.plusDays(5), null);
		assertThrows(IllegalArgumentException.class, () -> {
			builder.addRange(now, now.plusDays(5));
		});
	}

}
