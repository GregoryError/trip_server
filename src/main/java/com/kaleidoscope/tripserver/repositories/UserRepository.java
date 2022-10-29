package com.kaleidoscope.tripserver.repositories;

import com.kaleidoscope.tripserver.pojos.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}
