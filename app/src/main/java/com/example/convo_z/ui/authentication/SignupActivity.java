package com.example.convo_z.ui.authentication;

import static com.example.convo_z.utils.Constants.CASE_ACCOUNT_LINKING;

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
import com.example.convo_z.databinding.ActivitySignupBinding;
import com.example.convo_z.model.User;
import com.example.convo_z.ui.home.HomeActivity;
import com.example.convo_z.ui.settings.ProfileSettingsActivity;
import com.example.convo_z.ui.verification.PhoneVerificationPage;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.authentication.SignupActivityViewModel;
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
public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    private SignupActivityViewModel viewModel;
    private FirebaseAuth auth;
    SignInClient oneTapClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(SignupActivityViewModel.class);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        oneTapClient = Identity.getSignInClient(this);
        progressDialog = FunctionUtils.getProgressDialog(getString(R.string.registering_user), getString(R.string.we_are_signing_you_up), this);
        setUpClickListeners();
        handleEmailAndPasswordSignUpLiveData();
        handleProfileStatusLiveData();
    }

    private void setUpClickListeners() {
        binding.signupPhone.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, PhoneVerificationPage.class)
                .putExtra(getString(R.string.login_status), Constants.CASE_SIGN_IN_OR_SIGN_UP)));
        binding.google.setOnClickListener(v -> googleSignIn());
        binding.signinTxt.setOnClickListener(view -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
        binding.signupBtn.setOnClickListener(v -> {
            progressDialog.show();
            if (!binding.fullName.getText().toString().isEmpty() && !binding.email.getText().toString().isEmpty()
                    && !binding.password.getText().toString().isEmpty() && !binding.rePassword.getText().toString().isEmpty()) {

                if (binding.password.getText().toString().trim().equals(binding.rePassword.getText().toString().trim())) {
                    viewModel.signUpWithEmailAndPassword(binding.email.getText().toString().trim(), binding.password.getText().toString().trim(),
                            binding.fullName.getText().toString().trim(), this);
                } else {
                    binding.rePassword.setError(getString(R.string.the_passwords_dont_match));
                    binding.rePassword.requestFocus();
                    progressDialog.dismiss();
                }
            } else {
                String result = FunctionUtils.emptyDetector(binding.fullName.getText().toString().trim(), binding.email.getText().toString().trim(),
                        binding.password.getText().toString().trim(), binding.rePassword.getText().toString().trim());
                setSignUpFieldErrors(result);
            }
        });
    }

    private void setSignUpFieldErrors(String result) {
        // 0=empty, 1= not empty
        // 1111,1110,1101,1100,1011,1010,1001,1000,0111,0110,0101,0100,0011,0010,0001,0000
        switch (result) { // 1111 not poss here

            case "1110":
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
                return;

            case "1101":
                binding.password.setError(getString(R.string.enter_your_password));
                binding.password.requestFocus();
                progressDialog.dismiss();
                return;

            case "1100":
                binding.password.setError(getString(R.string.enter_your_password));
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.password.requestFocus();
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
                return;

            case "1011":
                binding.email.setError(getString(R.string.enter_your_email));
                binding.email.requestFocus();
                progressDialog.dismiss();
                return;

            case "1010":
                binding.email.setError(getString(R.string.enter_your_email));
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.email.requestFocus();
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
                return;

            case "1001":
                binding.email.setError(getString(R.string.enter_your_email));
                binding.password.setError(getString(R.string.enter_your_password));
                binding.email.requestFocus();
                binding.password.requestFocus();
                progressDialog.dismiss();
                return;

            case "1000":
                binding.email.setError(getString(R.string.enter_your_email));
                binding.password.setError(getString(R.string.enter_your_password));
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.email.requestFocus();
                binding.password.requestFocus();
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
                return;

            case "0111":
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.fullName.requestFocus();
                progressDialog.dismiss();
                return;

            case "0110":
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.fullName.requestFocus();
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
                return;

            case "0101":
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.password.setError(getString(R.string.enter_your_password));
                binding.fullName.requestFocus();
                binding.password.requestFocus();
                progressDialog.dismiss();
                return;

            case "0100":
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.password.setError(getString(R.string.enter_your_password));
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.fullName.requestFocus();
                binding.password.requestFocus();
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
                return;

            case "0011":
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.email.setError(getString(R.string.enter_your_email));
                binding.fullName.requestFocus();
                binding.email.requestFocus();
                progressDialog.dismiss();
                return;

            case "0010":
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.email.setError(getString(R.string.enter_your_email));
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.fullName.requestFocus();
                binding.email.requestFocus();
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
                return;

            case "0001":
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.email.setError(getString(R.string.enter_your_email));
                binding.password.setError(getString(R.string.enter_your_password));
                binding.fullName.requestFocus();
                binding.email.requestFocus();
                binding.password.requestFocus();
                progressDialog.dismiss();
                return;

            default: //0000
                binding.fullName.setError(getString(R.string.enter_your_full_name));
                binding.email.setError(getString(R.string.enter_your_email));
                binding.password.setError(getString(R.string.enter_your_password));
                binding.rePassword.setError(getString(R.string.enter_your_password_again));
                binding.fullName.requestFocus();
                binding.email.requestFocus();
                binding.password.requestFocus();
                binding.rePassword.requestFocus();
                progressDialog.dismiss();
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
                                                if (!snapshot.hasChild(user.getUid())) // if not,add it to db
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

    private void handleProfileStatusLiveData() {
        androidx.lifecycle.Observer<Constants> observer = profileStatus -> {
            progressDialog.dismiss();
            switch (profileStatus) {
                case CASE_PROFILE_UP_TO_DATE:
                    FunctionUtils.getSnackBar(getString(R.string.sign_in_successful), binding.getRoot()).show();
                    startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                    break;
                case CASE_GOOGLE_OR_EMAIL_SIGNUP:   //  Phone Number Verification Pending
                    startActivity(new Intent(SignupActivity.this, PhoneVerificationPage.class)
                            .putExtra(getString(R.string.login_status), CASE_ACCOUNT_LINKING));  // determines if this is an account linking case or new a independent signin/up using phone
                    break;
                case CASE_PHONE_SIGNUP:   //  User Details Submission Pending
                    startActivity(new Intent(SignupActivity.this, ProfileSettingsActivity.class));
                    break;
            }
        };
        viewModel.ProfileStatusLiveData.observe(this, observer);
    }

    private void handleEmailAndPasswordSignUpLiveData() {
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
        viewModel.EmailAndPasswordSignUpLiveData.observe(this, observer);
    }
}
