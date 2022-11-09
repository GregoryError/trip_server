package com.kaleidoscope.tripserver.pojos;

import java.util.List;

public interface TripItem {
    List<Integer> getTags();

    void setTags(List<Integer> tags);

    long getLikes();

    void setLikes(long likes);

    String getName();

    String getImageUrl();

    long getId();
}
