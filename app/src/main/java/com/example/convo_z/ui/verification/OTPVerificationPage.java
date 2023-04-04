package com.example.convo_z.ui.verification;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityOtpVerificationBinding;
import com.example.convo_z.repository.VerificationRepository;
import com.example.convo_z.ui.home.HomeActivity;
import com.example.convo_z.ui.settings.ProfileSettingsActivity;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.verification.OTPVerificationViewModel;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OTPVerificationPage extends AppCompatActivity {

    // It is the verification id that will be sent to the user
    private String mVerificationId;
    ProgressDialog progressDialog;
    ActivityOtpVerificationBinding binding;
    private OTPVerificationViewModel viewModel;
    static String phoneNumber;
    static Serializable loginStatus;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(OTPVerificationViewModel.class);
        auth = FirebaseAuth.getInstance();

        new VerificationRepository();  // to initialize various instances

        loadDetails();
        progressDialog = FunctionUtils.getProgressDialog(getString(R.string.verifying_otp), getString(R.string.this_will_take_a_few_moments), this);
        setUpClickListeners();
        handleSignInWithPhoneAuthCredentialLiveData();
    }

    private void loadDetails() {
        // getting mobile number from the previous activity
        // and sending the verification code to the number
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.phone_number))) {
            phoneNumber = intent.getStringExtra(getString(R.string.phone_number));
            sendVerificationCode(phoneNumber);
        }
        if (intent.hasExtra(getString(R.string.login_status)))
            loginStatus = intent.getSerializableExtra(getString(R.string.login_status));  // determines if this is an account linking case or new independent signin/up using phone.
    }

    private void setUpClickListeners() {
        // if the automatic sms detection does not work, user can also enter the code manually
        // so we're adding a Onclick listener to the button
        binding.verifyOtp.setOnClickListener(v -> {
            String code = binding.editTextOtp.getText().toString().trim();
            if (code.length() != 6) {
                binding.editTextOtp.setError(getString(R.string.enter_a_valid_6_digit_code));
                binding.editTextOtp.requestFocus();
                return;
            }
            // verifying the code entered manually
            verifyVerificationCode(code);
        });
    }

    // this method sends verification code. the country id is concatenated. we can take the country id as user input as well
    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber("+91" + phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // the callback to detect the verification status automatically
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) { // code sent,verify it.
            // Getting the code sent by SMS
            String smsCode = phoneAuthCredential.getSmsCode();
            // sometimes the code is not detected automatically. In that case the code will be null so user will have to manually enter the code. Not in this case though.
            if (smsCode != null) {
                binding.editTextOtp.setText(smsCode);
                verifyVerificationCode(smsCode); // verifying the code
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) { // if SHA configuration is wrong,etc. Code likely couldn't be sent.
            Toast.makeText(OTPVerificationPage.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // storing the verification id that is sent to the user
            mVerificationId = s;
        }
    };

    private void verifyVerificationCode(String code) {
        // creating the credential
        progressDialog.show();
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            if (loginStatus.equals(Constants.CASE_ACCOUNT_LINKING)) {  // account linking case
                linkWithCredentials(credential);
            } else {   // signin/signup using phone
                signInWithPhoneAuthCredential(credential);
            }
        } catch (Exception e) {
            progressDialog.dismiss();
            FunctionUtils.getSnackBar(getString(R.string.an_error_occurred_please_try_again), binding.container).show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnCompleteListener(OTPVerificationPage.this, task -> {
                    if (task.isSuccessful()) {
                        final String id = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                        viewModel.signInWithPhoneAuthCredential(id, phoneNumber, this);
                    } else {
                        // verification unsuccessful.. display an error message
                        progressDialog.dismiss();
                        String message = getString(R.string.something_is_wrong_we_will_fix_it_soon);
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            message = getString(R.string.invalid_code_entered);
                        }
                        FunctionUtils.getSnackBar(message, binding.container).show();
                    }
                });
    }

    private void linkWithCredentials(PhoneAuthCredential credential) {
        Objects.requireNonNull(auth.getCurrentUser()).linkWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();
                        assert user != null;
                        viewModel.linkWithCredentials(user.getUid(), phoneNumber, this);
                        SharedPreferences sp = getSharedPreferences(getString(R.string.login), MODE_PRIVATE);
                        sp.edit().putInt(getString(R.string.login_check), 1).apply();
                        startActivity(new Intent(OTPVerificationPage.this, HomeActivity.class));
                    } else {
                        FunctionUtils.getSnackBar(Objects.requireNonNull(task.getException()).toString(), binding.container).show();
                    }
                });
    }

    private void handleSignInWithPhoneAuthCredentialLiveData() {
        androidx.lifecycle.Observer<Resource<Data<Serializable>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    binding.verifyOtp.setEnabled(false);
                    break;
                case SUCCESS:
                    progressDialog.dismiss();
                    if (resource.getData().get().equals(Constants.CASE_NEW_USER)) {
                        startActivity(new Intent(OTPVerificationPage.this, ProfileSettingsActivity.class)
                                .putExtra(getString(R.string.disable_home_button), Constants.CASE_DISABLE_HOME)); // to disable home button
                    } else {
                        startActivity(new Intent(OTPVerificationPage.this, HomeActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                    break;
                case EXCEPTION:
                    progressDialog.dismiss();
                    break;
            }
        };
        viewModel.SignInWithPhoneAuthCredentialLiveData.observe(this, observer);
    }
}