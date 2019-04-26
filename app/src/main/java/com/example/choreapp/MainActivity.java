package com.example.choreapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.choreapp.models.User;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private int RC_SIGN_IN = 1;
    private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        providers = Arrays.asList(new AuthUI.IdpConfig.GoogleBuilder().build());

        RelativeLayout signinButton = findViewById(R.id.sign_in_button);
        signinButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.setAlpha(0.5f);
                } else {
                    v.setAlpha(1f);
                }
                return false;
            }
        });
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setAvailableProviders(providers)
                                .build(),
                        RC_SIGN_IN
                );
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != RC_SIGN_IN || resultCode != RESULT_OK) {
            onFail();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            onFail();
            return;
        }

        final String name = user.getDisplayName();
        String google_id = user.getUid();

        User newUser = new User(name, google_id);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(User.COLLECTION)
                .add(newUser.toMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        startCreateProfile(name);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        onFail();
                    }
                });
    }

    public void onFail() {
        Toast.makeText(this, "Sign-in failed", Toast.LENGTH_LONG).show();
    }

    public void startCreateProfile(String name) {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        intent.putExtra(defs.CREATE_PROFILE_NAME, name);
        startActivity(intent);
    }
}
