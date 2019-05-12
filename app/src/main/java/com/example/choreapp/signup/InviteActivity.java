package com.example.choreapp.signup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.main.GroupsActivity;

public class InviteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        LinearLayout backButton = findViewById(R.id.back);
        Utils.setTouchEffect(backButton, true, false, true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout messageInviteButton = findViewById(R.id.message_invite);
        Utils.setTouchEffect(messageInviteButton, true, false, false);

        LinearLayout codeInviteButton = findViewById(R.id.code_invite);
        Utils.setTouchEffect(codeInviteButton, true, false, false);

        LinearLayout otherInviteButton = findViewById(R.id.other_invite);
        Utils.setTouchEffect(otherInviteButton, true, false, false);

        RelativeLayout nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(InviteActivity.this, GroupsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}
