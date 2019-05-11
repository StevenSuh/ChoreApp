package com.example.choreapp.main.tasks;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;

import java.util.ArrayList;

public class TaskUserAdapter extends RecyclerView.Adapter<TaskUserAdapter.TaskViewHolder> {

    private ArrayList<TaskUser> mDataset;
    private Context context;
    private TaskItemAdapter taskItemAdapter;

    private int selectedPos = 0;

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TaskViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public TaskUserAdapter(ArrayList<TaskUser> mDataset,
                           Context context,
                           TaskItemAdapter taskItemAdapter) {
        this.mDataset = mDataset;
        this.context = context;
        this.taskItemAdapter = taskItemAdapter;
    }

    public void addItem(TaskUser item) {
        mDataset.add(item);
        this.notifyItemInserted(mDataset.size() - 1);
    }

    public void replaceDataset(ArrayList<TaskUser> mDataset) {
        this.mDataset = mDataset;
        this.notifyDataSetChanged();
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_user_item, parent, false);

        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        final int currPosition = position;
        final TaskUser user = this.mDataset.get(currPosition);

        View userColorView = holder.view.findViewById(R.id.task_filter_user_color);
        TextView userNameView = holder.view.findViewById(R.id.task_filter_user_name);

        Drawable colorCircle = context.getResources().getDrawable(defs.getColorInt(user.color));
        userColorView.setBackground(colorCircle);
        userNameView.setText(user.name);

        if (position != selectedPos) {
            holder.view.setAlpha(defs.LOW_OPACITY);
            Utils.setTouchEffect(holder.view, false, false);
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPos = currPosition;
                    taskItemAdapter.onFilterDataset(user.id);
                    notifyDataSetChanged();
                }
            });
        } else {
            holder.view.setAlpha(1f);
            holder.view.setOnTouchListener(null);
            holder.view.setOnClickListener(null);
        }

        if (position == 0) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.view.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            holder.view.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class TaskUser {
        String id;
        String color;
        String name;

        public TaskUser(String id, String color, String name) {
            this.id = id;
            this.color = color;
            this.name = name;
        }
    }
}