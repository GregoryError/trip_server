package com.kaleidoscope.tripserver;


import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.User;
import com.kaleidoscope.tripserver.presenters.MainPresenter;
import com.kaleidoscope.tripserver.repositories.PlaceRepository;
import com.kaleidoscope.tripserver.repositories.UserRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.*;

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

    @GetMapping("/main/{id}")
    public @ResponseBody MainPresenter getMainContent(@PathVariable Long id) {
        MainPresenter presenter = new MainPresenter();
        User user;
        if (userRepository.findById(id).isPresent()) {
            user = userRepository.findById(id).get();
            presenter.setLocationName(user.getLocation()); // TODO: convert coordinates to name of place


            // TODO: List of advisable places for this user based on tags ond rating (likes)

            List<Place> placeList = (ArrayList) placeRepository.findAll();
            List<Integer> countList = new ArrayList<>();
            for (Place place : placeList) {
                int count = 0;
                List<Integer> placeTags = place.getTags();
                for (int i = 0; i < placeTags.size(); ++i) {
                    if (user.getListOfTags().contains(placeTags.get(i))) {
                        ++count;
                    }
                }
                countList.add(count);
            }

            List<Place> sortedPlacesByTags = new ArrayList<>();

            for (int j = 0; j < placeList.size(); ++j) {
                System.out.println("Count list: " +
                        countList);

                int index = 0;
                for (int i = 0; i < countList.size(); ++i) {
                    int max = 0;
                    if (countList.get(i) > max) {
                        max = countList.get(i);
                        index = i;
                    }
                }

                sortedPlacesByTags.add(placeList.get(index));
                countList.remove(index);
                countList.add(index, -1);

            }

            JSONObject jsonObject = new JSONObject();
            Map<String, String> jsonObjectPlace = new HashMap<>();

            for (Place place : sortedPlacesByTags) {
                jsonObjectPlace.put("name", place.getName());
                jsonObjectPlace.put("imageUrl", place.getImageUrl());
                jsonObject.put(Long.toString(place.getId()), jsonObjectPlace);
                jsonObjectPlace.clear();
            }

            presenter.setAdvicePlacesJson(jsonObject.toString());

            System.out.println("Sorted by tags: " +
                    sortedPlacesByTags);
            
        }

        return presenter;
    }

}






















