package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class Task {

    public static String COLLECTION = "tasks";

    String name;
    int points;
    DocumentReference assigned_user;
    String reassign_interval;
    boolean is_done;
    DocumentReference group;

    public Task(String name, int points, DocumentReference assigned_user, String reassign_interval, boolean is_done, DocumentReference group) {
        this.name = name;
        this.points = points;
        this.assigned_user = assigned_user;
        this.reassign_interval = reassign_interval;
        this.is_done = is_done;
        this.group = group;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> task = new HashMap<>();
        task.put("name", name);
        task.put("points", points);
        task.put("assigned_user", assigned_user);
        task.put("reassign_interval", reassign_interval);
        task.put("is_done", is_done);
        task.put("group", group);

        return task;
    }
}
