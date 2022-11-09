package com.kaleidoscope.tripserver.repositories;

import com.kaleidoscope.tripserver.pojos.AppUser;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<AppUser, Long> {


    @Query(value = "SELECT * FROM app_user WHERE u_id = ?1", nativeQuery = true)
    Optional<AppUser> findByUid(String uId);
}
