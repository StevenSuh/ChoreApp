package com.example.choreapp.main.tasks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.Task;
import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class TasksActivity extends AppCompatActivity {

    private boolean isLoading = false;

    private RecyclerView listView;
    private TaskItemAdapter taskItemAdapter;

    private RecyclerView userView;
    private TaskUserAdapter taskUserAdapter;

    private LinearLayout tasksWrapper;
    private ProgressBar progressBar;

    private static int TASK_ADD = 1;
    public static int TASK_EDIT = 0;
    public static int RESULT_DELETE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        View groupButton = findViewById(R.id.group_button);
        View tasksButton = findViewById(R.id.tasks_button);
        View messagesButton = findViewById(R.id.messages_button);
        View accountButton = findViewById(R.id.account_button);
        Utils.initNavbar(groupButton, tasksButton, messagesButton, accountButton, defs.TASKS_PAGE, this);

        ImageView addButton = findViewById(R.id.add_button);
        Utils.setTouchEffect(addButton, true, false, true);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataHolder.getInstance().setTask(null);
                Intent intent = new Intent(TasksActivity.this, TaskDetailsActivity.class);
                startActivityForResult(intent, TASK_ADD);
            }
        });

        listView = findViewById(R.id.tasks_list);
        taskItemAdapter = new TaskItemAdapter(new ArrayList<TaskItemAdapter.TaskItem>(), TasksActivity.this);
        listView.setAdapter(taskItemAdapter);

        userView = findViewById(R.id.tasks_users);
        taskUserAdapter = new TaskUserAdapter(new ArrayList<TaskUserAdapter.TaskUser>(), TasksActivity.this, taskItemAdapter);
        userView.setAdapter(taskUserAdapter);

        tasksWrapper = findViewById(R.id.tasks_wrapper);
        progressBar = findViewById(R.id.progress);

        loadUsersAndTasks();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {
            return;
        }

        TaskItemAdapter.TaskItem taskItem = DataHolder.getInstance().getTask();

        if (requestCode == TASK_EDIT) {
            if (resultCode == RESULT_DELETE) {
                taskItemAdapter.deleteItem(taskItem);
            } else {
                taskItemAdapter.replaceItem(taskItem);
            }
        }
        if (requestCode == TASK_ADD) {
            taskItemAdapter.addItem(taskItem);
        }
    }

    public void loadUsersAndTasks() {
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

        showProgress(true);
        loadUsers();
        loadTasks();
    }

    private void loadUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentSnapshot group = DataHolder.getInstance().getGroup();
        final ArrayList<DocumentReference> users = (ArrayList<DocumentReference>) group.get(Group.USERS);

        final ArrayList<TaskUserAdapter.TaskUser> usersSnapshots = new ArrayList<>();

        if (users == null) {
            Toast.makeText(TasksActivity.this, "Server error", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(User.COLLECTION)
            .whereArrayContains(User.GROUPS, group.getReference())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                    showProgress(false);

                    if (!task.isSuccessful()) {
                        Toast.makeText(TasksActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot user : task.getResult()) {
                        TaskUserAdapter.TaskUser userItem = new TaskUserAdapter.TaskUser(user.getId(),
                                user.getString(User.COLOR),
                                user.getString(User.NAME),
                                user.getReference());
                        usersSnapshots.add(userItem);
                    }

                    DataHolder.getInstance().setUsersInGroup((ArrayList<TaskUserAdapter.TaskUser>) usersSnapshots.clone());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usersSnapshots.add(0, new TaskUserAdapter.TaskUser(defs.TASK_FILTER_ALL,
                                "#000000",
                                defs.TASK_FILTER_ALL,
                                null));
                            taskUserAdapter.replaceDataset(usersSnapshots);
                        }
                    });
                }
            });
    }

    private void loadTasks() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentSnapshot group = DataHolder.getInstance().getGroup();
        final ArrayList<DocumentReference> tasks = (ArrayList<DocumentReference>) group.get(Group.TASKS);

        if (tasks == null) {
            Toast.makeText(TasksActivity.this, "Server error", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(Task.COLLECTION)
            .whereEqualTo(Task.GROUP, group.getReference())
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull final com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                    showProgress(false);

                    if (!task.isSuccessful()) {
                        Toast.makeText(TasksActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    for (QueryDocumentSnapshot taskSnap : task.getResult()) {
                        DocumentReference assignedUserRef = taskSnap.getDocumentReference(Task.ASSIGNED_USER);
                        loadTaskItem(taskSnap, assignedUserRef);
                    }
                }
            });
    }

    private void loadTaskItem(final QueryDocumentSnapshot taskSnap, final DocumentReference assignedUserRef) {
        if (assignedUserRef == null) {
            addTaskItem(null, null, null, taskSnap, null);
            return;
        }

        assignedUserRef.get()
            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Toast.makeText(TasksActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    DocumentSnapshot assignedUser = task.getResult();
                    String userColor = assignedUser.getString(User.COLOR);
                    String userId = assignedUser.getId();
                    String userName = assignedUser.getString(User.NAME);

                    addTaskItem(userColor, userId, userName, taskSnap, assignedUserRef);
                }
            });
    }

    private void addTaskItem(String userColor,
                             String userId,
                             String userName,
                             DocumentSnapshot taskSnap,
                             DocumentReference assignedUserRef) {
        final TaskItemAdapter.TaskItem taskItem = new TaskItemAdapter.TaskItem(userColor,
                userId,
                userName,
                taskSnap.getString(Task.NAME),
                taskSnap.getLong(Task.POINTS),
                assignedUserRef,
                taskSnap.getString(Task.REASSIGN_INTERVAL),
                (boolean) taskSnap.get(Task.IS_DONE),
                taskSnap.getDocumentReference(Task.GROUP),
                taskSnap.getDocumentReference(Task.ACTIVITY),
                taskSnap.getReference());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                taskItemAdapter.addItem(taskItem);
            }
        });
    }

    public void showProgress(final boolean show) {
        isLoading = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        tasksWrapper.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        tasksWrapper.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
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
