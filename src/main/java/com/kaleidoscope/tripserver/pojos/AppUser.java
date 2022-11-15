package com.kaleidoscope.tripserver.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
import java.util.List;

@Entity
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @JsonProperty("uId")
    private String uId;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;

    private String ApiKey;
    private boolean sent;
    private int checkCount = 0;
    private int requestCount = 0;
    @ElementCollection(targetClass = Long.class)
    private List<Long> storiesTimeStamps;
    public void addStoriesTimeStamp(Long s) {
        storiesTimeStamps.add(s);
    }

    @ElementCollection(targetClass = Long.class)
    private List<Long> friends;
    public void addFriend(Long id) {
        friends.add(id);
    }

    @JsonProperty("fName")
    private String fName;
    @JsonProperty("nName")
    private String nName;
    @JsonProperty("lName")
    private String lName;
    @JsonProperty("location")
    private String location;
    @JsonProperty("listOfTags")
    @ElementCollection(targetClass = Integer.class)
    private List<Integer> listOfTags;
    @JsonProperty("info")
    @Column(length = 700)
    private String info;
    @JsonProperty("places")
    @ElementCollection(targetClass = Long.class)
    private List<Long> places;
    @JsonProperty("trips")
    @ElementCollection(targetClass = Long.class)
    private List<Long> trips;

    public void setId(long id) {
        this.id = id;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setApiKey(String apiKey) {
        ApiKey = apiKey;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public void setCheckCount(int checkCount) {
        this.checkCount = checkCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public void setStoriesTimeStamps(List<Long> storiesTimeStamps) {
        this.storiesTimeStamps = storiesTimeStamps;
    }

    public void setFriends(List<Long> friends) {
        this.friends = friends;
    }

    public void setFName(String fName) {
        this.fName = fName;
    }

    public void setNName(String nName) {
        this.nName = nName;
    }

    public void setLName(String lName) {
        this.lName = lName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setListOfTags(List<Integer> listOfTags) {
        this.listOfTags = listOfTags;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setPlaces(List<Long> places) {
        this.places = places;
    }

    public void setTrips(List<Long> trips) {
        this.trips = trips;
    }

    public long getId() {
        return id;
    }

    public String getUId() {
        return uId;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getApiKey() {
        return ApiKey;
    }

    public boolean isSent() {
        return sent;
    }

    public int getCheckCount() {
        return checkCount;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public List<Long> getStoriesTimeStamps() {
        return storiesTimeStamps;
    }

    public List<Long> getFriends() {
        return friends;
    }

    public String getFName() {
        return fName;
    }

    public String getNName() {
        return nName;
    }

    public String getLName() {
        return lName;
    }

    public String getLocation() {
        return location;
    }

    public List<Integer> getListOfTags() {
        return listOfTags;
    }

    public String getInfo() {
        return info;
    }

    public List<Long> getPlaces() {
        return places;
    }

    public List<Long> getTrips() {
        return trips;
    }
}



