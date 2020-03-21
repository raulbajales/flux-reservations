package com.campsite.reservation.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Defines a date range by setting a 'from' and a 'to' dates.")
public class DateRangeVO implements Comparable<DateRangeVO> {

	@ApiModelProperty(notes = "From date, in 'yyyy-MM-dd' format.")
	private LocalDate from;

	@ApiModelProperty(notes = "To date, in 'yyyy-MM-dd' format.")
    private LocalDate to;

	public DateRangeVO() {
		this(null, null);
	}

	/**
	 * @param from LocalDate
	 * @param to   LocalDate
	 * 
	 *             Rules: 'from' and 'to' cannot be both null, if 'from' is null
	 *             then it's set to today, 'to' can be null (in this case means the
	 *             date range is open)
	 */
	public DateRangeVO(LocalDate from, LocalDate to) {
		if (from != null && to != null) {
			Assert.isTrue(to.isAfter(from), String.format("Invalid date range, from: %s, to: %s", from, to));
		}
		this.from = from != null ? from : LocalDate.now();
		this.to = to;
	}

	public LocalDate getFrom() {
		return from;
	}

	public LocalDate getTo() {
		return to;
	}

	@JsonIgnore
	public Boolean isOpen() {
		return this.to == null;
	}

	public long totalDays() {
		return ChronoUnit.DAYS.between(this.from, this.to);
	}

	/**
	 * Checks if this DateRange instance is inside DateRange argument,
	 * inThisDateRange
	 */
	public boolean isInsideRange(DateRangeVO inThisDateRange) {
		return (this.from.isEqual(inThisDateRange.from) || this.from.isAfter(inThisDateRange.from))
				&& (inThisDateRange.isOpen()
						|| (this.to.isEqual(inThisDateRange.to) || this.to.isBefore(inThisDateRange.to)));
	}

	public Pair<Optional<DateRangeVO>, Optional<DateRangeVO>> minus(DateRangeVO other) {
		Optional<DateRangeVO> l = Optional.empty();
		Optional<DateRangeVO> r = Optional.empty();
		if (this.to.isBefore(other.from) || (this.from.isAfter(other.to))) {
			r = Optional.of(new DateRangeVO(this.from, this.to));
		} else if (this.from.isBefore(other.from) && (this.to.isAfter(other.to))) {
			l = Optional.of(new DateRangeVO(this.from, other.from));
			r = Optional.of(new DateRangeVO(other.to, this.to));
		} else if (this.from.isAfter(other.from) && (this.to.isAfter(other.to))) {
			r = Optional.of(new DateRangeVO(other.to, this.to));
		} else if (this.from.isBefore(other.from) && (this.to.isBefore(other.to))) {
			l = Optional.of(new DateRangeVO(this.from, other.from));
		}
		return Pair.of(l, r);
	}

	@Override
	public String toString() {
		return String.format("[from: %s, to: %s]", this.from, this.to);
	}

	@Override
	public boolean equals(Object obj) {
		DateRangeVO vo = (DateRangeVO) obj;
		Boolean cond1 = ((this.getFrom() != null && vo.getFrom() != null && this.getFrom().isEqual(vo.getFrom()))
				|| (this.getFrom() == null && vo.getFrom() == null));
		Boolean cond2 = ((this.getTo() != null && vo.getTo() != null && this.getTo().isEqual(vo.getTo()))
				|| (this.getTo() == null && vo.getTo() == null));
		return cond1 && cond2;
	}

	@Override
	public int hashCode() {
		return this.from.hashCode() * this.to.hashCode();
	}

	@Override
	public int compareTo(DateRangeVO o) {
		if (this.from.equals(o.from)) {
			if ((this.to == null && o.to == null) || (this.to.equals(o.to)))
				return 0;
			if (this.to == null && o.to != null)
				return 1;
			if (this.to != null && o.to == null)
				return -1;
		}
		return this.from.compareTo(o.from);
	}
}
