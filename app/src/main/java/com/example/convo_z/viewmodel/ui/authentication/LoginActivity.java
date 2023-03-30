package com.example.convo_z.viewmodel.ui.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.AuthenticationRepository;
import com.example.convo_z.viewmodel.ui.home.HomeActivity;
import com.example.convo_z.viewmodel.ui.settings.ProfileSettingsActivity;
import com.example.convo_z.viewmodel.ui.verification.PhoneVerificationPage;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.authentication.LoginActivityViewModel;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private LoginActivityViewModel viewModel;
    FirebaseAuth auth;
    SignInClient oneTapClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(LoginActivityViewModel.class);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        oneTapClient = Identity.getSignInClient(this);
        new AuthenticationRepository();  // to initialize firebase database and auth instances
        setUpProgressDialog();
        setUpClickListeners();
        handleEmailAndPasswordSignInLiveData();
        handleProfileStatusLiveData();
    }

    private void setUpProgressDialog() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Signing in user");
        progressDialog.setMessage("We're signing you in!");
        //  progressDialog.setProgressStyle(android.R.attr.progressBarStyleSmall);
        Objects.requireNonNull(progressDialog.getWindow()).
                setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.bg_progress_dialog));
    }

    private void setUpClickListeners() {
        binding.signupTxt.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        binding.signinPhone.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, PhoneVerificationPage.class);
            i.putExtra("code", "22"); // dummy value to avoid crash
            startActivity(i);
        });
        binding.google.setOnClickListener(v -> googleSignIn());
        binding.signin.setOnClickListener(v -> {
            if (!binding.email.getText().toString().isEmpty() && !binding.password.getText().toString().isEmpty()) {
                viewModel.signInWithEmailAndPassword(binding.email.getText().toString().trim(), binding.password.getText().toString().trim());
            } else {
                setSignInFieldErrors();
            }
        });
    }

    private void setSignInFieldErrors() {
        if (binding.email.getText().toString().trim().isEmpty() && !binding.password.getText().toString().trim().isEmpty()) {
            binding.email.setError("Enter your email");
            binding.email.requestFocus();
        } else if (!binding.email.getText().toString().trim().isEmpty() && binding.password.getText().toString().trim().isEmpty()) {
            binding.password.setError("Enter your password");
            binding.password.requestFocus();
        } else {
            binding.email.setError("Enter your email");
            binding.password.setError("Enter your password");
            binding.email.requestFocus();
            binding.password.requestFocus();
        }
    }

    ActivityResultLauncher<IntentSenderRequest> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(this, task -> {
                                    if (task.isSuccessful()) {
                                        final FirebaseUser user = auth.getCurrentUser();
                                        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override                 // to check if the user has already signed up with that google account
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                assert user != null;
                                                if (!snapshot.hasChild(user.getUid())) // if not, add it to db
                                                {
                                                    User newUser = new User();

                                                    ArrayList<HashMap<String, Object>> status = new ArrayList<>();
                                                    ArrayList<String> muted = new ArrayList<>();
                                                    ArrayList<String> blocked = new ArrayList<>();
                                                    ArrayList<String> hidden = new ArrayList<>();

                                                    HashMap<String, Object> s = new HashMap<>();
                                                    s.put("dummy", "");
                                                    status.add(s);
                                                    muted.add("");
                                                    blocked.add("");
                                                    hidden.add("");

                                                    newUser.setMuted(muted);
                                                    newUser.setStatus(status);
                                                    newUser.setBlocked(blocked);
                                                    newUser.setHidden(hidden);
                                                    newUser.setBio("");
                                                    newUser.setLastSeen("");
                                                    newUser.setEmail(user.getEmail());
                                                    //users.setPassword("");
                                                    newUser.setUserId(user.getUid());
                                                    newUser.setUserName(user.getDisplayName());
                                                    newUser.setProfilePic(Objects.requireNonNull(user.getPhotoUrl()).toString());

                                                    database.getReference().child("Users").child(user.getUid()).setValue(newUser);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                        viewModel.checkProfileStatus(Objects.requireNonNull(auth.getCurrentUser()).getUid(), getApplicationContext());
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } catch (ApiException e) {
                    // ...
                }
            });

    private void googleSignIn() {
        progressDialog.show();
        viewModel.googleSignIn(this, activityResultLauncher, oneTapClient);
    }

    private void handleEmailAndPasswordSignInLiveData() {
        androidx.lifecycle.Observer<Resource<Data<Boolean>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    progressDialog.show();
                    break;
                case SUCCESS:
                    viewModel.checkProfileStatus(auth.getUid(), getApplicationContext());
                    break;
                case EXCEPTION:
                    Toast.makeText(getApplicationContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                    break;
            }
        };
        viewModel.EmailAndPasswordSignInLiveData.observe(this, observer);
    }

    private void handleProfileStatusLiveData() {
        androidx.lifecycle.Observer<Constants> observer = profileStatus -> {
            progressDialog.dismiss();
            switch (profileStatus) {
                case CASE_PROFILE_UP_TO_DATE:
                    Toast.makeText(getApplicationContext(), "Sign in successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    break;
                case CASE_GOOGLE_OR_EMAIL_SIGNUP:   //  Phone Number Verification Pending
                    Intent i = new Intent(LoginActivity.this, PhoneVerificationPage.class);
                    i.putExtra("code", "44");  // determines if this is an account linking case or new a independent signin/up using phone
                    startActivity(i);
                    break;
                case CASE_PHONE_SIGNUP:   //  User Details Submission Pending
                    startActivity(new Intent(LoginActivity.this, ProfileSettingsActivity.class));
                    break;
            }
        };
        viewModel.ProfileStatusLiveData.observe(this, observer);
    }
}
