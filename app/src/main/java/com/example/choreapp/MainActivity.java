package com.example.choreapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.choreapp.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private boolean isSigningin = false;
    private LinearLayout signinTextView;
    private ProgressBar signinProgressView;

    private FirebaseAuth mAuth;

    private int RC_SIGN_IN = 1;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        RelativeLayout signinButton = findViewById(R.id.sign_in_button);
        Utils.setTouchEffect(signinButton, true);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signinWithGoogle();
            }
        });

        signinTextView = findViewById(R.id.signin_text);
        signinProgressView = findViewById(R.id.signin_progress);
    }

    private void signinWithGoogle() {
        showProgress(true);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void showProgress(final boolean show) {
        isSigningin = show;
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        signinTextView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 0 : 1)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        signinTextView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                    }
                });

        signinProgressView.animate()
                .setDuration(shortAnimTime)
                .alpha(show ? 1 : 0)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        signinProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != RC_SIGN_IN) {
            onFail();
            return;
        }

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            // Google Sign In was successful, authenticate with Firebase
            GoogleSignInAccount account = task.getResult(ApiException.class);

            if (account == null) {
                onFail();
                return;
            }

            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            onFail();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUser(user);
                        return;
                    }
                    onFail();
                }
            });
    }

    private void saveUser(FirebaseUser user) {
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
                    showProgress(false);
                    UserHolder.getInstance().setUser(documentReference, getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE));
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
        showProgress(false);
        Toast.makeText(this, "Sign-in failed", Toast.LENGTH_LONG).show();
    }

    public void startCreateProfile(String name) {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        intent.putExtra(defs.CREATE_PROFILE_NAME, name);
        startActivity(intent);
    }
}
