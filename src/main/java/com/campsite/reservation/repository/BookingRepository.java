package com.campsite.reservation.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.campsite.reservation.model.Booking;

@Repository
public interface BookingRepository extends ReactiveMongoRepository<Booking, String>, BookingRepositoryCustom {
	
}