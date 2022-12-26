package com.kaleidoscope.tripserver.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.List;


@Entity
public class Trip implements TripItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @JsonProperty("name")
    private String name;


    public Integer getImagesCount() {
        return imagesCount;
    }

    public void setImagesCount(Integer imagesCount) {
        this.imagesCount = imagesCount;
    }

    @JsonProperty("imagesCount")
    private Integer imagesCount;


    public List<Long> getPlacesList() {
        return placesList;
    }

    public void setPlacesList(List<Long> placesList) {
        this.placesList = placesList;
    }

    @JsonProperty("placesList")
    @ElementCollection(targetClass = Long.class)
    List<Long> placesList;

    @JsonProperty("contacts")
    private String contacts;
    @JsonProperty("tags")
    @ElementCollection(targetClass = Integer.class)
    private List<Integer> tags;
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

    public String getContacts() {
        return contacts;
    }

    @Override
    public List<Integer> getTags() {
        return tags;
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

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    @Override
    public void setTags(List<Integer> tags) {
        this.tags = tags;
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

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public void setLikeIds(List<Long> likeIds) {
        this.likeIds = likeIds;
    }
}