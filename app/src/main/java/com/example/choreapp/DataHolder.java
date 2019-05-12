package com.example.choreapp;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.example.choreapp.main.tasks.TaskItemAdapter;
import com.example.choreapp.main.tasks.TaskUserAdapter;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

// Acts as a cache
public class DataHolder {

    private DocumentSnapshot user = null;
    private DocumentSnapshot currentGroup = null;

    private TaskItemAdapter.TaskItem selectedTask = null;
    private ArrayList<TaskUserAdapter.TaskUser> usersInGroup = null;

    public DocumentSnapshot getUser() {
        return user;
    }

    public TaskItemAdapter.TaskItem getTask() {
        return selectedTask;
    }

    public ArrayList<TaskUserAdapter.TaskUser> getUsersInGroup() {
        return usersInGroup;
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

    public void setUsersInGroup(ArrayList<TaskUserAdapter.TaskUser> usersInGroup) {
        this.usersInGroup = usersInGroup;
    }

    public void setTask(TaskItemAdapter.TaskItem task) {
        this.selectedTask = task;
    }

    public boolean hasUser() {
        return user != null;
    }

    public boolean hasGroup() {
        return currentGroup != null;
    }

    public boolean hasTask() {
        return selectedTask != null;
    }

    public boolean hasUsersInGroup() {
        return usersInGroup != null;
    }

    private static final DataHolder dataHolder = new DataHolder();

    public static DataHolder getInstance() {
        return dataHolder;
    }
}
