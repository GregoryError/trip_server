package com.kaleidoscope.tripserver.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.context.annotation.Bean;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
public class Place implements TripItem{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("location")
    private String location;
    @JsonProperty("contacts")
    private String contacts;
    @JsonProperty("tags")
    @ElementCollection(targetClass = Integer.class)
    private List<Integer> tags;
    @JsonProperty("imageUrl")
    private String imageUrl;
    @JsonProperty("imageUrls")
    @ElementCollection(targetClass = String.class)
    private List<String> imageUrls;
    @JsonProperty("description")
    @Column(length = 3000)
    private String description;
    @JsonProperty("authorId")
    private long authorId;
    @JsonProperty("visitorIds")
    @ElementCollection(targetClass = Long.class)
    private List<Long> visitorIds;
    @JsonProperty("likes")
    private long likes;
    @JsonProperty("likeIds")
    @ElementCollection(targetClass = Long.class)
    private List<Long> likeIds;
}
