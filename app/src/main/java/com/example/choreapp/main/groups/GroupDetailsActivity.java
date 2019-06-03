package com.example.choreapp.main.groups;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.Task;
import com.example.choreapp.models.User;
import com.example.choreapp.signup.InviteActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class GroupDetailsActivity extends AppCompatActivity {

    private boolean isLoading = false;
    private boolean isLeaving = false;

    private View group_detail_wrapper;
    private ProgressBar progress;

    private TextView group_detail_name;

    private TextView leave_text;
    private ProgressBar leave_progress;

    private RecyclerView leaderboard;
    private LeaderboardItemAdapter leaderboardItemAdapter;
    private RecyclerView completed_tasks;
    private CompletedTaskItemAdapter completedTaskItemAdapter;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        prefs = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE);
        GroupItemAdapter.GroupItem currDetailGroup = DataHolder.getInstance().getCurrentDetailGroup();

        View backButton = findViewById(R.id.back_button);
        Utils.setTouchEffect(backButton, true, false, true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        ImageView addButton = findViewById(R.id.add_button);
        Utils.setTouchEffect(addButton, true, false, true);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GroupItemAdapter.GroupItem group = DataHolder.getInstance().getCurrentDetailGroup();
                Intent intent = new Intent(GroupDetailsActivity.this, InviteActivity.class);
                intent.putExtra(defs.INVITE_CODE, group.groupRef.getId());
                startActivity(intent);
            }
        });

        group_detail_wrapper = findViewById(R.id.group_detail_wrapper);
        progress = findViewById(R.id.progress);
        group_detail_name = findViewById(R.id.group_detail_name);
        group_detail_name.setText(currDetailGroup.name);

        RelativeLayout set_as_default = findViewById(R.id.set_as_default);
        Utils.setTouchEffect(set_as_default, true, false, true);
        set_as_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference groupRef = DataHolder.getInstance().getCurrentDetailGroup().groupRef;
                DataHolder.getInstance().setGroup(groupRef, prefs);
                Toast.makeText(GroupDetailsActivity.this, "Set as default group", Toast.LENGTH_SHORT).show();
            }
        });

        RelativeLayout leave_group = findViewById(R.id.leave_group);
        Utils.setTouchEffect(leave_group, true, false, true);
        leave_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Group group = DataHolder.getInstance().getGroup();
                GroupItemAdapter.GroupItem groupItem = DataHolder.getInstance().getCurrentDetailGroup();

                if (group.groupRef.getId().equals(groupItem.groupRef.getId())) {
                    Toast.makeText(GroupDetailsActivity.this, "Cannot leave default group", Toast.LENGTH_SHORT).show();
                    return;
                }

                confirmLeaveGroup();
            }
        });

        leave_text = findViewById(R.id.leave_text);
        leave_progress = findViewById(R.id.leave_progress);

        leaderboard = findViewById(R.id.leaderboard);
        leaderboard.setDrawingCacheEnabled(true);
        leaderboard.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        leaderboardItemAdapter = new LeaderboardItemAdapter(new ArrayList<LeaderboardItemAdapter.LeaderboardItem>(),
                this,
                (TextView) findViewById(R.id.no_leaderboard),
                leaderboard);
        leaderboard.setAdapter(leaderboardItemAdapter);

        completed_tasks = findViewById(R.id.completed_tasks);
        completed_tasks.setDrawingCacheEnabled(true);
        completed_tasks.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        completedTaskItemAdapter = new CompletedTaskItemAdapter(new ArrayList<CompletedTaskItemAdapter.CompletedTaskItem>(),
                this,
                (TextView) findViewById(R.id.no_completed_tasks),
                completed_tasks);
        completed_tasks.setAdapter(completedTaskItemAdapter);

        View groupButton = findViewById(R.id.group_button);
        View tasksButton = findViewById(R.id.tasks_button);
        View messagesButton = findViewById(R.id.messages_button);
        View accountButton = findViewById(R.id.account_button);

        Utils.initNavbar(groupButton, tasksButton, messagesButton, accountButton, defs.GROUP_PAGE, this);
        loadGroupDetail();
    }

    private void loadGroupDetail() {
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        GroupItemAdapter.GroupItem group = DataHolder.getInstance().getCurrentDetailGroup();

        if (group.tasks == null || group.users == null) {
            Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        db.collection(User.COLLECTION)
                .whereArrayContains(User.GROUPS, group.groupRef)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(GroupDetailsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            showProgress(false);
                            return;
                        }

                        for (QueryDocumentSnapshot userSnap : task.getResult()) {
                            String userColor = userSnap.getString(User.COLOR);
                            String userId = userSnap.getId();
                            String userName = userSnap.getString(User.NAME);

                            final LeaderboardItemAdapter.LeaderboardItem leaderboardItem = new LeaderboardItemAdapter.LeaderboardItem(userColor,
                                    userName,
                                    userId,
                                    0);

                            leaderboardItemAdapter.addItem(leaderboardItem);
                            showProgress(false);
                        }
                    }
                });

        db.collection(Task.COLLECTION)
                .whereEqualTo(Task.GROUP, group.groupRef)
                .whereEqualTo(Task.IS_DONE, true)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(GroupDetailsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            showProgress(false);
                            return;
                        }

                        for (QueryDocumentSnapshot taskSnap : task.getResult()) {
                            loadCompletedTasks(taskSnap);
                        }
                    }
                });
    }

    private void loadCompletedTasks(final QueryDocumentSnapshot taskSnap) {
        DocumentReference user = taskSnap.getDocumentReference(Task.ASSIGNED_USER);

        if (user == null) {
            showProgress(false);
            return;
        }

        user.get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(GroupDetailsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        showProgress(false);
                        return;
                    }

                    DocumentSnapshot assignedUser = task.getResult();
                    String userColor = assignedUser.getString(User.COLOR);
                    String userId = assignedUser.getId();
                    String userName = assignedUser.getString(User.NAME);

                    String taskName = taskSnap.getString(Task.NAME);
                    long taskPoints = taskSnap.getLong(Task.POINTS);
                    Date taskUpdated = taskSnap.getDate(Task.UPDATED);

                    final CompletedTaskItemAdapter.CompletedTaskItem completedTaskItem = new CompletedTaskItemAdapter.CompletedTaskItem(userColor,
                            userName,
                            userId,
                            taskName,
                            taskUpdated,
                            taskPoints);

                    completedTaskItemAdapter.addItem(completedTaskItem);
                    leaderboardItemAdapter.updateScore(completedTaskItem);
                    showProgress(false);
                }
            });
    }

    private void confirmLeaveGroup() {
        if (isLeaving) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to leave this group?");

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                leaveGroup();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void leaveGroup() {
        if (isLeaving) {
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

        User user = DataHolder.getInstance().getUser();
        GroupItemAdapter.GroupItem currDetailGroup = DataHolder.getInstance().getCurrentDetailGroup();

        if (user.groups == null || !user.groups.contains(currDetailGroup.groupRef)) {
            Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
            return;
        }

        showLeaveProgress(true);

        user.groups.remove(currDetailGroup.groupRef);
        user.userRef
                .update(User.GROUPS, user.groups)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(GroupDetailsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            showLeaveProgress(false);
                            return;
                        }
                        removeUserFromGroup();
                    }
                });
    }

    private void removeUserFromGroup() {
        User user = DataHolder.getInstance().getUser();
        GroupItemAdapter.GroupItem currDetailGroup = DataHolder.getInstance().getCurrentDetailGroup();

        if (currDetailGroup.users == null) {
            return;
        }

        currDetailGroup.users.remove(user.userRef);

        if (currDetailGroup.users.isEmpty()) {
            currDetailGroup.groupRef.delete();

            showLeaveProgress(false);
            setResult(GroupsActivity.RESULT_DELETE);
            finish();
            return;
        }

        currDetailGroup.groupRef
                .update(Group.USERS, currDetailGroup.users)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(GroupDetailsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            showLeaveProgress(false);
                            return;
                        }
                        showLeaveProgress(false);
                        setResult(GroupsActivity.RESULT_DELETE);
                        finish();
                    }
                });
    }

    public void showLeaveProgress(final boolean show) {
        isLeaving = show;

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        leave_text.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        leave_text.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        leave_progress.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        leave_progress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }

    public void showProgress(final boolean show) {
        isLoading = show;

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        group_detail_wrapper.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        group_detail_wrapper.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        progress.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }
}
