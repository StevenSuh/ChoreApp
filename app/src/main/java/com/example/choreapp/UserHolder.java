package com.example.choreapp;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserHolder {

    private DocumentReference userRef = null;
    private DocumentSnapshot userSnapshot = null;

    public DocumentReference getUserRef() {
        return userRef;
    }

    public DocumentSnapshot getUserSnapshot() {
        return userSnapshot;
    }

    public void setUser(DocumentReference user, final SharedPreferences prefs) {
        this.userRef = user;

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

                userSnapshot = user;
                prefs.edit()
                    .putString(User.USER_ID, user.getId())
                    .apply();
            }
        });
    }

    public void setUser(DocumentSnapshot user, SharedPreferences prefs) {
        this.userSnapshot = user;
        this.userRef = user.getReference();

        prefs.edit()
            .putString(User.USER_ID, user.getId())
            .apply();
    }

    public boolean hasUserRef() {
        return userRef != null;
    }

    public boolean hasUserSnapshot() {
        return userSnapshot != null;
    }

    private static final UserHolder userHolder = new UserHolder();

    public static UserHolder getInstance() {
        return userHolder;
    }
}
