package com.example.choreapp;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserHolder {

    private DocumentReference user = null;

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user, final SharedPreferences prefs) {
        this.user = user;

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

                prefs.edit()
                    .putString(User.USER_ID, user.getId())
                    .apply();
            }
        });
    }

    public boolean hasUser() {
        return user != null;
    }

    private static final UserHolder userHolder = new UserHolder();

    public static UserHolder getInstance() {
        return userHolder;
    }
}
