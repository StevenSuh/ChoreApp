package com.example.choreapp.main.tasks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.models.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TaskDetailsActivity extends AppCompatActivity {

    private boolean isSaving = false;

    private EditText taskNameView;
    private EditText taskPointsView;
    private Spinner assignToView;
    private Spinner reassignView;
    private ImageView isDoneView;

    private int assignToIndex = 0;
    private int reassignIndex = 0;
    private boolean isDone = false;

    private TextView saveTextView;
    private ProgressBar saveProgressView;

    private TextView deleteTextView;
    private ProgressBar deleteProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        DataHolder dataHolder = DataHolder.getInstance();
        if (!dataHolder.hasUsersInGroup()) {
            Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_CANCELED);
            finish();
            return;
        }

        ImageView exitButton = findViewById(R.id.exit_button);
        Utils.setTouchEffect(exitButton, true, false, true);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        taskNameView = findViewById(R.id.task_detail_name);
        taskPointsView = findViewById(R.id.task_detail_points);
        assignToView = findViewById(R.id.task_detail_assign_to);
        reassignView = findViewById(R.id.task_detail_reassign);
        isDoneView = findViewById(R.id.task_detail_is_done);

        assignToView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                assignToIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        reassignView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reassignIndex = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayList<String> usersInGroup = new ArrayList<>();
        for (TaskUserAdapter.TaskUser user : DataHolder.getInstance().getUsersInGroup()) {
            usersInGroup.add(user.name);
        }

        ArrayAdapter<String> assignToAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, usersInGroup);
        assignToAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignToView.setAdapter(assignToAdapter);
        assignToView.setSelection(0);

        ArrayAdapter<String> reassignAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, defs.REASSIGN_INTERVAL);
        reassignAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reassignView.setAdapter(reassignAdapter);
        reassignView.setSelection(0);

        Utils.setTouchEffect(isDoneView, false, true, true);
        isDoneView.setAlpha(defs.EXTRA_LOW_OPACITY);
        isDoneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDone = !isDone;
                v.setAlpha(isDone ? 1f : defs.EXTRA_LOW_OPACITY);
                Utils.setTouchEffect(v, isDone, true, true);
            }
        });

        RelativeLayout button = findViewById(R.id.next_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeAddOrModifyTask();
            }
        });

        saveTextView = findViewById(R.id.save_text);
        saveProgressView = findViewById(R.id.save_progress);

        if (dataHolder.hasTask()) {
            final TaskItemAdapter.TaskItem taskItem = dataHolder.getTask();

            TextView taskDetailText = findViewById(R.id.task_detail_text);
            taskDetailText.setText("Edit Task");

            taskNameView.setText(taskItem.name);
            taskPointsView.setText(String.valueOf(taskItem.points));

            isDone = taskItem.is_done;
            Utils.setTouchEffect(isDoneView, taskItem.is_done, true, true);
            isDoneView.setAlpha(taskItem.is_done ? 1f : defs.EXTRA_LOW_OPACITY);

            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) button.getLayoutParams();
            layoutParams.setMargins(40, 0, 40, 0);
            button.setLayoutParams(layoutParams);

            RelativeLayout deleteButton = findViewById(R.id.delete_button);
            deleteButton.setVisibility(View.VISIBLE);
            Utils.setTouchEffect(deleteButton, true, false, true);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    taskItem.taskRef.delete();
                    setResult(TasksActivity.RESULT_DELETE);
                    onSuccess();
                }
            });
        }
    }

    public void executeAddOrModifyTask() {
        if (isSaving) {
            return;
        }

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
            return;
        }

        taskNameView.setError(null);
        taskPointsView.setError(null);

        String nameStr = taskNameView.getText().toString();
        String pointsStr = taskPointsView.getText().toString();

        if (TextUtils.isEmpty(nameStr)) {
            taskNameView.setError("This field is required");
            taskNameView.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pointsStr)) {
            taskPointsView.setError("This field is required");
            taskPointsView.requestFocus();
            return;
        }

        if (!Utils.canParseLong(pointsStr)) {
            taskPointsView.setError("This field is invalid");
            taskPointsView.requestFocus();
            return;
        }

        showProgress(true);

        final Map<String, Object> taskMap = new HashMap<>();

        final String name = nameStr;
        final long points = Long.parseLong(pointsStr);
        final TaskUserAdapter.TaskUser assignedUser = DataHolder.getInstance()
                .getUsersInGroup()
                .get(assignToIndex);
        final String reassignInterval = defs.REASSIGN_INTERVAL[reassignIndex];
        final boolean isDone = this.isDone;
        final DocumentReference groupRef = DataHolder.getInstance().getGroup().groupRef;
        final Date created = new Date();
        final Date updated = new Date();

        taskMap.put(Task.NAME, name);
        taskMap.put(Task.POINTS, points);
        taskMap.put(Task.ASSIGNED_USER, assignedUser.userRef);
        taskMap.put(Task.REASSIGN_INTERVAL, reassignInterval);
        taskMap.put(Task.IS_DONE, isDone);
        taskMap.put(Task.GROUP, groupRef);
        taskMap.put(Task.ACTIVITY, null);
        taskMap.put(Task.CREATED, created);
        taskMap.put(Task.UPDATED, updated);

        if (DataHolder.getInstance().hasTask()) {
            final TaskItemAdapter.TaskItem taskItem = DataHolder.getInstance().getTask();

            taskMap.put(Task.ACTIVITY, taskItem.activity);
            taskMap.put(Task.CREATED, taskItem.created);

            if (reassignInterval.equals(taskItem.reassign_interval)) {
                taskMap.put(Task.UPDATED, taskItem.updated);
            }

            taskItem.taskRef.update(taskMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<Void> task) {
                    if (!task.isSuccessful()) {
                        onFailure();
                        return;
                    }
                    TaskItemAdapter.TaskItem newTask = new TaskItemAdapter.TaskItem(assignedUser.color,
                            assignedUser.id,
                            assignedUser.name,
                            name,
                            points,
                            assignedUser.userRef,
                            reassignInterval,
                            isDone,
                            groupRef,
                            taskItem.activity,
                            taskItem.taskRef,
                            taskItem.created,
                            (Date) taskMap.get(Task.UPDATED));
                    DataHolder.getInstance().setTask(newTask);

                    setResult(Activity.RESULT_OK);
                    onSuccess();
                }
            });
        } else {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection(Task.COLLECTION)
                .add(taskMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            onFailure();
                            return;
                        }
                        TaskItemAdapter.TaskItem newTask = new TaskItemAdapter.TaskItem(assignedUser.color,
                                assignedUser.id,
                                assignedUser.name,
                                name,
                                points,
                                assignedUser.userRef,
                                reassignInterval,
                                isDone,
                                groupRef,
                                null,
                                task.getResult(),
                                created,
                                updated);
                        DataHolder.getInstance().setTask(newTask);

                        setResult(Activity.RESULT_OK);
                        onSuccess();
                    }
                });
        }
    }

    public void onSuccess() {
        showProgress(false);
        Toast.makeText(TaskDetailsActivity.this, "Success", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void onFailure() {
        showProgress(false);
        Toast.makeText(TaskDetailsActivity.this, "Server error", Toast.LENGTH_SHORT).show();
    }

    public void showProgress(final boolean show) {
        isSaving = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        saveTextView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        saveTextView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        saveProgressView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        saveProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }
}
