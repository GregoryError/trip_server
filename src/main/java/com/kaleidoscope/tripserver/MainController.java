package com.kaleidoscope.tripserver;

import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.User;
import com.kaleidoscope.tripserver.presenters.MainPresenter;
import com.kaleidoscope.tripserver.repositories.PlaceRepository;
import com.kaleidoscope.tripserver.repositories.UserRepository;
import com.kaleidoscope.tripserver.utils.JsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

// TODO: connection to Firebase (check users)

@RestController
@RequestMapping("/data")
public class MainController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;

    private static final String SELF_NET_I_FACE_ADDRESS = "192.168.4.42";

    // TODO: Adjust media storage
    private static final String UPLOAD_DIR = "/Users/user/IdeaProjects/tripserver/uploads/";
    private Path path;

    @PostMapping("/add_user")
    public @ResponseBody ResponseEntity<Objects> addNewUser(@RequestBody User user) {
        userRepository.save(user);
        return new ResponseEntity<Objects>(HttpStatus.OK);
    }

    @PostMapping("/userImage")
    public @ResponseBody ResponseEntity<Objects> addUserImage(@RequestParam("file") MultipartFile file) {

        // TODO: Adjust media storage
        String originFileName = null;
        if (!file.isEmpty()) {
            if (file.getOriginalFilename() != null) {
                originFileName = file.getOriginalFilename();
            }
        }

        String fileName = StringUtils.cleanPath(originFileName + ".jpeg");

        try {
            path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            return new ResponseEntity<Objects>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
        }


        return new ResponseEntity<Objects>(HttpStatus.EXPECTATION_FAILED);
    }


    @GetMapping("/files/{id}")
    @ResponseBody
    public ResponseEntity<File> serveFile(@PathVariable Long id) {

        File file = new File(UPLOAD_DIR + Long.toString(id));


        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getName() + "\"").body(file);

    }


//    @GetMapping("/userImg/{id}")
//    @ResponseBody
//    public ResponseEntity<Resource> getUserImg(@PathVariable Long id) {
//        HttpHeaders headers = new HttpHeaders();
//        Resource resource =
//                new ServletContextResource(servletContext, "/uploads/" + Long.toString(id) + ".jpeg");
//        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
//    }


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






















