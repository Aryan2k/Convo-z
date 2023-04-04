package com.example.convo_z.ui.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityLoginBinding;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.AuthenticationRepository;
import com.example.convo_z.ui.home.HomeActivity;
import com.example.convo_z.ui.settings.ProfileSettingsActivity;
import com.example.convo_z.ui.verification.PhoneVerificationPage;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
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
        progressDialog = FunctionUtils.getProgressDialog(getString(R.string.signing_in_user), getString(R.string.we_are_signing_you_in), this);
        setUpClickListeners();
        handleEmailAndPasswordSignInLiveData();
        handleProfileStatusLiveData();
    }

    private void setUpClickListeners() {
        binding.signupTxt.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        binding.signinPhone.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, PhoneVerificationPage.class)
                .putExtra(getString(R.string.login_status), Constants.CASE_SIGN_IN_OR_SIGN_UP)));
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
            binding.email.setError(getString(R.string.enter_your_email));
            binding.email.requestFocus();
        } else if (!binding.email.getText().toString().trim().isEmpty() && binding.password.getText().toString().trim().isEmpty()) {
            binding.password.setError(getString(R.string.enter_your_password));
            binding.password.requestFocus();
        } else {
            binding.email.setError(getString(R.string.enter_your_email));
            binding.password.setError(getString(R.string.enter_your_password));
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
                                        database.getReference().child(getString(R.string.users)).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                    s.put(getString(R.string.dummy), getString(R.string.empty_string));
                                                    status.add(s);
                                                    muted.add(getString(R.string.empty_string));
                                                    blocked.add(getString(R.string.empty_string));
                                                    hidden.add(getString(R.string.empty_string));

                                                    newUser.setMuted(muted);
                                                    newUser.setStatus(status);
                                                    newUser.setBlocked(blocked);
                                                    newUser.setHidden(hidden);
                                                    newUser.setBio(getString(R.string.empty_string));
                                                    newUser.setLastSeen(getString(R.string.empty_string));
                                                    newUser.setEmail(user.getEmail());
                                                    // users.setPassword("");
                                                    newUser.setUserId(user.getUid());
                                                    newUser.setUserName(user.getDisplayName());
                                                    newUser.setProfilePic(Objects.requireNonNull(user.getPhotoUrl()).toString());

                                                    database.getReference().child(getString(R.string.users)).child(user.getUid()).setValue(newUser);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                        viewModel.checkProfileStatus(Objects.requireNonNull(auth.getCurrentUser()).getUid(), getApplicationContext());
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        FunctionUtils.getSnackBar(Objects.requireNonNull(task.getException()).toString(), binding.getRoot()).show();
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
                    FunctionUtils.getSnackBar(resource.getMessage(), binding.getRoot()).show();
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
                    FunctionUtils.getSnackBar(getString(R.string.sign_in_successful), binding.getRoot()).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    break;
                case CASE_GOOGLE_OR_EMAIL_SIGNUP:   //  Phone Number Verification Pending
                    startActivity(new Intent(LoginActivity.this, PhoneVerificationPage.class)
                            .putExtra(getString(R.string.login_status), Constants.CASE_ACCOUNT_LINKING));  // determines if this is an account linking case or new a independent signin/up using phone
                    break;
                case CASE_PHONE_SIGNUP:   //  User Details Submission Pending
                    startActivity(new Intent(LoginActivity.this, ProfileSettingsActivity.class));
                    break;
            }
        };
        viewModel.ProfileStatusLiveData.observe(this, observer);
    }
}
