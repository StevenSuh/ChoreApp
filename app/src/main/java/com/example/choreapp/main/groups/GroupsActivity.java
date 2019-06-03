package com.example.choreapp.main.groups;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.main.messages.MessagingService;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.User;
import com.example.choreapp.signup.CreateGroupActivity;
import com.example.choreapp.signup.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class GroupsActivity extends AppCompatActivity {

    private boolean isLoading = false;

    public static int GROUP_DETAIL = 0;
    public static int GROUP_ADD = 1;

    public static int RESULT_DELETE = 2;

    private TextView noGroup;
    private RecyclerView groupsList;
    private GroupItemAdapter groupItemAdapter;

    private ImageView addButton;
    private View groupsWrapper;
    private View navbar;
    private View navbarLine;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        boolean isLoggedIn = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE).getBoolean(defs.IS_LOGGED_IN, false);

        if (!isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, MessagingService.class);
            startService(intent);
        }

        addButton = findViewById(R.id.add_button);
        Utils.setTouchEffect(addButton, true, false, true);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataHolder.getInstance().setTask(null);
                Intent intent = new Intent(GroupsActivity.this, CreateGroupActivity.class);
                startActivityForResult(intent, GROUP_ADD);
            }
        });

        noGroup = findViewById(R.id.no_group);
        groupsList = findViewById(R.id.groups_list);
        groupItemAdapter = new GroupItemAdapter(new ArrayList<GroupItemAdapter.GroupItem>(), this);
        groupsList.setDrawingCacheEnabled(true);
        groupsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        groupsList.setAdapter(groupItemAdapter);

        groupsWrapper = findViewById(R.id.groups_wrapper);
        navbar = findViewById(R.id.navbar);
        navbarLine = findViewById(R.id.navbar_line);
        progressBar = findViewById(R.id.progress);

        View groupButton = findViewById(R.id.group_button);
        View tasksButton = findViewById(R.id.tasks_button);
        View messagesButton = findViewById(R.id.messages_button);
        View accountButton = findViewById(R.id.account_button);

        Utils.initDataHolder(this, new Runnable() {
            @Override
            public void run() {
                showProgress(true);
            }
        }, new Runnable() {
            @Override
            public void run() {
                Group group = DataHolder.getInstance().getGroup();
                MessagingService.subscribeTo(group.groupRef);
                showProgress(false);
                loadGroups();
            }
        });
        Utils.initNavbar(groupButton, tasksButton, messagesButton, accountButton, defs.GROUP_PAGE, this);

        if (DataHolder.getInstance().hasUser() && DataHolder.getInstance().hasGroup()) {
            loadGroups();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            groupItemAdapter.notifyDataSetChanged();
            return;
        }

        if (requestCode == GROUP_DETAIL) {
            if (resultCode == RESULT_DELETE) {
                GroupItemAdapter.GroupItem groupItem = DataHolder.getInstance().getCurrentDetailGroup();
                groupItemAdapter.deleteItem(groupItem);

                if (groupItemAdapter.getItemCount() == 0) {
                    groupsList.animate()
                            .setDuration(200)
                            .alpha(0)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    groupsList.setVisibility(View.INVISIBLE);
                                }
                            });

                    noGroup.animate()
                            .setDuration(200)
                            .alpha(1)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    noGroup.setVisibility(View.VISIBLE);
                                }
                            });
                }
            }
            groupItemAdapter.notifyDataSetChanged();
        }
        if (requestCode == GROUP_ADD) {
            Group group = DataHolder.getInstance().getGroup();
            GroupItemAdapter.GroupItem groupItem = new GroupItemAdapter.GroupItem(group.name,
                    group.users,
                    group.tasks,
                    group.activities,
                    group.messages,
                    group.groupRef);

            groupItemAdapter.addItem(groupItem);
            groupsList.animate()
                    .setDuration(200)
                    .alpha(1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            groupsList.setVisibility(View.VISIBLE);
                        }
                    });

            noGroup.animate()
                    .setDuration(200)
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            noGroup.setVisibility(View.INVISIBLE);
                        }
                    });
        }
    }

    private void loadGroups() {
        if (isLoading) {
            return;
        }

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_LONG).show();
            return;
        }

        if (!DataHolder.getInstance().hasUser() || !DataHolder.getInstance().hasGroup()) {
            Toast.makeText(this, "Server error", Toast.LENGTH_LONG).show();
            return;
        }

        showLoadProgress(true);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final User user = DataHolder.getInstance().getUser();

        if (user.groups == null) {
            Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(Group.COLLECTION)
                .whereArrayContains(Group.USERS, user.userRef)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(GroupsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            showLoadProgress(false);
                            return;
                        }

                        QuerySnapshot result = task.getResult();
                        if (result == null || result.isEmpty()) {
                            showLoadProgress(false);
                            groupsList.animate()
                                    .setDuration(200)
                                    .alpha(0)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            groupsList.setVisibility(View.INVISIBLE);
                                        }
                                    });

                            noGroup.animate()
                                    .setDuration(200)
                                    .alpha(1)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            noGroup.setVisibility(View.VISIBLE);
                                        }
                                    });
                            return;
                        }

                        for (QueryDocumentSnapshot groupSnap : result) {
                            addGroupItem(groupSnap);
                        }
                        showLoadProgress(false);
                    }
                });
    }

    private void addGroupItem(DocumentSnapshot groupSnap) {
        final GroupItemAdapter.GroupItem groupItem = new GroupItemAdapter.GroupItem(groupSnap.getString(Group.NAME),
                (ArrayList<DocumentReference>) groupSnap.get(Group.USERS),
                (ArrayList<DocumentReference>) groupSnap.get(Group.TASKS),
                (ArrayList<DocumentReference>) groupSnap.get(Group.ACTIVITIES),
                (ArrayList<DocumentReference>) groupSnap.get(Group.MESSAGES),
                groupSnap.getReference());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupItemAdapter.addItem(groupItem);
            }
        });
    }

    public void showLoadProgress(final boolean show) {
        isLoading = show;

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        groupsList.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        groupsList.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        progressBar.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }

    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        addButton.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        addButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        groupsWrapper.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        groupsWrapper.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        navbar.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        navbar.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        navbarLine.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        navbarLine.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        progressBar.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progressBar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }
}
