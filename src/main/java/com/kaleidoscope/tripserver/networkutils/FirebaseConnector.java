package com.kaleidoscope.tripserver.networkutils;

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

    public FirebaseConnector() {
        try {
            FileInputStream serviceAccount =
                    new FileInputStream(SERVICE_KEY);

//            FirebaseOptions.Builder builder = FirebaseOptions.builder();

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
        UserRecord userRecord = null;
        try {
            userRecord = firebaseAuth.getUser(uId);
            if (userRecord != null) {
                if ((userRecord.getEmail() != null) && userRecord.getEmail().equals(name))
                    return true;
                if ((userRecord.getPhoneNumber() != null) && userRecord.getPhoneNumber().equals(name))
                    return true;
            }
        } catch (FirebaseAuthException e) {
            throw new FirebaseAuthException(e);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getEmail(String uId) {
        try {
            UserRecord user = firebaseAuth.getUser(uId);
            if (user != null && user.getEmail() != null) {
                return user.getEmail();
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public String getPhone(String uId) {
        try {
            UserRecord user = firebaseAuth.getUser(uId);
            if (user != null && user.getPhoneNumber() != null) {
                return user.getPhoneNumber();
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public String getName(String uId) {
        try {
            UserRecord user = firebaseAuth.getUser(uId);
            if (user != null && user.getDisplayName() != null) {
                return user.getDisplayName();
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    public String getPhotoUrl(String uId) {
        try {
            UserRecord user = firebaseAuth.getUser(uId);
            if (user != null && user.getPhotoUrl() != null) {
                return user.getPhotoUrl();
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
        return "";
    }
}













