package com.kaleidoscope.tripserver.networkutils;

import com.google.api.gax.rpc.NotFoundException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FirebaseConnector {
    private final String SERVICE_KEY =
            "/Users/user/IdeaProjects/tripserver/src/main/resources/static/trip-friend-4409c-firebase-adminsdk-y41p3-36b843a233.json";

    private static FirebaseConnector instance = null;
    private FirebaseAuth firebaseAuth = null;

    private FirebaseConnector() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream(SERVICE_KEY);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static FirebaseConnector getInstance() {
        if (instance == null) {
            instance = new FirebaseConnector();
        }
        return instance;
    }

    public boolean checkUser(String name, String uId) throws FirebaseAuthException {
        try {
            UserRecord user = firebaseAuth.getUser(uId);
            if ((user.getEmail() != null) && user.getEmail().equals(name))
                return true;
            if ((user.getPhoneNumber() != null) && user.getPhoneNumber().equals(name))
                return true;
        } catch (FirebaseAuthException e) {
            System.out.println("FirebaseAuthException");
            throw new FirebaseAuthException(e);
        } catch (RuntimeException e) {
            System.out.println("RuntimeException");
            e.printStackTrace();
        }
        return false;
    }

    public String getEmail(String uId) {
        try {
            UserRecord user = firebaseAuth.getUser(uId);
            if (user.getEmail() != null) {
                return user.getEmail();
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String getPhone(String uId) {
        try {
            UserRecord user = firebaseAuth.getUser(uId);
            if (user.getPhoneNumber() != null) {
                return user.getPhoneNumber();
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}













