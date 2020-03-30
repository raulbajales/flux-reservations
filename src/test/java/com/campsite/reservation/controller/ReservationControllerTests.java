package com.campsite.reservation.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.campsite.reservation.exception.BookingNotFoundException;
import com.campsite.reservation.model.AvailabilityVO;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.service.ReservationService;

import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ReservationController.class)
@PropertySource("classpath:test.properties")
public class ReservationControllerTests {

	@Autowired
	Environment env;

	@MockBean
	ReservationService reservationService;

	@Autowired
	WebTestClient webClient;

	@SuppressWarnings("serial")
	@Test
	public void testFindAvailability_noDateRangeSet() {

		//
		// Given
		//
		Integer defaultMonthsForAvailabilityRequest = env
				.getProperty("reservation.default-months-for-availability-request", Integer.class);
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now, now.plusMonths(defaultMonthsForAvailabilityRequest));
		Mono<AvailabilityVO> value = Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
			{
				add(dateRange);
			}
		}, dateRange));
		when(reservationService.findAvailability(eq(new DateRangeVO(null, null)))).thenReturn(value);

		//
		// When / Then
		//
		webClient.get().uri("/reservations").exchange().expectStatus().isOk().expectBody(AvailabilityVO.class)
				.isEqualTo(value.block());
		verify(reservationService).findAvailability(new DateRangeVO(now, null));
	}

	@SuppressWarnings("serial")
	@Test
	public void testFindAvailability_dateRangeSet() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		LocalDate from = now.plusDays(15);
		LocalDate to = now.plusDays(15).plusMonths(1);
		DateRangeVO dateRange = new DateRangeVO(from, to);
		DateRangeVO actualAvailability = new DateRangeVO(now.plusDays(15).plusDays(5),
				now.plusDays(15).plusMonths(1).minusDays(5));
		Mono<AvailabilityVO> value = Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
			{
				add(actualAvailability);
			}
		}, dateRange));
		when(reservationService.findAvailability(eq(dateRange))).thenReturn(value);

		//
		// When / Then
		//
		webClient.get().uri(String.format("/reservations?from=%s&to=%s", from, to)).exchange().expectStatus().isOk()
				.expectBody(AvailabilityVO.class).isEqualTo(value.block());
		verify(reservationService).findAvailability(dateRange);
	}

	@SuppressWarnings("serial")
	@Test
	public void testFindAvailability_dateRangeIsOpen() {

		//
		// Given
		//
		Integer defaultMonthsForAvailabilityRequest = env
				.getProperty("reservation.default-months-for-availability-request", Integer.class);
		LocalDate now = LocalDate.now();
		LocalDate from = now.plusDays(10);
		DateRangeVO dateRange = new DateRangeVO(from, from.plusMonths(defaultMonthsForAvailabilityRequest));
		DateRangeVO actualAvailability = new DateRangeVO(now.plusDays(5), now.plusDays(15));
		DateRangeVO actualDateRange = new DateRangeVO(from, null);
		Mono<AvailabilityVO> value = Mono.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>() {
			{
				add(actualAvailability);
			}
		}, dateRange));
		when(reservationService.findAvailability(eq(actualDateRange))).thenReturn(value);

		//
		// When / Then
		//
		webClient.get().uri(String.format("/reservations?from=%s", from)).exchange().expectStatus().isOk()
				.expectBody(AvailabilityVO.class).isEqualTo(value.block());
		verify(reservationService).findAvailability(actualDateRange);
	}

	@Test
	public void testFindAvailability_noAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		LocalDate from = now.plusDays(15);
		LocalDate to = now.plusDays(15).plusMonths(1);
		DateRangeVO dateRange = new DateRangeVO(from, to);
		Mono<AvailabilityVO> value = Mono
				.<AvailabilityVO>just(new AvailabilityVO(new TreeSet<DateRangeVO>(), dateRange));
		when(reservationService.findAvailability(eq(dateRange))).thenReturn(value);

		//
		// When / Then
		//
		webClient.get().uri(String.format("/reservations?from=%s&to=%s", from, to)).exchange().expectStatus().isOk()
				.expectBody(AvailabilityVO.class).isEqualTo(value.block());
		verify(reservationService).findAvailability(dateRange);
	}

	@Test
	public void testMakeReservation_ok() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(10), now.plusDays(12));
		String bookingId = "someBookingId";
		String email = "email";
		String fullName = "fullName";
		Booking booking = new Booking(email, fullName, dateRange);
		Booking booked = new Booking(bookingId, email, fullName, dateRange);
		when(reservationService.makeReservation(eq(booking))).thenReturn(Mono.<Booking>just(booked));

		//
		// When / Then
		//
		webClient.post().uri("/reservations").contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(booking)).exchange().expectStatus().isCreated().expectHeader()
				.valueMatches("Location", String.format("/reservations/%s", booked.getId()));
		verify(reservationService).makeReservation(booking);
	}

	@Test
	public void testMakeReservation_noAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(10), now.plusDays(12));
		Booking booking = new Booking("email", "fullName", dateRange);
		when(reservationService.makeReservation(eq(booking)))
				.thenThrow(new IllegalArgumentException("No availability"));

		//
		// When / Then
		//
		webClient.post().uri("/reservations").contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(booking)).exchange().expectStatus().is4xxClientError();
		verify(reservationService).makeReservation(booking);
	}

	@Test
	public void testGetReservationInfo_ok() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO dateRange = new DateRangeVO(now.plusDays(10), now.plusDays(12));
		String bookingId = "someBookingId";
		Booking booking = new Booking(bookingId, "email", "fullName", dateRange);
		when(reservationService.getReservationInfo(eq(bookingId))).thenReturn(Mono.<Booking>just(booking));

		//
		// When / Then
		//
		webClient.get().uri(String.format("/reservations/%s", bookingId)).exchange().expectStatus().isOk()
				.expectBody(Booking.class).isEqualTo(booking);
		verify(reservationService).getReservationInfo(bookingId);
	}

	@Test
	public void testGetReservationInfo_notFound() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		when(reservationService.getReservationInfo(eq(bookingId))).thenThrow(new BookingNotFoundException(bookingId));

		//
		// When / Then
		//
		webClient.get().uri(String.format("/reservations/%s", bookingId)).exchange().expectStatus().isNotFound();
		verify(reservationService).getReservationInfo(bookingId);
	}

	@Test
	public void testCancelReservation_ok() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		when(reservationService.cancelReservation(eq(bookingId))).thenReturn(Mono.<Boolean>empty());

		//
		// When / Then
		//
		webClient.delete().uri(String.format("/reservations/%s", bookingId)).exchange().expectStatus().isOk();
		verify(reservationService).cancelReservation(bookingId);
	}

	@Test
	public void testCancelReservation_notFound() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		when(reservationService.cancelReservation(eq(bookingId))).thenThrow(new BookingNotFoundException(bookingId));

		//
		// When / Then
		//
		webClient.delete().uri(String.format("/reservations/%s", bookingId)).exchange().expectStatus().isNotFound();
		verify(reservationService).cancelReservation(bookingId);
	}

	@Test
	public void testModifyReservation_ok() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10), now.plusDays(12));
		String bookingId = "someBookingId";
		Booking updatedBooking = new Booking(bookingId, "email", "fullName", newDateRange);
		when(reservationService.modifyReservation(eq(bookingId), eq(newDateRange)))
				.thenReturn(Mono.<Booking>just(updatedBooking));

		//
		// When / Then
		//
		webClient.put().uri(String.format("/reservations/%s", bookingId)).body(BodyInserters.fromValue(newDateRange))
				.exchange().expectStatus().isOk();
		verify(reservationService).modifyReservation(bookingId, newDateRange);
	}

	@Test
	public void testModifyReservation_noAvailability() {

		//
		// Given
		//
		LocalDate now = LocalDate.now();
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10), now.plusDays(12));
		String bookingId = "someBookingId";
		when(reservationService.modifyReservation(eq(bookingId), eq(newDateRange)))
				.thenThrow(new IllegalArgumentException("No availability"));

		//
		// When / Then
		//
		webClient.put().uri(String.format("/reservations/%s", bookingId)).body(BodyInserters.fromValue(newDateRange))
				.exchange().expectStatus().is4xxClientError();
		verify(reservationService).modifyReservation(bookingId, newDateRange);
	}

	@Test
	public void testModifyReservation_notFound() {

		//
		// Given
		//
		String bookingId = "someBookingId";
		LocalDate now = LocalDate.now();
		DateRangeVO newDateRange = new DateRangeVO(now.plusDays(10), now.plusDays(12));
		when(reservationService.modifyReservation(eq(bookingId), eq(newDateRange)))
				.thenThrow(new BookingNotFoundException(bookingId));

		//
		// When / Then
		//
		webClient.put().uri(String.format("/reservations/%s", bookingId)).body(BodyInserters.fromValue(newDateRange))
				.exchange().expectStatus().isNotFound();
		verify(reservationService).modifyReservation(bookingId, newDateRange);
	}
}
