package com.kaleidoscope.tripserver.repositories;

import com.kaleidoscope.tripserver.pojos.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {


    @Query(value = "SELECT * FROM user WHERE u_id = ?1", nativeQuery = true)
    User findByUid(String uId);
}
