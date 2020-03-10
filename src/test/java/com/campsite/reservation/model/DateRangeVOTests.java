package com.campsite.reservation.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

public class DateRangeVOTests {

	@Test
	public void testCreateDefaultDateRange() {
		DateRangeVO dateRange = new DateRangeVO();
		assertNotNull(dateRange.getFrom(), "from not set");
		assertNull(dateRange.getTo(), "to must be null since it was not set");
		assertTrue(dateRange.getFrom().isEqual(LocalDate.now()), "from must be set to today as default");
	}

	@Test
	public void testCannotCreateInvalidDateRange() {
		LocalDate now = LocalDate.now();
		assertThrows(IllegalArgumentException.class, () -> new DateRangeVO(now, now.minusDays(5)));
	}

	@Test
	public void testIsOpen() {
		LocalDate now = LocalDate.now();
		assertTrue(new DateRangeVO(now, null).isOpen(), "isOpen must be true when to is null");
		assertFalse(new DateRangeVO(now, now.plusDays(1)).isOpen(), "isOpen must be false when to is not null");
	}

	@Test
	public void testIsInsideRange() {
		LocalDate now = LocalDate.now();
		DateRangeVO oneMonthRange = new DateRangeVO(now, now.plusMonths(1));

		// All inside
		assertTrue(new DateRangeVO(now, now.plusMonths(1)).isInsideRange(oneMonthRange));
		assertTrue(new DateRangeVO(now, now.plusDays(1)).isInsideRange(oneMonthRange));
		assertTrue(new DateRangeVO(now.plusDays(1), now.plusMonths(1)).isInsideRange(oneMonthRange));

		// All outside
		assertFalse(new DateRangeVO(now.minusMonths(1), now.minusMonths(1).plusDays(1)).isInsideRange(oneMonthRange));
		assertFalse(new DateRangeVO(now.plusMonths(1).plusDays(1), now.plusMonths(1).plusDays(10))
				.isInsideRange(oneMonthRange));

		// Part inside part outside
		assertFalse(new DateRangeVO(now.minusMonths(1), now.plusMonths(1)).isInsideRange(oneMonthRange));
		assertFalse(new DateRangeVO(now.minusMonths(1), now.plusMonths(1).plusDays(10)).isInsideRange(oneMonthRange));
		assertFalse(new DateRangeVO(now.minusMonths(1), now.plusDays(10)).isInsideRange(oneMonthRange));
		assertFalse(new DateRangeVO(now.minusMonths(1).plusDays(1), now.plusMonths(2)).isInsideRange(oneMonthRange));
	}

	@Test
	public void testMinus() {
		LocalDate now = LocalDate.now();
		DateRangeVO oneMonthRange = new DateRangeVO(now, now.plusMonths(1));

		// The date range to substract is inside
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case1 = oneMonthRange
				.minus(new DateRangeVO(now.plusDays(10), now.plusMonths(1).minusDays(10)));
		assertTrue(case1.getFirst().get().equals(new DateRangeVO(now, now.plusDays(10))));
		assertTrue(case1.getSecond().get().equals(new DateRangeVO(now.plusMonths(1).minusDays(10), now.plusMonths(1))));

		// The date range to substract is outside (in the past)
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case2 = oneMonthRange
				.minus(new DateRangeVO(now.minusMonths(2), now.minusMonths(1)));
		assertEquals(case2.getFirst(), Optional.empty());
		assertTrue(case2.getSecond().get().equals(oneMonthRange));

		// The date range to substract is outside (in the future)
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case3 = oneMonthRange
				.minus(new DateRangeVO(now.plusMonths(2), now.plusMonths(3)));
		assertEquals(case3.getFirst(), Optional.empty());
		assertTrue(case3.getSecond().get().equals(oneMonthRange));

		// The date range to substract is outside (in the past), but in the limit
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case4 = oneMonthRange
				.minus(new DateRangeVO(now.minusMonths(1), now));
		assertEquals(case4.getFirst(), Optional.empty());
		assertTrue(case4.getSecond().get().equals(oneMonthRange));

		// The date range to substract is outside (in the future), but in the limit
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case5 = oneMonthRange
				.minus(new DateRangeVO(now.plusMonths(1), now.plusMonths(2)));
		assertTrue(case5.getFirst().get().equals(oneMonthRange));
		assertEquals(case5.getSecond(), Optional.empty());
		
		// The date range to substract is outside (in the past), but with part inside
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case6 = oneMonthRange
				.minus(new DateRangeVO(now.minusMonths(1), now.plusDays(10)));
		assertEquals(case6.getFirst(), Optional.empty());
		assertTrue(case6.getSecond().get().equals(new DateRangeVO(now.plusDays(10), now.plusMonths(1))));

		// The date range to substract is outside (in the future), but with part inside
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case7 = oneMonthRange
				.minus(new DateRangeVO(now.plusDays(10), now.plusMonths(1).plusDays(10)));
		assertTrue(case7.getFirst().get().equals(new DateRangeVO(now, now.plusDays(10))));
		assertEquals(case7.getSecond(), Optional.empty());
		
		// The date range to substract is bigger and contains the other inside
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case8 = oneMonthRange
				.minus(new DateRangeVO(now.minusDays(10), now.plusMonths(1).plusDays(10)));
		assertEquals(case8.getFirst(), Optional.empty());
		assertEquals(case8.getSecond(), Optional.empty());
		
		// The date range to substract the same as the other
		Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> case9 = oneMonthRange
				.minus(oneMonthRange);
		assertEquals(case9.getFirst(), Optional.empty());
		assertEquals(case9.getSecond(), Optional.empty());
	}
}
