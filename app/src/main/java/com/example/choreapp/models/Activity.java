package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Activity {

    public static String COLLECTION = "activities";

    String task_name;
    String user_name;
    int points;
    Date date;
    DocumentReference group;

    public Activity(String task_name, String user_name, int points, Date date, DocumentReference group) {
        this.task_name = task_name;
        this.user_name = user_name;
        this.points = points;
        this.date = date;
        this.group = group;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> activity = new HashMap<>();
        activity.put("task_name", task_name);
        activity.put("user_name", user_name);
        activity.put("points", points);
        activity.put("date", date);
        activity.put("group", group);

        return activity;
    }
}