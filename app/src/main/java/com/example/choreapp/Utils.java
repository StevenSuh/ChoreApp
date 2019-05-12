package com.example.choreapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.choreapp.main.AccountActivity;
import com.example.choreapp.main.GroupsActivity;
import com.example.choreapp.main.MessagesActivity;
import com.example.choreapp.main.tasks.TasksActivity;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.User;
import com.example.choreapp.signup.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class Utils {

    private static ConnectivityManager connectivityManager;

    public static void setTouchEffect(View view, final boolean goDark, boolean extraDark, final boolean withClick) {
        final float lowOpacity = extraDark ? defs.EXTRA_LOW_OPACITY : defs.LOW_OPACITY;

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(goDark ? lowOpacity : 1f);
                    return !withClick;
                } else {
                    v.setAlpha(goDark ? 1f : lowOpacity);
                }
                return false;
            }
        });
    }

    public static boolean isNetworkAvailable(Context context) {
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void initDataHolder(final Context context, final Runnable init, final Runnable callback) {
        final SharedPreferences prefs = context.getSharedPreferences(defs.SHARED_PREF, Context.MODE_PRIVATE);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (!isNetworkAvailable(context)) {
            Toast.makeText(context, "No internet", Toast.LENGTH_SHORT).show();
            return;
        }

        final String groupId = prefs.getString(Group.GROUP_ID, "");

        if (!DataHolder.getInstance().hasUser()) {
            init.run();
            String userId = prefs.getString(User.USER_ID, "");

            db.collection(User.COLLECTION)
                .document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DocumentSnapshot user = task.getResult();
                        DataHolder.getInstance().setUser(user, prefs);

                        if (TextUtils.isEmpty(groupId)) {
                            ArrayList<DocumentReference> groups = (ArrayList<DocumentReference>) user.get(User.GROUPS);
                            DocumentReference group = groups.get(0);

                            if (group == null) {
                                nukePrefs(context);
                                return;
                            }

                            getGroupData(group.getId(), context, prefs, callback);
                            return;
                        }
                        getGroupData(groupId, context, prefs, callback);
                    }
                });
            return;
        }

        if (!DataHolder.getInstance().hasGroup()) {
            init.run();
            if (TextUtils.isEmpty(groupId)) {
                DocumentSnapshot user = DataHolder.getInstance().getUser();
                ArrayList<DocumentReference> groups = (ArrayList<DocumentReference>) user.get(User.GROUPS);
                DocumentReference group = groups.get(0);

                if (group == null) {
                    nukePrefs(context);
                    return;
                }

                getGroupData(group.getId(), context, prefs, callback);
                return;
            }
            getGroupData(groupId, context, prefs, callback);
            return;
        }
    }

    private static void getGroupData(String groupId,
                                     final Context context,
                                     final SharedPreferences prefs,
                                     final Runnable callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(Group.COLLECTION)
                .document(groupId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        DataHolder.getInstance().setGroup(task.getResult(), prefs);
                        callback.run();
                    }
                });
    }

    public static void initNavbar(View group, View tasks, View messages, View account, String type, final Context context) {
        group.setAlpha(type.equals("group") ? 1f : defs.LOW_OPACITY);
        tasks.setAlpha(type.equals("tasks") ? 1f : defs.LOW_OPACITY);
        messages.setAlpha(type.equals("messages") ? 1f : defs.LOW_OPACITY);
        account.setAlpha(type.equals("account") ? 1f : defs.LOW_OPACITY);

        Utils.setTouchEffect(group, false, false, true);
        Utils.setTouchEffect(tasks, false, false, true);
        Utils.setTouchEffect(messages, false, false, true);
        Utils.setTouchEffect(account, false, false, true);

        group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, GroupsActivity.class);
                context.startActivity(intent);
            }
        });
        tasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TasksActivity.class);
                context.startActivity(intent);
            }
        });
        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MessagesActivity.class);
                context.startActivity(intent);
            }
        });
        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AccountActivity.class);
                context.startActivity(intent);
            }
        });

        switch (type) {
            case "group":
                group.setOnTouchListener(null);
                group.setOnClickListener(null);
                break;
            case "tasks":
                tasks.setOnTouchListener(null);
                tasks.setOnClickListener(null);
                break;
            case "messages":
                messages.setOnTouchListener(null);
                messages.setOnClickListener(null);
                break;
            case "account":
                account.setOnTouchListener(null);
                account.setOnClickListener(null);
                break;
            default:
                group.setOnTouchListener(null);
                group.setOnClickListener(null);
                break;
        }
    }

    public static void nukePrefs(Context context) {
        Toast.makeText(context, "Account error", Toast.LENGTH_SHORT).show();

        SharedPreferences prefs = context.getSharedPreferences(defs.SHARED_PREF, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
