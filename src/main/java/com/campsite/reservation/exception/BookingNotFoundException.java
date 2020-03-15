package com.campsite.reservation.exception;

@SuppressWarnings("serial")
public class BookingNotFoundException extends RuntimeException {

	public BookingNotFoundException(String bookingId) {
		super(String.format("Booking id '%s' not found!", bookingId));
	}
}
