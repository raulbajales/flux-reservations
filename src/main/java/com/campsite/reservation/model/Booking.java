package com.campsite.reservation.model;

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

	public Booking(String email, String fullname, DateRangeVO dateRange) {
		Assert.notNull(email, "email needs to be set");
		Assert.notNull(fullname, "fullname needs to be set");
		Assert.notNull(dateRange, "dateRange needs to be set");
		Assert.isTrue(!dateRange.isOpen(), "Cannot set open date range for a booking.");
		this.email = email;
		this.fullname = fullname;
		this.dateRange = dateRange;
	}

	public Booking(String id, String email, String fullname, DateRangeVO dateRange) {
		this(email, fullname, dateRange);
		Assert.notNull(id, "id needs to be set");
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

	@Override
	public String toString() {
		return String.format("[id: %s, email: %s, fullName: %s, dateRange: %s]", this.id, this.email, this.fullname,
				this.dateRange);
	}

	@Override
	public boolean equals(Object obj) {
		Booking other = (Booking) obj;
		return (this.id != null ? this.id.equals(other.getId()) : other.getId() == null)
				&& this.email.equals(other.getEmail()) && this.fullname.equals(other.getFullname())
				&& this.dateRange.equals(other.getDateRange());
	}
}
