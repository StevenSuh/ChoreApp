package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class Task {

    public static String COLLECTION = "tasks";

    public static String NAME = "name";
    public static String POINTS = "points";
    public static String ASSIGNED_USER = "assigned_user";
    public static String REASSIGN_INTERVAL = "reassign_interval";
    public static String IS_DONE = "is_done";
    public static String GROUP = "group";

    public String name;
    public long points;
    public DocumentReference assigned_user;
    public String reassign_interval;
    public boolean is_done;
    public DocumentReference group;

    public Task(String name,
                long points,
                DocumentReference assigned_user,
                String reassign_interval,
                boolean is_done,
                DocumentReference group) {
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
