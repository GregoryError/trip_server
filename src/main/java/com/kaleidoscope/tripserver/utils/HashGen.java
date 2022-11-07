package com.kaleidoscope.tripserver.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HashGen {
    private static HashGen instance = null;
    private HashGen() {

    }

    public static HashGen getInstance() {
        if (instance == null) {
            instance = new HashGen();
        }
        return instance;
    }

    public String generate(String uId) {
        StringBuilder key_builder = new StringBuilder();
        String api_key = null;
        key_builder.append(DateTimeFormatter
                .ofPattern("yyyy/MM/dd HH:mm:ss")
                .format(LocalDateTime.now()));
        key_builder.append("." + uId + ".Iam a teapot");
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            System.out.println("Salt: " + key_builder.toString());
            messageDigest.update(key_builder.toString().getBytes());
            api_key = new String(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return api_key;
    }
}
