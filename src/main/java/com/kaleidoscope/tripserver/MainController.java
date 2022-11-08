package com.kaleidoscope.tripserver;

import com.google.firebase.auth.FirebaseAuthException;
import com.kaleidoscope.tripserver.networkutils.FirebaseConnector;
import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.User;
import com.kaleidoscope.tripserver.presenters.MainPresenter;
import com.kaleidoscope.tripserver.repositories.PlaceRepository;
import com.kaleidoscope.tripserver.repositories.UserRepository;
import com.kaleidoscope.tripserver.utils.HashGen;
import com.kaleidoscope.tripserver.utils.JsonBuilder;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

// TODO: connection to Firebase (check users)
//
//

@Controller
//@RequestMapping("/data")
public class MainController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;

    // TODO: Adjust media storage
    private static final String UPLOAD_DIR = "/Users/user/IdeaProjects/tripserver/uploads/";
    private Path path;

    @RequestMapping("/")
    public String index() {
        return "/docs/MainController.html";
    }

//    @GetMapping("/docs")
//    String whitePage() {
//        return "MainController";
//        //return ResponseEntity.status(HttpStatus.FORBIDDEN).body("So, what are you looking for?");
//    }

    @PostMapping("/login")
    public @ResponseBody ResponseEntity<String> login(@RequestParam("username") String username,
                                                      @RequestParam("uId") String uId) {

        // TODO: Request to Firebase once in a 5 times
        // TODO: if the user exist, generate API key for him and send "OK status"

        User user = null;

        try {
            user = userRepository.findByUid((uId));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String apiKey = null;

        if (user != null) {
            if ((user.getEmail() != null && user.getEmail().equals(username)) ||
                    (user.getPhone() != null && user.getPhone().equals(username))) {
                if (user.getCheckCount() < 5) {
                    apiKey = HashGen.getInstance().generate(uId);
                    user.setSent(false);
                    user.setApiKey(apiKey);
                    user.setCheckCount(user.getCheckCount() + 1);
                    userRepository.save(user);
                    return ResponseEntity.status(HttpStatus.OK).body("Locally confirmed");
                }
            }
        } else {
            user = new User();
        }
        try {
            if (FirebaseConnector.getInstance().checkUser(username, uId)) {
                System.out.println("Firebase checking");

                if (FirebaseConnector.getInstance().getEmail(uId) != null) {
                    user.setEmail(FirebaseConnector.getInstance().getEmail(uId));
                }
                if (FirebaseConnector.getInstance().getPhone(uId) != null) {
                    user.setPhone(FirebaseConnector.getInstance().getPhone(uId));
                }
                apiKey = HashGen.getInstance().generate(user.getUId());
                user.setSent(false);
                user.setUId(uId);
                user.setApiKey(apiKey);
                user.setCheckCount(0);
                userRepository.save(user);
                return ResponseEntity.status(HttpStatus.OK).body("Confirmed");
            }
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

        return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/key/{uId}")
    @ResponseBody
    ResponseEntity<String> getKey(@PathVariable String uId) {
        User user = userRepository.findByUid(uId);
        if (user != null) {
            if (!user.isSent()) {
                Map<String, String> map = new HashMap<>();
                map.put("key", user.getApiKey());
                map.put("id", Long.toString(user.getId()));

                user.setSent(true);
                user.setRequestCount(0);
                userRepository.save(user);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user", map);
                return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());

                // return ResponseEntity.status(HttpStatus.OK).body(user.getApiKey());
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The key you are asking for is already in use.");
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid argument.");
    }

    @PostMapping("/add_user_info")
    public @ResponseBody ResponseEntity<Objects> addNewUser(@RequestHeader("api_key") String api_key,
                                                            @RequestBody User user) {
        if (user != null)
            if (authorize(api_key, user.getId())) {
                if (userRepository.findById(user.getId()).isPresent()) {
                    User tempUser = userRepository.findById(user.getId()).get();
                    tempUser.setListOfTags(user.getListOfTags());
                    tempUser.setFName(user.getFName());
                    tempUser.setNName(user.getNName());
                    tempUser.setLName(user.getLName());
                    tempUser.setLocation(user.getLocation());
                    tempUser.setInfo(user.getInfo());
                    tempUser.setPlaces(user.getPlaces());
                    tempUser.setStories(user.getStories());
                    tempUser.setTrips(user.getTrips());
                    tempUser.setFriends(user.getFriends());
                    userRepository.save(tempUser);
                    return new ResponseEntity<Objects>(HttpStatus.OK);
                }
                return new ResponseEntity<Objects>(HttpStatus.NO_CONTENT);
            }
        return new ResponseEntity<Objects>(HttpStatus.FORBIDDEN);
    }

    @PostMapping("/addImage")
    public @ResponseBody ResponseEntity<Objects> addImage(@RequestHeader("api_key") String api_key,
                                                          @PathVariable Long id,
                                                          @RequestParam("file") MultipartFile file) {
        // TODO: Adjust media storage
        if (authorize(api_key, id)) {
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
        return new ResponseEntity<Objects>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/image/{name}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@RequestHeader("api_key") String api_key,
                                           @PathVariable Long id,
                                           @PathVariable String name) {
        if (authorize(api_key, id)) {
            path = Paths.get(UPLOAD_DIR + name + ".jpeg");
            byte[] imageBytes = new byte[0];
            try {
                imageBytes = Files.readAllBytes(path);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/user/{id}")
    public @ResponseBody Optional<User> getUser(@RequestHeader("api_key") String api_key,
                                                @PathVariable Long id) {
        if (authorize(api_key, id)) {
            User user = userRepository.findById(id).get();
            user.setUId("");
            user.setApiKey("");
            return Optional.of(user);
        }
        return null;
    }

    @PostMapping("/add_place")
    public @ResponseBody ResponseEntity<Objects> addNewPlace(@RequestHeader("api_key") String api_key,
                                                             @PathVariable Long id,
                                                             @RequestBody Place place) {
        // TODO: implement check the length of 'description'
        if (authorize(api_key, id)) {
            placeRepository.save(place);
            return new ResponseEntity<Objects>(HttpStatus.OK);
        }
        return new ResponseEntity<Objects>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/place/{id}")
    public @ResponseBody Optional<Place> getPlace(@PathVariable Long id) {
        return placeRepository.findById(id);
    }

    @GetMapping("/main/{id}")
    public @ResponseBody MainPresenter getMainContent(@RequestHeader("api_key") String api_key,
                                                      @PathVariable Long id) {
        if (authorize(api_key, id)) {
            MainPresenter presenter = new MainPresenter();
            User user;
            if (userRepository.findById(id).isPresent()) {
                user = userRepository.findById(id).get();
                presenter.setLocationName(user.getLocation()); // TODO: convert coordinates to name of place

                // List of advisable places for this user based on tags TODO: add rating (likes)
                presenter.setAdvicePlacesJson(JsonBuilder.getInstance()
                        .objectsByTags((List) placeRepository.findAll(),
                                user.getListOfTags()).toString());
                // List of advisable Trips for this user based on tags TODO: add rating (likes)

            }

            return presenter;
        }
        return null;
    }

    private boolean authorize(String api_key, long id) {
        User user = null;
        if (userRepository.findById(id).isPresent())
            user = userRepository.findById(id).get();
        if (user != null) {
            if (user.isSent())
                if (user.getRequestCount() < 15)
                    if (api_key.equals(user.getApiKey())) {
                        user.setRequestCount(user.getRequestCount() + 1);
                        userRepository.save(user);
                        return true;
                    }
        }
        return false;
    }

}






















