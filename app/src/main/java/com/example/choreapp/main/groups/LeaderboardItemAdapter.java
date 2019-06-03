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
import java.util.Collections;
import java.util.Comparator;

public class LeaderboardItemAdapter extends RecyclerView.Adapter<LeaderboardItemAdapter.LeaderboardViewHolder> {

    public ArrayList<LeaderboardItem> mDataset;
    private Context context;
    private TextView noLeaderboardItem;
    private View leaderboardWrapper;
    private boolean visible = false;

    public static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public LeaderboardViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public LeaderboardItemAdapter(ArrayList<LeaderboardItem> mDataset, Context context, TextView noLeaderboardItem, View leaderboardWrapper) {
        this.mDataset = mDataset;
        this.context = context;
        this.noLeaderboardItem = noLeaderboardItem;
        this.leaderboardWrapper = leaderboardWrapper;
    }

    public void addItem(LeaderboardItem item) {
        mDataset.add(item);
        Collections.sort(mDataset, new Comparator<LeaderboardItem>() {
            @Override
            public int compare(LeaderboardItem o1, LeaderboardItem o2) {
                return (int) (o2.totalPoints - o1.totalPoints);
            }
        });

        this.notifyDataSetChanged();
    }

    public void updateScore(CompletedTaskItemAdapter.CompletedTaskItem completedTaskItem) {
        LeaderboardItem existingItem = null;

        for (LeaderboardItem leaderboardItem : mDataset) {
            if (leaderboardItem.userId.equals(completedTaskItem.userId)) {
                existingItem = leaderboardItem;
                break;
            }
        }

        if (existingItem == null) {
            return;
        }

        existingItem.totalPoints += completedTaskItem.taskPoints;
        Collections.sort(mDataset, new Comparator<LeaderboardItem>() {
            @Override
            public int compare(LeaderboardItem o1, LeaderboardItem o2) {
                return (int) (o2.totalPoints - o1.totalPoints);
            }
        });
        this.notifyDataSetChanged();
    }

    @Override
    public LeaderboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_list_item, parent, false);

        return new LeaderboardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LeaderboardViewHolder holder, int position) {
        LeaderboardItem item = this.mDataset.get(position);

        View leaderboard_user_color = holder.view.findViewById(R.id.leaderboard_user_color);
        TextView leaderboard_user_name = holder.view.findViewById(R.id.leaderboard_user_name);
        TextView leaderboard_points = holder.view.findViewById(R.id.leaderboard_points);

        Drawable colorCircle = context.getResources().getDrawable(defs.getColorInt(item.userColor));
        leaderboard_user_color.setBackground(colorCircle);
        leaderboard_user_color.setVisibility(View.VISIBLE);

        String totalPoints = item.totalPoints + " PTS";
        leaderboard_user_name.setText(item.userName);
        leaderboard_points.setText(totalPoints);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class LeaderboardItem {
        String userColor;
        String userName;
        String userId;
        long totalPoints;

        public LeaderboardItem(String userColor, String userName, String userId, long totalPoints) {
            this.userColor = userColor;
            this.userName = userName;
            this.userId = userId;
            this.totalPoints = totalPoints;
        }
    }
}
