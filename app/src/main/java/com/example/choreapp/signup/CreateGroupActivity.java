package com.example.choreapp.signup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.defs;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupActivity extends AppCompatActivity {

    private boolean isCreating = false;

    private EditText groupNameView;
    private EditText groupLinkView;

    private TextView nextTextView;
    private ProgressBar nextProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        LinearLayout backButton = findViewById(R.id.back);
        Utils.setTouchEffect(backButton, true, false, true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        groupNameView = findViewById(R.id.create_group_name);
        groupLinkView = findViewById(R.id.create_group_link);

        RelativeLayout nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeCreateGroup();
            }
        });

        nextTextView = findViewById(R.id.next_text);
        nextProgressView = findViewById(R.id.next_progress);
    }

    public void executeCreateGroup() {
        if (isCreating) {
            return;
        }

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_LONG).show();
            return;
        }

        groupNameView.setError(null);
        groupLinkView.setError(null);

        String groupName = groupNameView.getText().toString();
        String groupCode = groupLinkView.getText().toString();

        if (TextUtils.isEmpty(groupName) && TextUtils.isEmpty(groupCode)) {
            Toast.makeText(this, "Both fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(groupName) && !TextUtils.isEmpty(groupCode)) {
            Toast.makeText(this, "Both fields cannot be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        if (TextUtils.isEmpty(groupCode)) {
            createNewGroup(groupName);
        } else {
            checkGroupByCode(groupCode);
        }
    }

    public void createNewGroup(String name) {
        Group newGroup = new Group(name);

        if (DataHolder.getInstance().hasUser()) {
            newGroup.users.add(DataHolder.getInstance().getUser().userRef);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Group.COLLECTION)
            .add(newGroup.toMap())
            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if (!task.isSuccessful()) {
                        failCreateGroup(null);
                        return;
                    }
                    successCreateGroup(task.getResult());
                }
            });
    }

    public void checkGroupByCode(final String code) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Group.COLLECTION)
            .document(code).get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (!task.isSuccessful()) {
                        failCreateGroup("Code is invalid");
                        return;
                    }

                    DocumentSnapshot groupSnapshot = task.getResult();
                    if (groupSnapshot == null) {
                        failCreateGroup("Code is invalid");
                        return;
                    }

                    joinGroup(groupSnapshot);
                }
            });
    }

    public void joinGroup(final DocumentSnapshot group) {
        List<DocumentReference> users = (List<DocumentReference>) group.get(Group.USERS);
        if (users == null) {
            users = new ArrayList<>();
        }
        users.add(DataHolder.getInstance().getUser().userRef);

        group.getReference()
            .update(Group.USERS, users)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        failCreateGroup(null);
                        return;
                    }
                    successCreateGroup(group.getReference());
                }
            });
    }

    public void successCreateGroup(final DocumentReference group) {
        final User user = DataHolder.getInstance().getUser();

        if (user.groups == null) {
            user.groups = new ArrayList<>();
        }
        user.groups.add(group);
        DataHolder.getInstance().setGroup(group, getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE));

        user.userRef
            .update(User.GROUPS, user.groups)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        failCreateGroup(null);
                        return;
                    }
                    showProgress(false);

                    getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE)
                            .edit()
                            .putBoolean(defs.IS_LOGGED_IN, true)
                            .apply();
                    Intent intent = new Intent(CreateGroupActivity.this, InviteActivity.class);
                    intent.putExtra(defs.INVITE_CODE, group.getId());
                    startActivity(intent);
                }
            });
    }

    public void failCreateGroup(String error) {
        if (error == null) {
            error = "Server error";
        }

        showProgress(false);
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    public void showProgress(final boolean show) {
        isCreating = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        nextTextView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        nextTextView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        nextProgressView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        nextProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }
}
