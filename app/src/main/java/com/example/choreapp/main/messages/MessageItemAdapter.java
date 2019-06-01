package com.example.choreapp.main.messages;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.defs;
import com.example.choreapp.models.Message;
import com.example.choreapp.models.User;
import com.google.firebase.firestore.DocumentReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageItemAdapter extends RecyclerView.Adapter<MessageItemAdapter.MessageViewHolder> {

    private ArrayList<MessageItem> mDataset;
    private Context context;
    private View currTimestamp = null;

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public MessageViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public MessageItemAdapter(ArrayList<MessageItem> mDataset, Context context) {
        this.mDataset = mDataset;
        this.context = context;
    }

    public void addItem(MessageItem item) {
        int index = 0;

        for (MessageItem messageItem: mDataset) {
            if (messageItem.date.after(item.date)) {
                break;
            }
            index++;
        }

        mDataset.add(index, item);
        this.notifyDataSetChanged();
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_item, parent, false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        MessageItem message = this.mDataset.get(position);
        User currUser = DataHolder.getInstance().getUser();

        boolean isYou = message.user.getId().equals(currUser.userRef.getId());

        if (isYou) {
            LinearLayout view = (LinearLayout) holder.view;
            view.setGravity(Gravity.END);
        }

        final TextView textTimestamp = holder.view.findViewById(R.id.text_timestamp);
        View userColor = holder.view.findViewById(R.id.message_user_color);
        TextView userName = holder.view.findViewById(R.id.message_user_name);
        TextView content = holder.view.findViewById(R.id.message_content);

        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.US);
        textTimestamp.setText(format.format(message.date));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currTimestamp != null) {
                    currTimestamp.setVisibility(View.GONE);

                    if (currTimestamp.equals(textTimestamp)) {
                        currTimestamp = null;
                        return;
                    }
                }
                textTimestamp.setVisibility(View.VISIBLE);
                currTimestamp = textTimestamp;
            }
        });

        if (!isYou && message.is_anonymous) {
            userColor.setVisibility(View.GONE);
            userName.setText("Anonymous");
        } else {
            Drawable colorCircle = context.getResources().getDrawable(defs.getColorInt(message.userColor));
            userColor.setBackground(colorCircle);
            userName.setText(isYou ? "You" : message.userName);
        }

        content.setText(message.content);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static class MessageItem extends Message {
        String userColor;
        String userName;
        DocumentReference messageRef;

        public MessageItem(String content,
                           boolean is_anonymous,
                           String userName,
                           DocumentReference user,
                           DocumentReference group,
                           Date date,
                           String userColor,
                           DocumentReference messageRef) {
            super(content, is_anonymous, userName, user, group, date);

            this.userColor = userColor;
            this.userName = userName;
            this.messageRef= messageRef;
        }
    }
}
