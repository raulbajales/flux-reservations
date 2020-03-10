package com.campsite.reservation.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class BookingTests {

	@Test
	public void testPreconditions() {
		assertThrows(IllegalArgumentException.class,
				() -> new Booking("email", "fullname", new DateRangeVO(LocalDate.now(), null)));
	}

}
