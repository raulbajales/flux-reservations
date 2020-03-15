package com.campsite.reservation.repository.impl;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.util.Assert;

import com.campsite.reservation.exception.BookingNotFoundException;
import com.campsite.reservation.model.Booking;
import com.campsite.reservation.model.DateRangeVO;
import com.campsite.reservation.repository.BookingRepositoryCustom;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BookingRepositoryCustomImpl implements BookingRepositoryCustom {

	@Autowired
	private ReactiveMongoTemplate mongo;

	@Override
	public Flux<Booking> findByDateRange(DateRangeVO dateRange) {
		Assert.notNull(dateRange, "The given dateRange must not be null!");
		Assert.isTrue(!dateRange.isOpen(), "The given dateRange cannot be open!");
		return mongo.find(query(where("dateRange.to").lte(dateRange.getTo())
				.orOperator(where("dateRange.from").gte(dateRange.getFrom())))
						.with(Sort.by(Sort.Direction.DESC, "dateRange.from")),
				Booking.class);
	}

	@Override
	public Flux<Booking> findByDateRangeExcluding(DateRangeVO dateRange, String bookingId) {
		Assert.notNull(bookingId, "The given id must not be null!");
		Assert.notNull(dateRange, "The given dateRange must not be null!");
		Assert.isTrue(!dateRange.isOpen(), "The given dateRange cannot be open!");
		return mongo.find(query(where("dateRange.to").lte(dateRange.getTo())
				.orOperator(where("dateRange.from").gte(dateRange.getFrom()))).addCriteria(where("id").ne(bookingId))
						.with(Sort.by(Sort.Direction.DESC, "dateRange.from")),
				Booking.class);
	}

	@Override
	public Mono<Void> deleteById(String bookingId) {
		Assert.notNull(bookingId, "The given id must not be null!");
		return mongo.findOne(query(where("id").is(bookingId)), Booking.class)
				.switchIfEmpty(Mono.error(new BookingNotFoundException(bookingId)))
				.flatMap(booking -> mongo.remove(booking)).then();
	}

	@Override
	public Mono<Booking> findById(String bookingId) {
		Assert.notNull(bookingId, "The given id must not be null!");
		return mongo.findOne(query(where("id").is(bookingId)), Booking.class)
				.switchIfEmpty(Mono.error(new BookingNotFoundException(bookingId)));
	}
}
