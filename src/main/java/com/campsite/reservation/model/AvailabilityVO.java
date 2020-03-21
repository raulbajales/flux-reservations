package com.campsite.reservation.model;

import java.time.LocalDate;
import java.util.TreeSet;

import org.springframework.util.Assert;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response for an availability request. Shows all the available date ranges for the requested date range.")
public class AvailabilityVO {
	
	@ApiModelProperty(notes = "Date range for the requested availability.")
	private DateRangeVO inThisDateRange;

	@ApiModelProperty(notes = "Set of date ranges available, sorted by 'from' date.")
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
		Assert.notNull(dateRange, "dateRange needs to be set");
		return new AvailabilityVO.Builder(dateRange);
	}

	@Override
	public String toString() {
		return String.format("[inThisDateRange: %s, datesAvailable: %s]", inThisDateRange, datesAvailable);
	}

	@Override
	public boolean equals(Object obj) {
		AvailabilityVO vo = (AvailabilityVO) obj;
		boolean cond1 = this.inThisDateRange.equals(vo.getInThisDateRange());
		boolean cond2 = (this.datesAvailable == null && vo.getDatesAvailable() == null)
				|| (this.datesAvailable.isEmpty() && vo.getDatesAvailable().isEmpty())
				|| (this.datesAvailable.containsAll(vo.getDatesAvailable())
						&& vo.getDatesAvailable().containsAll(this.datesAvailable));
		return cond1 && cond2;
	}

	public static class Builder {

		private AvailabilityVO availability;

		private Builder(DateRangeVO dateRange) {
			this.availability = new AvailabilityVO();
			this.availability.datesAvailable = new TreeSet<DateRangeVO>();
			this.availability.inThisDateRange = dateRange;
		}

		public void addRange(LocalDate from, LocalDate to) {
			Assert.notNull(from, "from needs to be set");
			addRange(new DateRangeVO(from, to));
		}

		public Builder addRange(DateRangeVO dateRange) {
			Assert.notNull(dateRange, "dateRange needs to be set");
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
