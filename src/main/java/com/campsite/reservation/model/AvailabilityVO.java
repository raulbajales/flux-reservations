package com.campsite.reservation.model;

import java.time.LocalDate;
import java.util.TreeSet;

import org.springframework.util.Assert;

public class AvailabilityVO {
	private DateRangeVO inThisDateRange;
	private TreeSet<DateRangeVO> datesAvailable;

	public AvailabilityVO() {

	}

	public AvailabilityVO(TreeSet<DateRangeVO> datesAvailable, DateRangeVO inThisDateRange) {
		this.inThisDateRange = inThisDateRange;
		this.datesAvailable = datesAvailable;
	}

	public DateRangeVO getInThisDateRange() {
		return inThisDateRange;
	}

	public TreeSet<DateRangeVO> getDatesAvailable() {
		return datesAvailable;
	}

	public static AvailabilityVO.Builder builder(DateRangeVO dateRange) {
		return new AvailabilityVO.Builder(dateRange);
	}

	public static class Builder {

		private AvailabilityVO availability;

		public Builder(DateRangeVO dateRange) {
			this.availability = new AvailabilityVO();
			this.availability.datesAvailable = new TreeSet<DateRangeVO>();
			this.availability.inThisDateRange = dateRange;
		}

		public void addRange(LocalDate from, LocalDate to) {
			addRange(new DateRangeVO(from, to));
		}

		public Builder addRange(DateRangeVO dateRange) {
			Assert.isTrue(dateRange.isInsideRange(this.availability.inThisDateRange),
					String.format("Cannot add date range %s because it's out of availability range %s", dateRange,
							this.availability.inThisDateRange));
			if (this.availability.datesAvailable.isEmpty()) {
				this.availability.datesAvailable.add(dateRange);
			} else {
				Assert.isTrue(!this.availability.datesAvailable.last().isOpen(),
						String.format("Cannot add date range %s into %s because last is open", dateRange,
								this.availability.getDatesAvailable()));
				this.availability.datesAvailable.add(dateRange);
			}
			return this;
		}

		public AvailabilityVO build() {
			return this.availability;
		}
	}
}
