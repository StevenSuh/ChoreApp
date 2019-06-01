package com.example.choreapp.signup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.choreapp.DataHolder;
import com.example.choreapp.main.groups.GroupsActivity;
import com.example.choreapp.R;
import com.example.choreapp.Utils;
import com.example.choreapp.defs;
import com.example.choreapp.main.messages.MessagingService;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

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

        SharedPreferences prefs = getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean(defs.IS_LOGGED_IN, false);

        if (isLoggedIn) {
            startActivity(new Intent(this, GroupsActivity.class));
            finish();
            return;
        } else {
            Intent intent = new Intent(this, MessagingService.class);
            stopService(intent);
        }

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        RelativeLayout signinButton = findViewById(R.id.sign_in_button);
        Utils.setTouchEffect(signinButton, true, false, true);
        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signinWithGoogle();
            }
        });

        signinTextView = findViewById(R.id.signin_text);
        signinProgressView = findViewById(R.id.signin_progress);
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

    private void signinWithGoogle() {
        if (isSigningin) {
            return;
        }

        if (!Utils.isNetworkAvailable(this)) {
            Toast.makeText(this, "No internet", Toast.LENGTH_LONG).show();
            return;
        }

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

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    checkUser(user);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    onFail();
                }
            });
    }

    private void checkUser(final FirebaseUser user) {
        if (user == null) {
            onFail();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String google_id = user.getUid();

        db.collection(User.COLLECTION)
            .whereEqualTo(User.GOOGLE_ID, google_id)
            .limit(1)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.isSuccessful()) {
                        saveUser(user);
                        return;
                    }

                    QuerySnapshot queryDocumentSnapshots = task.getResult();
                    if (queryDocumentSnapshots == null || queryDocumentSnapshots.getDocuments().isEmpty()) {
                        saveUser(user);
                        return;
                    }

                    DocumentSnapshot userQuery = queryDocumentSnapshots.getDocuments().get(0);

                    showProgress(false);
                    DataHolder.getInstance().setUser(userQuery, getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE));

                    if (userQuery.get(User.COLOR) == null) {
                        startCreateProfile(user.getDisplayName());
                        return;
                    }

                    getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE).edit()
                            .putBoolean(defs.IS_LOGGED_IN, true)
                            .apply();

                    Intent intent = new Intent(LoginActivity.this, GroupsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            });
    }

    private void saveUser(FirebaseUser user) {
        final String name = user.getDisplayName();
        String google_id = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User newUser = new User(name, google_id);
        db.collection(User.COLLECTION)
                .add(newUser.toMap())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        showProgress(false);
                        DataHolder.getInstance().setUser(documentReference, getSharedPreferences(defs.SHARED_PREF, MODE_PRIVATE));
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
