package com.kaleidoscope.tripserver;

import com.google.firebase.auth.FirebaseAuthException;
import com.kaleidoscope.tripserver.networkutils.FirebaseConnector;
import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.AppUser;
import com.kaleidoscope.tripserver.pojos.Trip;
import com.kaleidoscope.tripserver.presenters.MainPresenter;
import com.kaleidoscope.tripserver.repositories.PlaceRepository;
import com.kaleidoscope.tripserver.repositories.TripRepository;
import com.kaleidoscope.tripserver.repositories.UserRepository;
import com.kaleidoscope.tripserver.utils.HashGen;
import com.kaleidoscope.tripserver.utils.JsonBuilder;
import lombok.NonNull;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Controller
//@RequestMapping("/data")
public class MainController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private TripRepository tripRepository;

    // TODO: Adjust media storage
    private static final String UPLOAD_DIR = "/Users/user/IdeaProjects/tripserver/uploads/";
    private Path path;

    private int storiesBeforeCleaningCounter = 0;

    @RequestMapping("/")
    public String index() {
        return "/docs/MainController.html";
    }

    // The client must provide Firebase credentials
    @PostMapping("/login")
    public @ResponseBody ResponseEntity<String> login(@RequestParam("username") String username,
                                                      @RequestParam("uId") String uId) {

        // Request to Firebase once in 5 times
        // If the user exist, generate API key for him and send "OK status"

        AppUser appUser = null;

        if (userRepository.count() > 0)
            if (userRepository.findByUid(uId).isPresent())
                appUser = userRepository.findByUid((uId)).get();


        String apiKey = null;

        if (appUser != null) {
            if ((appUser.getEmail() != null && appUser.getEmail().equals(username)) ||
                    (appUser.getPhone() != null && appUser.getPhone().equals(username))) {
                if (appUser.getCheckCount() < 5) {
                    apiKey = HashGen.getInstance().generate(uId);
                    appUser.setSent(false);
                    appUser.setApiKey(apiKey);
                    appUser.setCheckCount(appUser.getCheckCount() + 1);
                    userRepository.save(appUser);
                    return ResponseEntity.status(HttpStatus.OK).body("Locally confirmed");
                }
            }
        } else {
            appUser = new AppUser();
        }
        try {
            if (FirebaseConnector.getInstance().checkUser(username, uId)) {

                if (FirebaseConnector.getInstance().getEmail(uId) != null) {
                    appUser.setEmail(FirebaseConnector.getInstance().getEmail(uId));
                }
                if (FirebaseConnector.getInstance().getPhone(uId) != null) {
                    appUser.setPhone(FirebaseConnector.getInstance().getPhone(uId));
                }

                apiKey = HashGen.getInstance().generate(appUser.getUId());
                appUser.setSent(false);
                appUser.setUId(uId);
                appUser.setApiKey(apiKey);
                // set initial name if Firebase provides it:
                if (appUser.getFName().length() < 1) {
                    String firstLastName = FirebaseConnector.getInstance().getName(uId);
                    String names[] = firstLastName.split(" ");
                    if (names.length > 1) {
                        appUser.setFName(names[0]);
                        appUser.setLName(names[1]);
                    } else {
                        appUser.setFName(firstLastName);
                    }
                }

                if (appUser.getEmail().length() < 1) {
                    if (!FirebaseConnector.getInstance().getEmail(uId).isEmpty()) {
                        appUser.setEmail(FirebaseConnector.getInstance().getEmail(uId));
                    }
                }

                if (appUser.getPhone().length() < 1) {
                    if (!FirebaseConnector.getInstance().getPhone(uId).isEmpty()) {
                        appUser.setPhone(FirebaseConnector.getInstance().getPhone(uId));
                    }
                }


                appUser.setCheckCount(0);
                userRepository.save(appUser);
                return ResponseEntity.status(HttpStatus.OK).body("Confirmed");
            }
        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }

        return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
    }

    // After the user provided a Firebase credentials (login()), he ask for a api_key here.
    @GetMapping("/key/{uId}")
    @ResponseBody
    public ResponseEntity<String> getKey(@PathVariable String uId) {
        AppUser appUser = null;
        if (userRepository.count() > 0)
            if (userRepository.findByUid(uId).isPresent())
                appUser = userRepository.findByUid(uId).get();
        if (appUser != null) {
            if (!appUser.isSent()) {
                Map<String, String> map = new HashMap<>();
                map.put("key", appUser.getApiKey());
                map.put("id", Long.toString(appUser.getId()));

                appUser.setSent(true);
                appUser.setRequestCount(0);
                userRepository.save(appUser);

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
    public @ResponseBody ResponseEntity<Objects> addUserInfo(@RequestHeader("api_key") String api_key,
                                                             @RequestBody AppUser appUser) {
        if (appUser != null)
            if (authorize(api_key, appUser.getId())) {
                if (userRepository.findById(appUser.getId()).isPresent()) {
                    AppUser tempAppUser = userRepository.findById(appUser.getId()).get();
                    tempAppUser.setListOfTags(appUser.getListOfTags());
                    tempAppUser.setFName(appUser.getFName());
                    tempAppUser.setNName(appUser.getNName());
                    tempAppUser.setLName(appUser.getLName());
                    tempAppUser.setLocation(appUser.getLocation());
                    tempAppUser.setInfo(appUser.getInfo());
                    tempAppUser.setPlaces(appUser.getPlaces());
                    tempAppUser.setTrips(appUser.getTrips());
                    tempAppUser.setFriends(appUser.getFriends());
                    userRepository.save(tempAppUser);
                    return new ResponseEntity<Objects>(HttpStatus.OK);
                }
                return new ResponseEntity<Objects>(HttpStatus.NO_CONTENT);
            }
        return new ResponseEntity<Objects>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/user/{id}")
    public @ResponseBody Optional<AppUser> getUserInfo(@RequestHeader("api_key") String api_key,
                                                       @PathVariable Long id) {
        if (authorize(api_key, id)) {
            AppUser appUser = null;
            if (userRepository.findById(id).isPresent())
                appUser = userRepository.findById(id).get();
            appUser.setUId("");
            appUser.setApiKey("");
            return Optional.of(appUser);
        }
        return null;
    }

    @PostMapping("/addImage")
    public @ResponseBody ResponseEntity<Objects> addImage(@RequestHeader("api_key") String api_key,
                                                          @RequestHeader("id") Long id,
                                                          @RequestParam("file") MultipartFile file) {
        if (authorize(api_key, id)) {
            if (putImage(file)) {
                return new ResponseEntity<Objects>(HttpStatus.OK);
            } else {
                return new ResponseEntity<Objects>(HttpStatus.EXPECTATION_FAILED);
            }
        }
        return new ResponseEntity<Objects>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/image/{name}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@RequestHeader("api_key") String api_key,
                                           @RequestHeader("id") Long id,
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

    @PostMapping("/add_place")
    public @ResponseBody ResponseEntity<Objects> addNewPlace(@RequestHeader("api_key") String api_key,
                                                             @RequestHeader("id") Long id,
                                                             @RequestBody Place place) {
        if (authorize(api_key, id)) {
            if (place.getDescription().length() < 4000) {
                placeRepository.save(place);
                return new ResponseEntity<Objects>(HttpStatus.OK);
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).body("The description over-length.");
            }
        }
        return new ResponseEntity<Objects>(HttpStatus.FORBIDDEN);
    }

    // No secure yet. Mb make it open for world?
    @GetMapping("/place/{id}")
    public @ResponseBody Optional<Place> getPlace(@PathVariable Long id) {
        return placeRepository.findById(id);
    }

    // No secure yet. Mb make it open for world?
    @GetMapping("/trip/{id}")
    public @ResponseBody Optional<Trip> getTrip(@PathVariable Long id) {
        return tripRepository.findById(id);
    }

    @PostMapping("/add_trip")
    public @ResponseBody ResponseEntity<Objects> addNewTrip(@RequestHeader("api_key") String api_key,
                                                            @RequestHeader("id") Long id,
                                                            @RequestBody Trip trip) {
        if (authorize(api_key, id)) {
            if (trip.getDescription().length() < 7000) {
                tripRepository.save(trip);
                return new ResponseEntity<Objects>(HttpStatus.OK);
            } else {
                ResponseEntity.status(HttpStatus.FORBIDDEN).body("The description over-length.");
            }
            return new ResponseEntity<Objects>(HttpStatus.OK);
        }
        return new ResponseEntity<Objects>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/main/{id}")
    public @ResponseBody MainPresenter getMainContent(@RequestHeader("api_key") String api_key,
                                                      @PathVariable Long id) {
        if (authorize(api_key, id)) {
            MainPresenter presenter = new MainPresenter();
            AppUser appUser = null;
            if (userRepository.findById(id).isPresent()) {
                appUser = userRepository.findById(id).get();

                presenter.setHeadImgUrl("TODO: current place image");

                // Place of current User location
                presenter.setLocationName(appUser.getLocation()); // TODO: convert coordinates to name of place

                // List of advisable places for this user based on tags
                presenter.setAdvicePlacesJson(JsonBuilder.getInstance()
                        .placesByTags((List) placeRepository.findAll(),
                                appUser.getListOfTags()).toString());

                // List of advisable Trips for this user based on rating (likes) and tags
                presenter.setTopTripsJson(JsonBuilder.getInstance()
                        .objectsByRatingAndTags((List) tripRepository.findAll(),
                                appUser.getListOfTags()).toString());

                // List of advisable Places for this user based on rating (likes) and tags
                presenter.setTopPlacesJson(JsonBuilder.getInstance()
                        .objectsByRatingAndTags((List) placeRepository.findAll(),
                                appUser.getListOfTags()).toString());

                presenter.setStoriesJson(getStories(id));

            }

            return presenter;
        }
        return null;
    }

    @PostMapping("/putStory")
    public @ResponseBody ResponseEntity<String> putStory(@RequestHeader("api_key") String api_key,
                                                         @RequestHeader("id") Long id,
                                                         @RequestParam("file") MultipartFile file) {
        if (authorize(api_key, id)) {
            AppUser appUser = null;

            storiesBeforeCleaningCounter++;

            if (storiesBeforeCleaningCounter == 33) {
                storiesBeforeCleaningCounter = 0;
                clearStories();
            }

            if (userRepository.findById(id).isPresent()) {
                appUser = userRepository.findById(id).get();
            }

            if (appUser != null) {
                // appUser.setStories(appUser.getStories() + 1);
                appUser.addStoriesTimeStamp(System.currentTimeMillis() / 1000L);
                userRepository.save(appUser);
                if (putImage(file)) {
                    return new ResponseEntity<String>(HttpStatus.OK);
                }
            }
            return new ResponseEntity<String>(HttpStatus.EXPECTATION_FAILED);
        }
        return new ResponseEntity<String>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/subscribe/{userId}")
    public ResponseEntity<String> subscribe(@RequestHeader("api_key") String api_key,
                                            @RequestHeader("id") Long id,
                                            @PathVariable("userId") Long userId) {

        if (authorize(api_key, id)) {
            if (userRepository.findById(id).isPresent()) {
                AppUser user = userRepository.findById(id).get();
                user.addFriend(userId);
                userRepository.save(user);
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

    private void clearStories() {
        // Clear time stamps containers for users IF publishing time exceed 24h and delete image-files
        Iterable<AppUser> usersList = userRepository.findAll();
        Long now = System.currentTimeMillis() / 1000;

        for (AppUser user : usersList) {
            for (int i = 0; i < user.getStoriesTimeStamps().size(); ++i) {
                if ((now - user.getStoriesTimeStamps().get(i)) >= (TimeUnit.HOURS.toMillis(24) / 1000)) {
                    try {
                        File file = new File(UPLOAD_DIR + "story_"
                                + Long.toString(user.getId())
                                + "_" + Long.toString(user.getStoriesTimeStamps().get(i)));
                        file.delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    user.getStoriesTimeStamps().remove(i);
                }
            }
        }


    }

    private boolean putImage(@NonNull MultipartFile file) {
        // TODO: Adjust media storage on a deploy stage
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
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getStories(Long id) {
        AppUser appUser = null;
        if (userRepository.findById(id).isPresent()) {
            appUser = userRepository.findById(id).get();
        }

        // Friends of this user
        List<Long> friends = null;
        if (appUser != null)
            friends = appUser.getFriends();

        // Compose Json
        JSONObject jsonObject = new JSONObject();
        Map<String, String> jsonMap = new HashMap<>();
        AppUser friendUser = null;

        if (friends != null) {
            for (Long userId : friends) {
                if (userRepository.findById(userId).isPresent() &&
                        userRepository.findById(userId).get().getStoriesTimeStamps().size() > 0) {
                    friendUser = userRepository.findById(userId).get();
                    jsonMap.put("id", Long.toString(userId));
                    jsonMap.put("name", friendUser.getNName());
                    for (int i = 0; i < friendUser.getStoriesTimeStamps().size(); ++i) {
                        jsonMap.put(Integer.toString(i) + "_stamp", Long.toString(friendUser
                                .getStoriesTimeStamps().get(i)));
                    }
                    jsonObject.put("stories", jsonMap);
                    jsonMap.clear();
                }
            }
        }
        return jsonObject.toString();
    }

    private boolean authorize(String api_key, long id) {
        AppUser appUser = null;
        if (userRepository.findById(id).isPresent())
            appUser = userRepository.findById(id).get();
        if (appUser != null) {
            if (appUser.isSent())
                if (appUser.getRequestCount() < 15)
                    if (api_key.equals(appUser.getApiKey())) {
                        appUser.setRequestCount(appUser.getRequestCount() + 1);
                        userRepository.save(appUser);
                        return true;
                    }
        }
        return false;
    }

}
























