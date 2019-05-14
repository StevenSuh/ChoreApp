package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Group {

    public static String COLLECTION = "groups";
    public static String GROUP_ID = "group_id";
    public static String NAME = "name";
    public static String USERS = "users";
    public static String TASKS = "tasks";
    public static String ACTIVITIES = "activities";
    public static String MESSAGES = "messages";

    public DocumentReference groupRef;
    public String name;
    public ArrayList<DocumentReference> users = new ArrayList<>();
    public ArrayList<DocumentReference> tasks = new ArrayList<>();
    public ArrayList<DocumentReference> activities = new ArrayList<>();
    public ArrayList<DocumentReference> messages = new ArrayList<>();

    public Group(String name) {
        this.name = name;
    }

    public Group(DocumentReference groupRef, Map<String, Object> map) {
        this.groupRef = groupRef;
        this.name = (String) map.get(NAME);
        this.users = (ArrayList<DocumentReference>) map.get(USERS);
        this.tasks = (ArrayList<DocumentReference>) map.get(TASKS);
        this.activities = (ArrayList<DocumentReference>) map.get(ACTIVITIES);
        this.messages = (ArrayList<DocumentReference>) map.get(MESSAGES);
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