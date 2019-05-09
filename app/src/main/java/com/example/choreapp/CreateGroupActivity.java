package com.example.choreapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameView;
    private EditText groupLinkView;

    private TextView nextTextView;
    private ProgressBar nextProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        LinearLayout backButton = findViewById(R.id.back);
        Utils.setTouchEffect(backButton, true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        groupNameView = findViewById(R.id.create_group_name);
        groupLinkView = findViewById(R.id.create_group_link);

        RelativeLayout nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeCreateGroup();
            }
        });

        nextTextView = findViewById(R.id.next_text);
        nextProgressView = findViewById(R.id.next_progress);
    }

    public void completeCreateGroup() {
        groupNameView.setError(null);
        groupLinkView.setError(null);

        String groupName = groupNameView.getText().toString();
        String groupLink = groupLinkView.getText().toString();

        if (TextUtils.isEmpty(groupName) && TextUtils.isEmpty(groupLink)) {
            Toast.makeText(this, "Both fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(groupName)) {

        }
    }
}
