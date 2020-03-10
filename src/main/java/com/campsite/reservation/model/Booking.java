package com.campsite.reservation.model;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.Assert;

@Document
public class Booking {

	@Id
	private String id;

	private String email;
	private String fullname;
	private DateRangeVO dateRange;

	public Booking() {
	}

	public Booking(@NotNull String email, @NotNull String fullname, @NotNull DateRangeVO dateRange) {
		Assert.isTrue(!dateRange.isOpen(), "Cannot set open date range for a booking.");
		this.email = email;
		this.fullname = fullname;
		this.dateRange = dateRange;
	}

	public Booking(@NotNull String id, @NotNull String email, @NotNull String fullname, @NotNull DateRangeVO dateRange) {
		this(email, fullname, dateRange);
		this.id = id;
	}
	
	public static Booking from(Booking booking, DateRangeVO newDateRange) {
		Booking newBooking = new Booking(booking.email, booking.fullname, newDateRange);
		newBooking.id = booking.id;
		return newBooking;
	}

	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFullname() {
		return fullname;
	}

	public DateRangeVO getDateRange() {
		return dateRange;
	}
}
