package com.example.choreapp.signup;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.main.GroupsActivity;

public class InviteActivity extends AppCompatActivity {

    private String group_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        group_code = getIntent().getStringExtra(defs.INVITE_CODE);

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
        messageInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMessage();
            }
        });

        LinearLayout codeInviteButton = findViewById(R.id.code_invite);
        Utils.setTouchEffect(codeInviteButton, true, false, false);
        codeInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickCode();
            }
        });

        LinearLayout otherInviteButton = findViewById(R.id.other_invite);
        Utils.setTouchEffect(otherInviteButton, true, false, false);
        otherInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickOther();
            }
        });

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

    private void onClickMessage() {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", "Join my group at ChoreApp! Code: " + group_code);
        sendIntent.setType("vnd.android-dir/mms-sms");
        startActivity(sendIntent);
    }

    private void onClickCode() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", "Join my group at ChoreApp! Code: " + group_code);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Code copied", Toast.LENGTH_SHORT).show();
    }

    private void onClickOther() {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Join my group at ChoreApp! Code: " + group_code);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }
}
