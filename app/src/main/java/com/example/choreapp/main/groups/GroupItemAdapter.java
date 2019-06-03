package com.example.choreapp.main.groups;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.User;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class GroupItemAdapter extends RecyclerView.Adapter<GroupItemAdapter.GroupViewHolder> {

    public ArrayList<GroupItem> mDataset;
    private Context context;

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public GroupViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public GroupItemAdapter(ArrayList<GroupItem> mDataset, Context context) {
        this.mDataset = mDataset;
        this.context = context;
    }

    public void addItem(GroupItem item) {
        mDataset.add(item);
        this.notifyDataSetChanged();
    }

    public void deleteItem(GroupItem item) {
        int index = -1;

        for (int i = 0; i < mDataset.size(); i++) {
            GroupItem groupItem = mDataset.get(i);
            if (groupItem.groupRef.getId().equals(item.groupRef.getId())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            mDataset.remove(index);
        }
        this.notifyDataSetChanged();
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_item, parent, false);

        return new GroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {
        final GroupItem group = this.mDataset.get(position);
        Group currGroup = DataHolder.getInstance().getGroup();

        Utils.setTouchEffect(holder.view, true, false, true);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataHolder.getInstance().setCurrentDetailGroup(group);
                Intent intent = new Intent(context, GroupDetailsActivity.class);
                ((GroupsActivity) context).startActivityForResult(intent, GroupsActivity.GROUP_DETAIL);
            }
        });

        ImageView groupSelected = holder.view.findViewById(R.id.group_selected);
        TextView groupName = holder.view.findViewById(R.id.group_name);
        TextView groupMember = holder.view.findViewById(R.id.group_member);

        if (group.groupRef.getId().equals(currGroup.groupRef.getId())) {
            groupSelected.setVisibility(View.VISIBLE);
        } else {
            groupSelected.setVisibility(View.INVISIBLE);
        }

        String members = group.users.size() + " Members";

        groupName.setText(group.name);
        groupMember.setText(members);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class GroupItem extends Group {
        DocumentReference groupRef;

        public GroupItem(String name,
                         ArrayList<DocumentReference> users,
                         ArrayList<DocumentReference> tasks,
                         ArrayList<DocumentReference> activities,
                         ArrayList<DocumentReference> messages,
                         DocumentReference groupRef) {
            super(name, users, tasks, activities, messages);

            this.groupRef= groupRef;
        }
    }
}
