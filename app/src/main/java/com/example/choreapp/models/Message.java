package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message {

    public static String COLLECTION = "messages";

    public static String CONTENT = "content";
    public static String IS_ANONYMOUS = "is_anonymous";
    public static String USERNAME = "user_name";
    public static String USER = "user";
    public static String GROUP = "group";
    public static String DATE = "date";

    public DocumentReference messageRef;
    public String content;
    public boolean is_anonymous;
    public String user_name;
    public DocumentReference user;
    public DocumentReference group;
    public Date date;

    public Message(String content, boolean is_anonymous, String user_name, DocumentReference user, DocumentReference group, Date date) {
        this.content = content;
        this.is_anonymous = is_anonymous;
        this.user_name = user_name;
        this.user = user;
        this.group = group;
        this.date = date;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("content", content);
        message.put("is_anonymous", is_anonymous);
        message.put("user_name", user_name);
        message.put("user", user);
        message.put("group", group);
        message.put("date", date);

        return message;
    }
}