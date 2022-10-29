package com.kaleidoscope.tripserver.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @JsonProperty("uId")
    private String uId;
    @JsonProperty("email")
    private String email;
    @JsonProperty("fName")
    private String fName;
    @JsonProperty("nName")
    private String nName;
    @JsonProperty("lName")
    private String lName;
    @JsonProperty("avatarUrl")
    private String avatarUrl;
    @JsonProperty("phone")
    private String phone;
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
    @JsonProperty("stories")
    @ElementCollection(targetClass = Long.class)
    private List<Long> stories;
    @JsonProperty("friends")
    @ElementCollection(targetClass = Long.class)
    private List<Long> friends;
}



