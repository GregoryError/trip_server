package com.kaleidoscope.tripserver.utils;

import com.kaleidoscope.tripserver.pojos.TripItem;
import org.json.JSONObject;
import java.util.*;

public class JsonBuilder {

    private static JsonBuilder instance = null;

    private JsonBuilder() {
    }


    public static JsonBuilder getInstance() {
        if (instance == null) {
            instance = new JsonBuilder();
        }
        return instance;
    }

    public List<TripItem> objectsByTags(List<TripItem> tripItemList, List<Integer> userTagsList) {
        List<TripItem> itemList = new ArrayList<>();

        List<Integer> countList = new ArrayList<>();
        for (TripItem item : tripItemList) {
            int count = 0;
            List<Integer> itemTags = item.getTags();
            for (int i = 0; i < itemTags.size(); ++i) {
                if (userTagsList.contains(itemTags.get(i))) {
                    ++count;
                }
            }
            countList.add(count);
        }

        int index = -1;
        int max = -1;

        for (int j = 0; j < tripItemList.size(); ++j) {
            index = -1;
            max = -1;
            for (int i = 0; i < countList.size(); ++i) {
                if (countList.get(i) > max) {
                    max = countList.get(i);
                    index = i;
                }
            }

            if (index > -1) {
                itemList.add(tripItemList.get(index));
                countList.remove(index);
                countList.add(index, -1);
            }
        }

        return itemList;
    }

    private JSONObject jsonFromTripItemList(List<TripItem> tripItemList) {
        JSONObject jsonObject = new JSONObject();
        Map<String, String> jsonObjectItem = new HashMap<>();
        int order = 0;

        for (TripItem item : tripItemList) {
            jsonObjectItem.put("name", item.getName());
            jsonObjectItem.put("id", Long.toString(item.getId()));
            jsonObject.put(Integer.toString(order++), jsonObjectItem);
            jsonObjectItem.clear();
        }
        return jsonObject;
    }

    public JSONObject placesByTags(List tripItemList, List<Integer> userTagsList) {
        List<TripItem> placeList = objectsByTags(tripItemList, userTagsList);
        return jsonFromTripItemList(placeList);
    }

    public JSONObject objectsByRatingAndTags(List tripItemList, List<Integer> userTagsList) {
        List<TripItem> tripList = objectsByTags(tripItemList, userTagsList);
        tripList.sort((o1, o2) -> (o1.getLikes() <= o2.getLikes()) ? 0 : -1);
        return jsonFromTripItemList(tripList);
    }

}




















