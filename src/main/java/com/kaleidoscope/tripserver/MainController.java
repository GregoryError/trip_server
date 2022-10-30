package com.kaleidoscope.tripserver;


import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.User;
import com.kaleidoscope.tripserver.presenters.MainPresenter;
import com.kaleidoscope.tripserver.repositories.PlaceRepository;
import com.kaleidoscope.tripserver.repositories.UserRepository;
import com.kaleidoscope.tripserver.utils.JsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

// TODO: connection to Firebase (check users)

@RestController
@RequestMapping("/data")
public class MainController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;

    private static final String UPLOAD_DIR = "/uploads/";
    private Path path;

    @PostMapping("/add_user")
    public @ResponseBody ResponseEntity<Objects> addNewUser(/*@RequestBody User user,*/ @RequestParam("file") MultipartFile file) {


        if (!file.isEmpty()) {
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            // save an avatar-image on a server disk
            try {
                path = Paths.get(UPLOAD_DIR + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // userRepository.save(user); <-- !
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

    @GetMapping("/main/{id}")
    public @ResponseBody MainPresenter getMainContent(@PathVariable Long id) {
        MainPresenter presenter = new MainPresenter();
        User user;
        if (userRepository.findById(id).isPresent()) {
            user = userRepository.findById(id).get();
            presenter.setLocationName(user.getLocation()); // TODO: convert coordinates to name of place

            // List of advisable places for this user based on tags TODO: ond rating (likes)
            presenter.setAdvicePlacesJson(JsonBuilder
                    .objectsByTags((List) placeRepository.findAll(),
                            user.getListOfTags()).toString());
            // List of advisable Trips for this user based on tags TODO: ond rating (likes)



        }

        return presenter;
    }

}






















