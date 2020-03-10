package com.campsite.reservation.repository;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;

import reactor.core.publisher.Flux;

public class BookingRepositoryCustomImpl implements BookingRepositoryCustom {

	@Autowired
	private ReactiveMongoTemplate mongo;

	@Override
	public Flux<Booking> findByDateRange(DateRangeVO dateRange) {
		return mongo.find(query(where("dateRange.to").lte(dateRange.getTo())
				.orOperator(where("dateRange.from").gte(dateRange.getFrom())))
						.with(Sort.by(Sort.Direction.DESC, "dateRange.from")),
				Booking.class);
	}

	@Override
	public Flux<Booking> findByDateRangeExcluding(DateRangeVO dateRange, String bookingId) {
		return mongo.find(query(where("dateRange.to").lte(dateRange.getTo())
				.orOperator(where("dateRange.from").gte(dateRange.getFrom()))).addCriteria(where("id").ne(bookingId))
						.with(Sort.by(Sort.Direction.DESC, "dateRange.from")),
				Booking.class);
	}
}
