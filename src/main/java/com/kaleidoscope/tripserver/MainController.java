package com.kaleidoscope.tripserver;

import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.User;
import com.kaleidoscope.tripserver.repositories.PlaceRepository;
import com.kaleidoscope.tripserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.Optional;

// TODO: connection to Firebase (check users)

@RestController
@RequestMapping("/data")
public class MainController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;

    @PostMapping("/add_user")
    public @ResponseBody ResponseEntity<Objects> addNewUser(@RequestBody User user) {
        System.out.println("USER: " +
                user.toString());

        userRepository.save(user);
        return new ResponseEntity<Objects>(HttpStatus.OK);
    }

    @GetMapping("/user/{id}")
    public @ResponseBody Optional<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id);
    }


    @PostMapping("/add_place")
    public @ResponseBody ResponseEntity<Objects> addNewPlace(@RequestBody Place place) {
        System.out.println("PLACE: " +
                place.toString());

        // TODO: implement check the length of 'description'

        placeRepository.save(place);
        return new ResponseEntity<Objects>(HttpStatus.OK);
    }

    @GetMapping("/place/{id}")
    public @ResponseBody Optional<Place> getPlace(@PathVariable Long id) {
        return placeRepository.findById(id);
    }

}






















