package com.example.choreapp.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.choreapp.R;
import com.example.choreapp.defs;
import com.example.choreapp.signup.LoginActivity;

public class GroupsActivity extends AppCompatActivity {

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
    }
}
