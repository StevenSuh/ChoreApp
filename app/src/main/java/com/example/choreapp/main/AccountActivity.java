package com.example.choreapp.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.defs;
import com.example.choreapp.Utils;
import com.example.choreapp.models.User;
import com.example.choreapp.signup.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AccountActivity extends AppCompatActivity {

    private boolean isSaving = false;

    private boolean allowTasks;
    private boolean allowMessages;
    private int selectedColor = -1;
    private View selectedColorButton = null;
    private EditText nameView;

    private View saveTextView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        SharedPreferences prefs = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE);
        allowTasks = prefs.getBoolean(defs.NOTIFICATIONS_ALLOW_TASKS, true);
        allowMessages = prefs.getBoolean(defs.NOTIFICATIONS_ALLOW_MESSAGES, true);

        View groupButton = findViewById(R.id.group_button);
        View tasksButton = findViewById(R.id.tasks_button);
        View messagesButton = findViewById(R.id.messages_button);
        View accountButton = findViewById(R.id.account_button);
        Utils.initNavbar(groupButton, tasksButton, messagesButton, accountButton, defs.ACCOUNT_PAGE, this);

        User user = DataHolder.getInstance().getUser();
        nameView = findViewById(R.id.settings_name);
        nameView.setText(user.name);
        initColors();

        initNotifications();

        RelativeLayout saveButton = findViewById(R.id.save_button);
        Utils.setTouchEffect(saveButton, true, false, true);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        saveTextView = findViewById(R.id.save_text);
        progressBar = findViewById(R.id.save_progress);

        RelativeLayout signOutButton = findViewById(R.id.sign_out_button);
        Utils.setTouchEffect(signOutButton, true, false, true);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
    }

    private void signOut() {
        finish();
        Utils.nukePrefs(this);
    }

    private void updateProfile() {
        if (isSaving) {
            return;
        }

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        SharedPreferences prefs = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE);
        prefs.edit()
            .putBoolean(defs.NOTIFICATIONS_ALLOW_TASKS, allowTasks)
            .putBoolean(defs.NOTIFICATIONS_ALLOW_MESSAGES, allowMessages)
            .apply();

        final String name = nameView.getText().toString();

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(User.NAME, name);
        updateMap.put(User.COLOR, defs.USER_COLORS[selectedColor]);

        final User user = DataHolder.getInstance().getUser();
        user.userRef.update(updateMap)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    showProgress(false);

                    if (!task.isSuccessful()) {
                        Toast.makeText(AccountActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AccountActivity.this, "Success", Toast.LENGTH_SHORT).show();
                        user.name = name;
                        user.color = defs.USER_COLORS[selectedColor];
                    }
                }
            });
    }

    private void initColors() {
        View colorOne = findViewById(R.id.color_one);
        View colorTwo = findViewById(R.id.color_two);
        View colorThree = findViewById(R.id.color_three);
        View colorFour = findViewById(R.id.color_four);
        View colorFive = findViewById(R.id.color_five);
        View colorSix = findViewById(R.id.color_six);
        View[] colors = { colorOne, colorTwo, colorThree, colorFour, colorFive, colorSix };

        colorOne.setAlpha(defs.EXTRA_LOW_OPACITY);
        colorTwo.setAlpha(defs.EXTRA_LOW_OPACITY);
        colorThree.setAlpha(defs.EXTRA_LOW_OPACITY);
        colorFour.setAlpha(defs.EXTRA_LOW_OPACITY);
        colorFive.setAlpha(defs.EXTRA_LOW_OPACITY);
        colorSix.setAlpha(defs.EXTRA_LOW_OPACITY);

        Utils.setTouchEffect(colorOne, false, true, true);
        Utils.setTouchEffect(colorTwo, false, true, true);
        Utils.setTouchEffect(colorThree, false, true, true);
        Utils.setTouchEffect(colorFour, false, true, true);
        Utils.setTouchEffect(colorFive, false, true, true);
        Utils.setTouchEffect(colorSix, false, true, true);

        selectColorHandler(colorOne, 1);
        selectColorHandler(colorTwo, 2);
        selectColorHandler(colorThree, 3);
        selectColorHandler(colorFour, 4);
        selectColorHandler(colorFive, 5);
        selectColorHandler(colorSix, 6);

        User user = DataHolder.getInstance().getUser();
        selectedColor = Arrays.asList(defs.USER_COLORS)
                .indexOf(user.color);
        selectedColorButton = colors[selectedColor];
        selectedColorButton.setAlpha(1f);
        selectedColorButton.setOnTouchListener(null);
        selectedColorButton.setOnClickListener(null);
    }

    private void selectColorHandler(View view, final int colorIndex) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedColorButton != null) {
                    selectedColorButton.setAlpha(defs.EXTRA_LOW_OPACITY);
                    Utils.setTouchEffect(selectedColorButton, false, false, true);
                    selectColorHandler(selectedColorButton, selectedColor);
                }

                // array start at 0
                selectedColor = colorIndex - 1;
                selectedColorButton = v;
                v.setAlpha(1f);
                v.setOnTouchListener(null);
                v.setOnClickListener(null);
            }
        });
    }

    private void initNotifications() {
        View notifTasks = findViewById(R.id.notification_tasks);
        View notifMsgs = findViewById(R.id.notification_messages);

        notifTasks.setAlpha(allowTasks ? 1f : defs.EXTRA_LOW_OPACITY);
        notifMsgs.setAlpha(allowMessages ? 1f : defs.EXTRA_LOW_OPACITY);

        notifTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowTasks = !allowTasks;
                v.setAlpha(allowTasks ? 1f : defs.EXTRA_LOW_OPACITY);
            }
        });
        notifMsgs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowMessages = !allowMessages;
                v.setAlpha(allowMessages ? 1f : defs.EXTRA_LOW_OPACITY);
            }
        });
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
}
