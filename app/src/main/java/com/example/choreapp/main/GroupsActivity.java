package com.example.choreapp.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.signup.LoginActivity;

public class GroupsActivity extends AppCompatActivity {

    private View groupsWrapper;
    private View navbar;
    private View navbarLine;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        boolean isLoggedIn = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE).getBoolean(defs.IS_LOGGED_IN, false);

        if (!isLoggedIn) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        groupsWrapper = findViewById(R.id.groups_wrapper);
        navbar = findViewById(R.id.navbar);
        navbarLine = findViewById(R.id.navbar_line);
        progressBar = findViewById(R.id.progress);

        View groupButton = findViewById(R.id.group_button);
        View tasksButton = findViewById(R.id.tasks_button);
        View messagesButton = findViewById(R.id.messages_button);
        View accountButton = findViewById(R.id.account_button);

        Utils.initDataHolder(this, new Runnable() {
            @Override
            public void run() {
                showProgress(true);
            }
        }, new Runnable() {
            @Override
            public void run() {
                showProgress(false);
            }
        });
        Utils.initNavbar(groupButton, tasksButton, messagesButton, accountButton, defs.GROUP_PAGE, this);
    }

    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        groupsWrapper.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        groupsWrapper.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        navbar.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        navbar.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        navbarLine.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        navbarLine.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
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
