package com.campsite.reservation.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Example;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.campsite.reservation.exception.BookingNotFoundException;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

@ExtendWith(SpringExtension.class)
@DataMongoTest
public class BookingRepositoryCustomTests {

	@Autowired
	BookingRepository repository;

	@BeforeEach
	public void cleanUp() {
		repository.deleteAll().block();
	}

	@SuppressWarnings("serial")
	@Test
	public void testFindByDateRange() {

		//
		// Given
		//
		String email1 = "email1";
		String email2 = "email2";
		LocalDate now = LocalDate.now();
		List<Booking> bookings = new ArrayList<Booking>() {
			{
				add(new Booking(email1, "fullName1", new DateRangeVO(now.plusDays(3), now.plusDays(5))));
				add(new Booking(email2, "fullName2", new DateRangeVO(now.plusDays(8), now.plusDays(10))));
				add(new Booking("email3", "fullName3", new DateRangeVO(now.plusDays(20), now.plusDays(22))));
			}
		};
		repository.saveAll(bookings).collectList().block();

		//
		// When
		//
		List<Booking> retValue = repository.findByDateRange(new DateRangeVO(now.plusDays(1), now.plusDays(15)))
				.collectList().block();

		//
		// Then
		//
		assertNotNull(retValue);
		assertTrue(!retValue.isEmpty());
		assertEquals(retValue.size(), 2);
		List<String> emailsInRetValue = retValue.stream().map(booking -> booking.getEmail())
				.collect(Collectors.toList());
		assertTrue(emailsInRetValue.contains(email1));
		assertTrue(emailsInRetValue.contains(email2));
	}

	@SuppressWarnings("serial")
	@Test
	public void testFindByDateRangeExcluding() {

		//
		// Given
		//
		String email1 = "email1";
		String email2 = "email2";
		LocalDate now = LocalDate.now();
		Booking bookingToExclude = new Booking(email2, "fullName2", new DateRangeVO(now.plusDays(8), now.plusDays(10)));
		List<Booking> bookings = new ArrayList<Booking>() {
			{
				add(new Booking(email1, "fullName1", new DateRangeVO(now.plusDays(3), now.plusDays(5))));
				add(bookingToExclude);
				add(new Booking("email3", "fullName3", new DateRangeVO(now.plusDays(20), now.plusDays(22))));
			}
		};
		repository.saveAll(bookings).collectList().block();
		String bookingIdToExclude = repository.findOne(Example.of(bookingToExclude)).block().getId();

		//
		// When
		//
		List<Booking> retValue = repository
				.findByDateRangeExcluding(new DateRangeVO(now.plusDays(1), now.plusDays(15)), bookingIdToExclude)
				.collectList().block();

		//
		// Then
		//
		assertNotNull(retValue);
		assertTrue(!retValue.isEmpty());
		assertEquals(retValue.size(), 1);
		List<String> emailsInRetValue = retValue.stream().map(booking -> booking.getEmail())
				.collect(Collectors.toList());
		assertTrue(emailsInRetValue.contains(email1));
	}

	@SuppressWarnings("serial")
	@Test
	public void testDeleteById_ok() {

		//
		// Given
		//
		String email1 = "email1";
		String email2 = "email2";
		String email3 = "email3";
		LocalDate now = LocalDate.now();
		Booking bookingToDelete = new Booking(email2, "fullName2", new DateRangeVO(now.plusDays(8), now.plusDays(10)));
		List<Booking> bookings = new ArrayList<Booking>() {
			{
				add(new Booking(email1, "fullName1", new DateRangeVO(now.plusDays(3), now.plusDays(5))));
				add(bookingToDelete);
				add(new Booking(email3, "fullName3", new DateRangeVO(now.plusDays(20), now.plusDays(22))));
			}
		};
		repository.saveAll(bookings).collectList().block();
		String bookingIdToDelete = repository.findOne(Example.of(bookingToDelete)).block().getId();

		//
		// When
		//
		repository.customDeleteById(bookingIdToDelete).block();

		//
		// Then
		//
		List<Booking> remaining = repository.findAll().collectList().block();
		assertNotNull(remaining);
		assertTrue(!remaining.isEmpty());
		assertEquals(remaining.size(), 2);
		List<String> emailsInRetValue = remaining.stream().map(booking -> booking.getEmail())
				.collect(Collectors.toList());
		assertTrue(emailsInRetValue.contains(email1));
		assertTrue(emailsInRetValue.contains(email3));
	}

	@Test
	public void testDeleteById_notFound() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		repository.save(new Booking("email", "fullName", new DateRangeVO(now.plusDays(8), now.plusDays(10)))).block();
		String nonExistentBookingId = LocalDate.now().toString();

		//
		// When / Then
		//
		assertThrows(BookingNotFoundException.class, () -> repository.customDeleteById(nonExistentBookingId).block());
	}

	@SuppressWarnings("serial")
	@Test
	public void testFindById_ok() {

		//
		// Given
		//
		String email2 = "email2";
		LocalDate now = LocalDate.now();
		Booking bookingToGet = new Booking(email2, "fullName2", new DateRangeVO(now.plusDays(8), now.plusDays(10)));
		List<Booking> bookings = new ArrayList<Booking>() {
			{
				add(new Booking("email1", "fullName1", new DateRangeVO(now.plusDays(3), now.plusDays(5))));
				add(bookingToGet);
				add(new Booking("email3", "fullName3", new DateRangeVO(now.plusDays(20), now.plusDays(22))));
			}
		};
		repository.saveAll(bookings).collectList().block();
		String bookingIdToGet = repository.findOne(Example.of(bookingToGet)).block().getId();

		//
		// When
		//
		Booking booking = repository.customFindById(bookingIdToGet).block();

		//
		// Then
		//
		assertNotNull(booking);
		assertTrue(booking.getEmail().equals(email2));
	}

	@Test
	public void testFindById_notFound() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		repository.save(new Booking("email", "fullName", new DateRangeVO(now.plusDays(8), now.plusDays(10)))).block();
		String nonExistentBookingId = LocalDate.now().toString();

		//
		// When / Then
		//
		assertThrows(BookingNotFoundException.class, () -> repository.customFindById(nonExistentBookingId).block());
	}
}
