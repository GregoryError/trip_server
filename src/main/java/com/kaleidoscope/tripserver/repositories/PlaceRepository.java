package com.kaleidoscope.tripserver.repositories;

import com.kaleidoscope.tripserver.pojos.Place;
import org.springframework.data.repository.CrudRepository;

public interface PlaceRepository extends CrudRepository<Place, Long> {
}
