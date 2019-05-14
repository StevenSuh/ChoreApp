package com.example.choreapp.main.tasks;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.models.Activity;
import com.example.choreapp.models.Task;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;

public class TaskItemAdapter extends RecyclerView.Adapter<TaskItemAdapter.TaskViewHolder> {

    private ArrayList<TaskItem> mDataset;
    private ArrayList<TaskItem> mOrigDataset;
    private Context context;

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public TaskViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public TaskItemAdapter(ArrayList<TaskItem> mDataset, Context context) {
        this.mDataset = mDataset;
        this.mOrigDataset = (ArrayList<TaskItem>) mDataset.clone();
        this.context = context;
    }

    public void addItem(TaskItem item) {
        mDataset.add(item);
        mOrigDataset.add(item);
        this.notifyDataSetChanged();
    }

    public void replaceItem(TaskItem item) {
        int currIndex = 0;
        int origIndex = 0;

        for (TaskItem taskItem : mDataset) {
            if (taskItem.taskRef.getId().equals(item.taskRef.getId())) {
                break;
            }
            currIndex++;
        }

        for (TaskItem taskItem : mOrigDataset) {
            if (taskItem.taskRef.getId().equals(item.taskRef.getId())) {
                break;
            }
            origIndex++;
        }

        mDataset.set(currIndex, item);
        mOrigDataset.set(origIndex, item);
        this.notifyDataSetChanged();
    }

    public void deleteItem(TaskItem item) {
        int currIndex = 0;
        int origIndex = 0;

        for (TaskItem taskItem : mDataset) {
            if (taskItem.taskRef.getId().equals(item.taskRef.getId())) {
                break;
            }
            currIndex++;
        }

        for (TaskItem taskItem : mOrigDataset) {
            if (taskItem.taskRef.getId().equals(item.taskRef.getId())) {
                break;
            }
            origIndex++;
        }

        mDataset.remove(currIndex);
        mOrigDataset.remove(origIndex);
        this.notifyDataSetChanged();
    }

    public void replaceDataset(ArrayList<TaskItem> mDataset) {
        this.mDataset = mDataset;
        this.mOrigDataset = (ArrayList<TaskItem>) mDataset.clone();
        this.notifyDataSetChanged();
    }

    public void onFilterDataset(String userId) {
        this.mDataset = new ArrayList<>();

        for (TaskItem task : this.mOrigDataset) {
            if (defs.TASK_FILTER_ALL.equals(userId) || userId.equals(task.userId)) {
                this.mDataset.add(task);
            }
        }
        this.notifyDataSetChanged();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_item, parent, false);

        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        final TaskItem task = this.mDataset.get(position);

        final ImageView checkIcon = holder.view.findViewById(R.id.task_done);
        final ProgressBar checkProgress = holder.view.findViewById(R.id.task_done_progress);
        View userColor = holder.view.findViewById(R.id.task_user_color);
        TextView taskNameView = holder.view.findViewById(R.id.task_name);
        ImageView taskArrow = holder.view.findViewById(R.id.task_arrow);

        checkIcon.setAlpha(task.is_done ? 1f : defs.EXTRA_LOW_OPACITY);
        taskNameView.setText(task.name);

        if (!TextUtils.isEmpty(task.color)) {
            Drawable colorCircle = context.getResources().getDrawable(defs.getColorInt(task.color));
            userColor.setBackground(colorCircle);
            userColor.setVisibility(View.VISIBLE);
        } else {
            userColor.setVisibility(View.GONE);
        }

        Utils.setTouchEffect(checkIcon, task.is_done, true, true);
        checkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCheckIcon(task, checkIcon, checkProgress);
            }
        });

        Utils.setTouchEffect(taskArrow, true, false, true);
        taskArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataHolder.getInstance().setTask(task);
                Intent intent = new Intent(context, TaskDetailsActivity.class);
                ((TasksActivity) context).startActivityForResult(intent, TasksActivity.TASK_EDIT);
            }
        });
    }

    private void onClickCheckIcon(final TaskItem task, final View checkIcon, final View checkProgress) {
        if (task.isUpdating) {
            return;
        }

        if (task.is_done) {
            if (task.activity != null) {
                task.activity.delete();
            }
            task.taskRef.update(Task.IS_DONE, task.is_done);

            task.is_done = false;
            checkIcon.setAlpha(defs.EXTRA_LOW_OPACITY);
            return;
        }

        showProgress(true, checkIcon, checkProgress);
        task.isUpdating = true;

        Activity activity = new Activity(task.name, task.assigned_user, task.points, new Date(), task.group);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Activity.COLLECTION)
            .add(activity.toMap())
            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentReference> job) {
                    onSaveActivityFromTask(checkIcon, checkProgress, job, task);
                }
            });
    }

    private void onSaveActivityFromTask(View checkIcon,
                                        View checkProgress,
                                        com.google.android.gms.tasks.Task<DocumentReference> job,
                                        TaskItem task) {
        showProgress(false, checkIcon, checkProgress);
        task.isUpdating = false;

        if (!job.isSuccessful()) {
            Toast.makeText(context, "Server error", Toast.LENGTH_SHORT).show();
            return;
        }

        task.is_done = true;
        checkIcon.setAlpha(1f);

        task.taskRef.update(Task.IS_DONE, task.is_done,
                Task.ACTIVITY, job.getResult());
        task.activity = job.getResult();
    }

    public void showProgress(final boolean show, final View main, final View progress) {
        int shortAnimTime = 200;

        main.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        main.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });

        progress.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        progress.setVisibility(show ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class TaskItem extends Task {
        String color;
        String userId;
        String userName;
        DocumentReference taskRef;

        public boolean isUpdating = false;

        public TaskItem(String color,
                        String userId,
                        String userName,
                        String name,
                        long points,
                        DocumentReference assigned_user,
                        String reassign_interval,
                        boolean is_done,
                        DocumentReference group,
                        DocumentReference activity,
                        DocumentReference taskRef,
                        Date created,
                        Date updated) {
            super(name, points, assigned_user, reassign_interval, is_done, group, activity, created, updated);

            this.color = color;
            this.userName = userName;
            this.userId = userId;
            this.taskRef = taskRef;
        }
    }
}
