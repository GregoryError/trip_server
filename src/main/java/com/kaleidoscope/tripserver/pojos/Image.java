package com.kaleidoscope.tripserver.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {

    @JsonProperty("file")
    private byte[] file;

    @JsonProperty("name")
    private String name;

    public Image() {

    }

    public Image(byte[] file, String name) {
        this.file = file;
        this.name = name;
    }

    public byte[] getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public void setName(String name) {
        this.name = name;
    }
}
