package com.example.choreapp.signup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;
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

import com.example.choreapp.DataHolder;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

public class CreateProfileActivity extends AppCompatActivity {

    private boolean isCreating = false;

    private int selectedColor = -1;
    private View selectedColorButton = null;
    private EditText nameView;

    private TextView nextTextView;
    private ProgressBar nextProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        LinearLayout backButton = findViewById(R.id.back);
        Utils.setTouchEffect(backButton, true, false, true);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        View colorOne = findViewById(R.id.color_one);
        View colorTwo = findViewById(R.id.color_two);
        View colorThree = findViewById(R.id.color_three);
        View colorFour = findViewById(R.id.color_four);
        View colorFive = findViewById(R.id.color_five);
        View colorSix = findViewById(R.id.color_six);

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

        nameView = findViewById(R.id.create_name);
        String name = getIntent().getStringExtra(defs.CREATE_PROFILE_NAME);

        if (!TextUtils.isEmpty(name)) {
            nameView.setText(name);
        }

        RelativeLayout nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProfile();
            }
        });

        nextTextView = findViewById(R.id.next_text);
        nextProgressView = findViewById(R.id.next_progress);
    }

    public void selectColorHandler(View view, final int colorIndex) {
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

    public void createProfile() {
        if (isCreating) {
            return;
        }

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_LONG).show();
            return;
        }

        nameView.setError(null);

        if (selectedColor < 0) {
            Toast.makeText(this, "Select a profile color", Toast.LENGTH_SHORT).show();
            return;
        }

        final String name = nameView.getText().toString();
        if (TextUtils.isEmpty(name)) {
            nameView.setError("This field is required");
            nameView.requestFocus();
            return;
        }

        showProgress(true);

        DocumentReference user = DataHolder.getInstance().getUser().userRef;
        user.update(
                User.NAME, name,
                User.COLOR, defs.USER_COLORS[selectedColor])
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    showProgress(false);

                    if (!task.isSuccessful()) {
                        Toast.makeText(CreateProfileActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User user = DataHolder.getInstance().getUser();
                    user.name = name;
                    user.color = defs.USER_COLORS[selectedColor];
                    startCreateGroup();
                }
            });
    }

    public void showProgress(final boolean show) {
        isCreating = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        nextTextView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        nextTextView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        nextProgressView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        nextProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }

    private void startCreateGroup() {
        getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE)
            .edit()
            .putBoolean(defs.IS_LOGGED_IN, true)
            .apply();

        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivity(intent);
    }
}
