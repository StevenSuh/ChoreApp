package com.example.choreapp.main.messages;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.main.GroupsActivity;
import com.example.choreapp.models.Message;
import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;

import javax.annotation.Nullable;

public class MessagingService extends Service {

    private static boolean isOnMessagePage = false;
    private static MessageItemAdapter messageList = null;
    private static ListenerRegistration currListener = null;

    private static MessagingService instance = null;
    private static MessagingService getInstance() {
        return instance;
    }

    public static void setIsOnMessagePage(boolean value, MessageItemAdapter messageItemAdapter) {
        isOnMessagePage = value;
        messageList = messageItemAdapter;
    }

    public static void subscribeTo(DocumentReference groupRef) {
        if (currListener != null) {
            currListener.remove();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final Query query = db.collection(Message.COLLECTION).whereEqualTo(Message.GROUP, groupRef);
        currListener = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                if (queryDocumentSnapshots == null || queryDocumentSnapshots.getMetadata().hasPendingWrites()) {
                    return;
                }

                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    if (!dc.getType().equals(DocumentChange.Type.ADDED)) {
                        continue;
                    }

                    User user = DataHolder.getInstance().getUser();
                    QueryDocumentSnapshot newMessage = dc.getDocument();
                    String newMsgUserId = newMessage.getDocumentReference(Message.USER).getId();

                    if (isOnMessagePage) {
                        addMessageToList(newMessage);
                    } else if (!user.userRef.getId().equals(newMsgUserId)) {
                        String content = newMessage.getString(Message.CONTENT);
                        String userName = newMessage.getString(Message.USERNAME);

                        String messageBody = userName + ": " + content;
                        instance.sendNotification(messageBody);
                    }
                }
            }
        });
    }

    private static void addMessageToList(final QueryDocumentSnapshot snapshot) {
        if (messageList == null) {
            return;
        }

        DocumentReference userRef = snapshot.getDocumentReference(Message.USER);

        if (userRef == null) {
            return;
        }

        userRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(instance, "Server error", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DocumentSnapshot user = task.getResult();
                        String userColor = user.getString(User.COLOR);
                        String userName = user.getString(User.NAME);

                        String content = snapshot.getString(Message.CONTENT);
                        boolean isAnonymous = (boolean) snapshot.get(Message.IS_ANONYMOUS);
                        Date date = snapshot.getDate(Message.DATE);
                        DocumentReference groupRef = snapshot.getDocumentReference(Message.GROUP);

                        MessageItemAdapter.MessageItem message = new MessageItemAdapter.MessageItem(content,
                                isAnonymous,
                                userName,
                                user.getReference(),
                                groupRef,
                                date,
                                userColor,
                                snapshot.getReference());
                        messageList.addItem(message);
                    }
                });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        System.out.println("MessagingService started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (currListener != null) {
            currListener.remove();
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, GroupsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "Message";
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.app_logo_transp)
                        .setContentTitle("New Message")
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "New Message",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }
}
