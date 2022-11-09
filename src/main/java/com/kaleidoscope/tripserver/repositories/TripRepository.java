package com.kaleidoscope.tripserver.repositories;

import com.kaleidoscope.tripserver.pojos.Trip;
import org.springframework.data.repository.CrudRepository;

public interface TripRepository extends CrudRepository<Trip, Long> {
}
