package com.kaleidoscope.tripserver.utils;

import com.kaleidoscope.tripserver.pojos.Place;
import com.kaleidoscope.tripserver.pojos.TripItem;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonBuilder {

    public JsonBuilder() {}

    public static JSONObject objectsByTags(List tripItemList, List<Integer> userTagsList) {
        List<Place> placeList = (ArrayList) tripItemList;
        List<Integer> countList = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        int order = 0;
        Map<String, String> jsonObjectPlace = new HashMap<>();
        for (Place place : placeList) {

            int count = 0;
            List<Integer> placeTags = place.getTags();
            for (int i = 0; i < placeTags.size(); ++i) {
                if (userTagsList.contains(placeTags.get(i))) {
                    ++count;
                }
            }
            countList.add(count);
        }

        for (int j = 0; j < placeList.size(); ++j) {
            int index = 0;
            for (int i = 0; i < countList.size(); ++i) {
                int max = 0;
                if (countList.get(i) > max) {
                    max = countList.get(i);
                    index = i;
                }
            }

            jsonObjectPlace.put("name", placeList.get(index).getName());
            jsonObjectPlace.put("imageUrl", placeList.get(index).getImageUrl());
            jsonObjectPlace.put("id", Long.toString(placeList.get(index).getId()));
            jsonObject.put(Integer.toString(order++), jsonObjectPlace);
            jsonObjectPlace.clear();

            countList.remove(index);
            countList.add(index, -1);
        }
        return jsonObject;
    }
}




















