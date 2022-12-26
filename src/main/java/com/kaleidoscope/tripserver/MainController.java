package com.kaleidoscope.tripserver;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.gson.JsonObject;
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
import org.json.JSONArray;
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
//@RestController
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
        // If the user exists, generate API key for him and send "OK status"
        // The client must request the key once


        // Now check if user is valid and generate (if so) api_key and answer

        AppUser appUser = null;

        if (userRepository.count() > 0)
            if (userRepository.findByUid(uId).isPresent())
                appUser = userRepository.findByUid((uId)).get();

        String apiKey = null;

        if (appUser != null) {
            if ((appUser.getEmail() != null && appUser.getEmail().equals(username)) ||
                    (appUser.getPhone() != null && appUser.getPhone().equals(username))) {
                if (appUser.getCheckCount() < 5) {
                    apiKey = HashGen.getInstance().generate(uId);                       // Generate KEY
                    appUser.setSent(false);
                    appUser.setApiKey(apiKey);
                    appUser.setCheckCount(appUser.getCheckCount() + 1);
                    userRepository.save(appUser);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("response", "Locally_confirmed");
                    return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
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

                apiKey = HashGen.getInstance().generate(appUser.getUId());             // Generate KEY
                appUser.setSent(false);
                appUser.setUId(uId);
                appUser.setApiKey(apiKey);

                if (appUser.getEmail() == null) {
                    if (!FirebaseConnector.getInstance().getEmail(uId).isEmpty()) {
                        appUser.setEmail(FirebaseConnector.getInstance().getEmail(uId));
                    }
                }

                if (appUser.getPhone() == null) {
                    if (!FirebaseConnector.getInstance().getPhone(uId).isEmpty()) {
                        appUser.setPhone(FirebaseConnector.getInstance().getPhone(uId));
                    }
                }

                appUser.setCheckCount(0);
                userRepository.save(appUser);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("response", "confirmed");
                return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
            }
        } catch (FirebaseAuthException e) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("response", e.getMessage());
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(jsonObject.toString());
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("response", "user not found");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject.toString());
    }


    // After the user provided a Firebase credentials (login()), he ask for a api_key here.
    @GetMapping("/key/{uId}")
    @ResponseBody
    public ResponseEntity<String> getKey(@PathVariable String uId) {

        //   System.out.println("onGetKey: " + uId);

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
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The key you are asking for is already in use.");
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid argument.");
    }

    @PostMapping("/add_user_info")
    public @ResponseBody ResponseEntity<String> addUserInfo(@RequestHeader("api_key") String api_key,
                                                            @RequestBody AppUser appUser) {
        JSONObject jsonObject = new JSONObject();
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

                    jsonObject.put("response", "updated");
                    return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
                }
                jsonObject.put("response", "cant make answer");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(jsonObject.toString());
            }

        jsonObject.put("response", "bad auth");
        return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
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
    public @ResponseBody ResponseEntity<String> addImage(@RequestHeader("api_key") String api_key,
                                                         @RequestHeader("id") Long id,
                                                         @RequestParam("file") MultipartFile file) {

        System.out.println("TRYING UPLOAD IMAGE: " + file.getOriginalFilename());
        if (authorize(api_key, id)) {
            if (putImage(file)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("response", "uploaded");
                return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("response", "error while uploading");
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(jsonObject.toString());
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("response", "wrong auth");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject.toString());
    }

    @GetMapping("/image/{name}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@RequestHeader("api_key") String api_key,
                                           @RequestHeader("id") Long id,
                                           @PathVariable String name) {

        if (authorize(api_key, id)) {
            String dirPath = null;

            if (name.contains("place")) {
                dirPath = UPLOAD_DIR + "places/";
            } else if (name.contains("trip")) {
                dirPath = UPLOAD_DIR + "trips/";
            } else if (name.contains("user")) {
                dirPath = UPLOAD_DIR + "users/";
            } else {
                dirPath = UPLOAD_DIR;
            }

            //   System.out.println("Dir path: " + dirPath);

            path = Paths.get(dirPath + name + ".jpeg");

            System.out.println("TRY: " + path);


            byte[] imageBytes;
            try {
                imageBytes = Files.readAllBytes(path);
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(imageBytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping("/add_place")
    public @ResponseBody ResponseEntity<String> addNewPlace(@RequestHeader("api_key") String api_key,
                                                            @RequestHeader("id") Long id,
                                                            @RequestBody Place place) {
        JSONObject jsonObject = new JSONObject();
        if (authorize(api_key, id)) {
            if (place.getDescription().length() < 4000) {
                place.setAuthorId(id);
                jsonObject.put("response", Long.toString(placeRepository.save(place).getId()));
                return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
            } else {
                jsonObject.put("response", "Description over-length.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject.toString());

            }
        }
        jsonObject.put("response", "wrong auth.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject.toString());
    }

    // No secure yet. Mb make it open for world?
    @GetMapping("/place/{pId}")
    public @ResponseBody Optional<Place> getPlace(@RequestHeader("api_key") String api_key,
                                                  @RequestHeader("id") Long id, @PathVariable Long pId) {
        if (authorize(api_key, id)) {
            return placeRepository.findById(pId);
        }
        return Optional.empty();
    }

    // No secure yet. Mb make it open for world?
    @GetMapping("/trip/{id}")


    public @ResponseBody Optional<Trip> getTrip(@PathVariable Long id) {
        return tripRepository.findById(id);
    }

    @PostMapping("/add_trip")
    public @ResponseBody ResponseEntity<String> addNewTrip(@RequestHeader("api_key") String api_key,
                                                           @RequestHeader("id") Long id,
                                                           @RequestBody Trip trip) {
        JSONObject jsonObject = new JSONObject();
        if (authorize(api_key, id)) {
            if (trip.getDescription().length() < 7000) {
                trip.setAuthorId(id);
                jsonObject.put("response", Long.toString(tripRepository.save(trip).getId()));
                return ResponseEntity.status(HttpStatus.OK).body(jsonObject.toString());
            } else {
                jsonObject.put("response", "The description over-length.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject.toString());
            }
        }
        jsonObject.put("response", "wrong auth");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject.toString());
    }

    @GetMapping("/main/{id}")
    public @ResponseBody MainPresenter getMainContent(@RequestHeader("api_key") String api_key,
                                                      @PathVariable Long id) {

        // TODO: add snapshots cloud

        //  System.out.println("on getMain: id = " + Long.toString(id) + " key = " + api_key);

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
        //  System.out.println("NULL");
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


    @GetMapping("/places_cache")
    @ResponseBody
    public ResponseEntity<String> getPlacesCache(@RequestHeader("api_key") String api_key,
                                                 @RequestHeader("id") Long id) {
        JsonObject jsonObject = new JsonObject();

        if (authorize(api_key, id)) {
            if (placeRepository != null) {
                Iterable<Place> places = placeRepository.findAll();
                for (Place p : places) {
                    jsonObject.addProperty(p.getName(), Long.toString(p.getId()));
                }
            }
            //  System.out.println("JSON:" + jsonObject.toString());
            return ResponseEntity.ok(jsonObject.toString());
        }
        jsonObject.addProperty("response", "wrong auth");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject.toString());
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
        System.out.println("on PutImage: " + fileName);

        Long f_id = Long.valueOf(originFileName.substring(0, originFileName.indexOf('_')));

        String dirPath = null;


        if (originFileName.substring(originFileName.indexOf('_')).equals("_place_prev")
                || originFileName.contains("_place_addons")) {
            dirPath = UPLOAD_DIR + "places/";
            if (originFileName.contains("_place_addons")) {
                if (placeRepository.findById(f_id).isPresent()) {
                    Place place = null;
                    if (placeRepository.findById(f_id).isPresent())
                        place = placeRepository.findById(f_id).get();
                    if (place.getImagesCount() == null)
                        place.setImagesCount(1);
                    else
                        place.setImagesCount(place.getImagesCount() + 1);
                    placeRepository.save(place);
                }
            }
        } else if (originFileName.contains("_trip_prev")
                || originFileName.contains("_trip_addons")) {
            dirPath = UPLOAD_DIR + "trips/";
            if (originFileName.contains("_trip_addons")) {
                Trip trip = null;
                if (tripRepository.findById(f_id).isPresent()) {
                    trip = tripRepository.findById(f_id).get();
                    if (trip.getImagesCount() == null)
                        trip.setImagesCount(1);
                    else
                        trip.setImagesCount(trip.getImagesCount() + 1);
                    tripRepository.save(trip);
                }
            }
        } else if (originFileName.contains("_user")) {
            dirPath = UPLOAD_DIR + "users/";
        } else {
            dirPath = UPLOAD_DIR;
        }

        System.out.println("PATH to save: " + dirPath);
        System.out.println("NAME to save: " + fileName);

        try {
            path = Paths.get(dirPath + fileName);
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
            if (api_key.equals(appUser.getApiKey())) {
                System.out.println("200 for: " + api_key);
                return true;
            }
        }

//        AppUser appUser = null;
//        if (userRepository.findById(id).isPresent())
//            appUser = userRepository.findById(id).get();
//        if (appUser != null) {
//            if (appUser.isSent())
//                if (appUser.getRequestCount() < 15) {
//                    if (api_key.equals(appUser.getApiKey())) {
//                        appUser.setRequestCount(appUser.getRequestCount() + 1);
//
//                        System.out.println("Req. count: " + appUser.getRequestCount());
//
//                        userRepository.save(appUser);
//                        System.out.println("200 for: " + api_key);
//                        return true;
//                    }
//                }
//        }
        System.out.println("403 for: " + api_key);
        return false;
    }


    @PostMapping("/addVisitor/{pId}")
    public @ResponseBody ResponseEntity<String> addVisitor(@RequestHeader("api_key") String api_key,
                                                           @RequestHeader("id") Long id,
                                                           @PathVariable Long pId) {
        JSONObject jsonObject_response = new JSONObject();

        if (authorize(api_key, id)) {

            System.out.println("ADD_VISITOR: " + id + " PLACE: " + pId);
            if (placeRepository.findById(pId).isPresent()) {
                Place place = placeRepository.findById(pId).get();
                List<Long> visitors = place.getVisitorIds();
                if (visitors == null) {
                    visitors = new ArrayList<>();
                }
                visitors.add(id);
                place.setVisitorIds(visitors);
                placeRepository.save(place);
            }
            jsonObject_response.put("response", "added");
            return ResponseEntity.status(HttpStatus.OK).body(jsonObject_response.toString());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject_response.toString());
    }

    @PostMapping("/like/{pId}")
    public @ResponseBody ResponseEntity<String> like(@RequestHeader("api_key") String api_key,
                                                     @RequestHeader("id") Long id,
                                                     @PathVariable Long pId) {
        System.out.println("On Like");

        JSONObject jsonObject_response = new JSONObject();
        if (placeRepository.findById(pId).isPresent()) {
            Place place = placeRepository.findById(pId).get();
            List<Long> whoLikedList = place.getLikeIds();
            if (whoLikedList != null) {
                if (whoLikedList.contains(id)) {
                    whoLikedList.remove(id);
                } else {
                    whoLikedList.add(id);
                }
            } else {
                whoLikedList = new ArrayList<>();
                whoLikedList.add(id);
            }

            place.setLikeIds(whoLikedList);
            placeRepository.save(place);
            jsonObject_response.put("response", "like");
            ResponseEntity.status(HttpStatus.OK).body(jsonObject_response.toString());

        }
        jsonObject_response.put("response", "bad auth");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject_response.toString());
    }

    @GetMapping("/user_name/{uId}")
    public @ResponseBody ResponseEntity<String> getUserName(@RequestHeader("api_key") String api_key,
                                                            @RequestHeader("id") Long id,
                                                            @PathVariable Long uId) {
        JSONObject jsonObject_response = new JSONObject();
        if (authorize(api_key, id)) {
            if (userRepository.findById(uId).isPresent()) {
                AppUser appUser = userRepository.findById(id).get();
                jsonObject_response.put("fName", appUser.getFName());
                jsonObject_response.put("lName", appUser.getLName());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(jsonObject_response.toString());
        }
        jsonObject_response.put("response", "bad auth");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonObject_response.toString());
    }


}
























