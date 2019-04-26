package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    public static String COLLECTION = "groups";

    String name;
    List<DocumentReference> users;
    List<DocumentReference> tasks;
    List<DocumentReference> activities;
    List<DocumentReference> messages;

    public Group(String name) {
        this.name = name;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> group = new HashMap<>();
        group.put("name", name);
        group.put("users", users);
        group.put("tasks", tasks);
        group.put("activities", activities);
        group.put("messages", messages);

        return group;
    }
}