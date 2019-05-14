package com.example.choreapp.models;

import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;
import java.util.Map;

public class Message {

    public static String COLLECTION = "messages";

    public DocumentReference messageRef;
    public String content;
    public boolean is_anonymous;
    public DocumentReference user;
    public DocumentReference group;

    public Message(String content, boolean is_anonymous, DocumentReference user, DocumentReference group) {
        this.content = content;
        this.is_anonymous = is_anonymous;
        this.user = user;
        this.group = group;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> message = new HashMap<>();
        message.put("content", content);
        message.put("is_anonymous", is_anonymous);
        message.put("user", user);
        message.put("group", group);

        return message;
    }
}