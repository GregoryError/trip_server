package com.kaleidoscope.tripserver.utils;

import org.apache.commons.codec.binary.Hex;

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
        key_builder.append("." + uId);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(key_builder.toString().getBytes());
            messageDigest.update(".Iam a teapot".getBytes());
            api_key = String.valueOf(Hex.encodeHex(messageDigest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return api_key;
    }
}
