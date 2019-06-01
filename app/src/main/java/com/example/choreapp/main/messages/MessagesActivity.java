package com.example.choreapp.main.messages;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.models.Group;
import com.example.choreapp.models.Message;
import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

public class MessagesActivity extends AppCompatActivity {

    private boolean isLoading = false;
    private boolean isSending = false;

    private boolean isAnonymous = false;

    private RecyclerView listView;
    private MessageItemAdapter messageItemAdapter;
    private TextView noMessage;

    private View isAnonToggle;

    private EditText sendText;
    private View sendBtn;
    private TextView sendBtnText;
    private ProgressBar sendBtnProgress;

    private LinearLayout messagesWrapper;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        SharedPreferences prefs = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE);
        isAnonymous = prefs.getBoolean(defs.IS_ANONYMOUS, false);

        View groupButton = findViewById(R.id.group_button);
        View tasksButton = findViewById(R.id.tasks_button);
        View messagesButton = findViewById(R.id.messages_button);
        View accountButton = findViewById(R.id.account_button);
        Utils.initNavbar(groupButton, tasksButton, messagesButton, accountButton, defs.MESSAGES_PAGE, this);

        listView = findViewById(R.id.messages_list);
        messageItemAdapter = new MessageItemAdapter(new ArrayList<MessageItemAdapter.MessageItem>(), this);
        listView.setDrawingCacheEnabled(true);
        listView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        listView.setAdapter(messageItemAdapter);
        noMessage = findViewById(R.id.no_message);

        sendText = findViewById(R.id.message_send_text);
        sendText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    sendMessage();
                    return true;
                }
                return false;
            }
        });

        sendBtnText = findViewById(R.id.message_send_btn_text);
        sendBtnProgress = findViewById(R.id.message_send_btn_progress);

        sendBtn = findViewById(R.id.message_send_btn);
        Utils.setTouchEffect(sendBtn, true, false, true);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        isAnonToggle = findViewById(R.id.message_is_anonymous);
        isAnonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleIsAnon();
            }
        });

        messagesWrapper = findViewById(R.id.messages_wrapper);
        progressBar = findViewById(R.id.progress);

        MessagingService.setIsOnMessagePage(true, messageItemAdapter);
        loadMessages();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MessagingService.setIsOnMessagePage(false, null);
    }

    private void toggleIsAnon() {
        isAnonymous = !isAnonymous;

        SharedPreferences prefs = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE);
        prefs.edit().putBoolean(defs.IS_ANONYMOUS, isAnonymous).apply();

        isAnonToggle.setAlpha(isAnonymous ? 1f : 0.25f);
    }

    private void loadMessages() {
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

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Group group = DataHolder.getInstance().getGroup();

        if (group.messages == null) {
            Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection(Message.COLLECTION)
                .whereEqualTo(Message.GROUP, group.groupRef)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        showProgress(false);

                        if (!task.isSuccessful()) {
                            Toast.makeText(MessagesActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        QuerySnapshot result = task.getResult();
                        if (result == null || result.isEmpty()) {
                            listView.animate()
                                    .setDuration(200)
                                    .alpha(0)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            listView.setVisibility(View.INVISIBLE);
                                        }
                                    });

                            noMessage.animate()
                                    .setDuration(200)
                                    .alpha(1)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            noMessage.setVisibility(View.VISIBLE);
                                        }
                                    });
                            return;
                        }

                        for (QueryDocumentSnapshot messageSnap : task.getResult()) {
                            DocumentReference assignedUserRef = messageSnap.getDocumentReference(Message.USER);
                            loadMessageItem(messageSnap, assignedUserRef);
                        }
                    }
                });
    }

    private void loadMessageItem(final QueryDocumentSnapshot messageSnap, final DocumentReference assignedUserRef) {
        if (assignedUserRef == null) {
            return;
        }

        assignedUserRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MessagesActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentSnapshot assignedUser = task.getResult();
                        String userColor = assignedUser.getString(User.COLOR);
                        String userName = assignedUser.getString(User.NAME);

                        addMessageItem(userColor, userName, messageSnap, assignedUserRef);
                    }
                });
    }

    private void addMessageItem(String userColor,
                             String userName,
                             DocumentSnapshot messageSnap,
                             DocumentReference assignedUserRef) {
        final MessageItemAdapter.MessageItem messageItem = new MessageItemAdapter.MessageItem(messageSnap.getString(Message.CONTENT),
                (boolean) messageSnap.get(Message.IS_ANONYMOUS),
                userName,
                assignedUserRef,
                messageSnap.getDocumentReference(Message.GROUP),
                messageSnap.getDate(Message.DATE),
                userColor,
                messageSnap.getReference());

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageItemAdapter.addItem(messageItem);
                listView.scrollToPosition(messageItemAdapter.getItemCount() - 1);
            }
        });
    }

    private void sendMessage() {
        if (isSending) {
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

        final String content = sendText.getText().toString();

        if (TextUtils.isEmpty(content)) {
            return;
        }

        showSendProgress(true);

        final User user = DataHolder.getInstance().getUser();
        final Group group = DataHolder.getInstance().getGroup();
        final Date currDate = new Date();

        Message message = new Message(content,
                isAnonymous,
                user.name,
                user.userRef,
                group.groupRef,
                currDate);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Message.COLLECTION)
                .add(message.toMap())
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (!task.isSuccessful()) {
                            failSend();
                            return;
                        }

                        MessageItemAdapter.MessageItem newMessage = new MessageItemAdapter.MessageItem(content,
                                isAnonymous,
                                user.name,
                                user.userRef,
                                group.groupRef,
                                currDate,
                                user.color,
                                task.getResult());
                        successSend(newMessage);
                    }
                });
    }

    public void successSend(MessageItemAdapter.MessageItem messageItem) {
        showSendProgress(false);
        messageItemAdapter.addItem(messageItem);
        sendText.setText("");
        listView.scrollToPosition(messageItemAdapter.getItemCount() - 1);
    }

    public void failSend() {
        showSendProgress(false);
        Toast.makeText(this, "Server error", Toast.LENGTH_SHORT).show();
    }

    public void showProgress(final boolean show) {
        isLoading = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        messagesWrapper.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        messagesWrapper.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
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

    public void showSendProgress(final boolean show) {
        isSending = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        sendBtnText.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        sendBtnText.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        sendBtnProgress.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        sendBtnProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }
}
