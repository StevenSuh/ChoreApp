package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    public static String COLLECTION = "groups";
    public static String NAME = "name";
    public static String USERS = "users";
    public static String TASKS = "tasks";
    public static String ACTIVITIES = "activities";
    public static String MESSAGES = "messages";

    String name;
    public List<DocumentReference> users = new ArrayList<>();
    public List<DocumentReference> tasks = new ArrayList<>();
    public List<DocumentReference> activities = new ArrayList<>();
    public List<DocumentReference> messages = new ArrayList<>();

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