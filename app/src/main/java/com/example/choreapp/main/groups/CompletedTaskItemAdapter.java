package com.example.choreapp.main.groups;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.choreapp.R;
import com.example.choreapp.defs;

import java.util.ArrayList;
import java.util.Date;

public class CompletedTaskItemAdapter extends RecyclerView.Adapter<CompletedTaskItemAdapter.CompletedTaskViewHolder> {

    public ArrayList<CompletedTaskItem> mDataset;
    private Context context;
    private TextView noCompletedTasks;
    private View completedTasksWrapper;
    private boolean visible = false;

    public static class CompletedTaskViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public CompletedTaskViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public CompletedTaskItemAdapter(ArrayList<CompletedTaskItem> mDataset, Context context, TextView noCompletedTasks, View completedTasksWrapper) {
        this.mDataset = mDataset;
        this.context = context;
        this.noCompletedTasks = noCompletedTasks;
        this.completedTasksWrapper = completedTasksWrapper;
    }

    public void addItem(CompletedTaskItem item) {
        int index = 0;

        for (CompletedTaskItem completedTaskItem : mDataset) {
            if (item.taskUpdated.after(completedTaskItem.taskUpdated)) {
                break;
            }
            index++;
        }

        mDataset.add(index, item);
        this.notifyDataSetChanged();
    }

    @Override
    public CompletedTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.completed_task_list_item, parent, false);

        return new CompletedTaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CompletedTaskViewHolder holder, int position) {
        CompletedTaskItem item = this.mDataset.get(position);

        View completed_user_color = holder.view.findViewById(R.id.completed_user_color);
        TextView completed_user_name = holder.view.findViewById(R.id.completed_user_name);
        TextView completed_task_name = holder.view.findViewById(R.id.completed_task_name);
        TextView completed_task_points = holder.view.findViewById(R.id.completed_task_points);

        Drawable colorCircle = context.getResources().getDrawable(defs.getColorInt(item.userColor));
        completed_user_color.setBackground(colorCircle);
        completed_user_color.setVisibility(View.VISIBLE);

        String taskPoints = item.taskPoints + " PTS";
        completed_user_name.setText(item.userName);
        completed_task_name.setText(item.taskName);
        completed_task_points.setText(taskPoints);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class CompletedTaskItem {
        String userColor;
        String userName;
        String userId;
        String taskName;
        Date taskUpdated;
        long taskPoints;

        public CompletedTaskItem(String userColor, String userName, String userId, String taskName, Date taskUpdated, long taskPoints) {
            this.userColor = userColor;
            this.userName = userName;
            this.userId = userId;
            this.taskName = taskName;
            this.taskUpdated = taskUpdated;
            this.taskPoints = taskPoints;
        }
    }
}
