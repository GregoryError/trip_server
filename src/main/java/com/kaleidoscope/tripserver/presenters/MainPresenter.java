package com.kaleidoscope.tripserver.presenters;

import org.json.JSONObject;

public class MainPresenter {

    private String locationName;
    private String headImgUrl;
    private String advicePlacesJson;
    private String topPlacesJson;
    private String storiesJson;
    private String topTripsJson;

    public MainPresenter() {}

    public MainPresenter(String locationName, String headImgUrl, String advicePlacesJson, String topPlacesJson, String storiesJson, String topTripsJson) {
        this.locationName = locationName;
        this.headImgUrl = headImgUrl;
        this.advicePlacesJson = advicePlacesJson;
        this.topPlacesJson = topPlacesJson;
        this.storiesJson = storiesJson;
        this.topTripsJson = topTripsJson;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public String getAdvicePlacesJson() {
        return advicePlacesJson;
    }

    public String getTopPlacesJson() {
        return topPlacesJson;
    }

    public String getStoriesJson() {
        return storiesJson;
    }

    public String getTopTripsJson() {
        return topTripsJson;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public void setAdvicePlacesJson(String advicePlacesJson) {
        this.advicePlacesJson = advicePlacesJson;
    }

    public void setTopPlacesJson(String topPlacesJson) {
        this.topPlacesJson = topPlacesJson;
    }

    public void setStoriesJson(String storiesJson) {
        this.storiesJson = storiesJson;
    }

    public void setTopTripsJson(String topTripsJson) {
        this.topTripsJson = topTripsJson;
    }
}



























