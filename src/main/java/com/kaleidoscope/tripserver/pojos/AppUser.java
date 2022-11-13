package com.kaleidoscope.tripserver.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
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
    private int stories = 0;
    @ElementCollection(targetClass = Long.class)
    private List<Long> storiesTimeStamps;
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

}



