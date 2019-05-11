package com.example.choreapp;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.choreapp.models.Group;
import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// Acts as a cache
public class DataHolder {

    private DocumentSnapshot user = null;
    private DocumentSnapshot currentGroup = null;

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

        prefs.edit()
            .putString(User.USER_ID, user.getId())
            .apply();
    }

    public DocumentSnapshot getGroup() {
        return currentGroup;
    }

    public void setGroup(DocumentReference group, final SharedPreferences prefs) {
        group.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                DocumentSnapshot group = task.getResult();
                if (group == null) {
                    return;
                }

                DataHolder.getInstance().setGroup(group, prefs);
            }
        });
    }

    public void setGroup(DocumentSnapshot group, SharedPreferences prefs) {
        this.currentGroup = group;

        prefs.edit()
            .putString(Group.GROUP_ID, group.getId())
            .apply();
    }

    public boolean hasUser() {
        return user != null;
    }

    public boolean hasGroup() {
        return currentGroup != null;
    }

    private static final DataHolder dataHolder = new DataHolder();

    public static DataHolder getInstance() {
        return dataHolder;
    }
}
