package com.kaleidoscope.tripserver.repositories;

import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.TripItem;
import org.springframework.data.repository.CrudRepository;

public interface PlaceRepository extends CrudRepository<Place, Long> {
}
