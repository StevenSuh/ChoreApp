package com.example.choreapp.main.tasks;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.models.Task;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

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

        ImageView checkIcon = holder.view.findViewById(R.id.task_done);
        View userColor = holder.view.findViewById(R.id.task_user_color);
        TextView taskNameView = holder.view.findViewById(R.id.task_name);
        LinearLayout taskArrow = holder.view.findViewById(R.id.task_arrow);

        checkIcon.setAlpha(task.is_done ? 1f : defs.EXTRA_LOW_OPACITY);
        taskNameView.setText(task.name);

        if (!TextUtils.isEmpty(task.color)) {
            Drawable colorCircle = context.getResources().getDrawable(defs.getColorInt(task.color));
            userColor.setBackground(colorCircle);
            userColor.setVisibility(View.VISIBLE);
        } else {
            userColor.setVisibility(View.GONE);
        }

        Utils.setTouchEffect(checkIcon, task.is_done, true);
        checkIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.is_done = !task.is_done;
                
                v.setAlpha(task.is_done ? 1f : defs.EXTRA_LOW_OPACITY);
                task.taskRef.update(Task.IS_DONE, task.is_done);
            }
        });

        Utils.setTouchEffect(taskArrow, true, false);
        taskArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TaskDetailsActivity.class);
                context.startActivity(intent);
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
        DocumentReference taskRef;

        public TaskItem(String color,
                        String userId,
                        String name,
                        long points,
                        DocumentReference assigned_user,
                        String reassign_interval,
                        boolean is_done,
                        DocumentReference group,
                        DocumentReference taskRef) {
            super(name, points, assigned_user, reassign_interval, is_done, group);

            this.color = color;
            this.userId = userId;
            this.taskRef = taskRef;
        }
    }
}
