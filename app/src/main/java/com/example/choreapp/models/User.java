package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User {

    public static String COLLECTION = "users";
    public static String USER_ID = "user_id";
    public static String GOOGLE_ID = "google_id";

    public static String NAME = "name";
    public static String COLOR = "color";
    public static String GROUPS = "groups";

    String name;
    String google_id;
    public List<DocumentReference> groups = new ArrayList<>();
    String color;

    public User(String name, String google_id) {
        this.name = name;
        this.google_id = google_id;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("google_id", google_id);
        user.put("groups", groups);
        user.put("color", color);

        return user;
    }
}