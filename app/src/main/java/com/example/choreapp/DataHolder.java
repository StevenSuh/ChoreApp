package com.example.choreapp;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    private DocumentSnapshot user = null;

    private List<DocumentSnapshot> groups = new ArrayList<>();

    public DocumentSnapshot getUser() {
        return user;
    }

    public void setUser(DocumentReference user, final SharedPreferences prefs) {
        user.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                DocumentSnapshot user = task.getResult();
                if (user == null) {
                    return;
                }

                DataHolder.getInstance().setUser(user, prefs);
            }
        });
    }

    public void setUser(DocumentSnapshot user, SharedPreferences prefs) {
        this.user = user;
        this.groups = new ArrayList<>();

        prefs.edit()
            .putString(User.USER_ID, user.getId())
            .apply();
    }

    public boolean hasUser() {
        return user != null;
    }

    public boolean hasGroups() {
        return !groups.isEmpty();
    }

    public void addGroup(DocumentSnapshot group) {
        groups.add(group);
    }

    private static final DataHolder dataHolder = new DataHolder();

    public static DataHolder getInstance() {
        return dataHolder;
    }
}
