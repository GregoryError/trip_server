package com.kaleidoscope.tripserver.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.context.annotation.Bean;

import javax.persistence.*;
import java.util.List;

@Entity
public class Place implements TripItem {

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

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public String getContacts() {
        return contacts;
    }

    @Override
    public List<Integer> getTags() {
        return tags;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public String getDescription() {
        return description;
    }

    public long getAuthorId() {
        return authorId;
    }

    public List<Long> getVisitorIds() {
        return visitorIds;
    }

    @Override
    public long getLikes() {
        return likes;
    }

    public List<Long> getLikeIds() {
        return likeIds;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    @Override
    public void setTags(List<Integer> tags) {
        this.tags = tags;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
    }

    public void setVisitorIds(List<Long> visitorIds) {
        this.visitorIds = visitorIds;
    }

    @Override
    public void setLikes(long likes) {
        this.likes = likes;
    }

    public void setLikeIds(List<Long> likeIds) {
        this.likeIds = likeIds;
    }
}
