package com.example.convo_z.Verification;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.convo_z.Login.LoginClass;
import com.example.convo_z.MainActivity;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.SettingsActivity;
import com.example.convo_z.databinding.ActivityOtpVerificationBinding;
import com.example.convo_z.databinding.ActivityPhoneVerificationBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class OTPVerification extends AppCompatActivity {


    //These are the objects needed
    //It is the verification id that will be sent to the user
    private String mVerificationId;

    //The edittext to input the code
    private EditText editTextCode;
    ProgressDialog progressDialog;

    ActivityOtpVerificationBinding binding2;

    FirebaseDatabase database;
     static String mobile;
     static String passcode="";
    //firebase auth object
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding2 = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding2.getRoot());

        database = FirebaseDatabase.getInstance();
        //initializing objects
        mAuth = FirebaseAuth.getInstance();

        editTextCode = binding2.editTextCode;

        progressDialog = new ProgressDialog(OTPVerification.this);
        progressDialog.setTitle("Verifying OTP");
        progressDialog.setMessage("This will just take a few moments.");

        //getting mobile number from the previous activity
        //and sending the verification code to the number

         Intent intent = getIntent();
         mobile = intent.getStringExtra("mobile");

         sendVerificationCode(mobile);
         passcode= intent.getStringExtra("code");  //determines if this is an account linking case or new independent signin/up using phone

        //if the automatic sms detection does not work, user can also enter the code manually
        //so adding a Onclick listener to the button

        binding2.verifyotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();

                if (code.length()!= 6) {
                    editTextCode.setError("Enter the valid 6-digit code");
                    editTextCode.requestFocus();
                    return;
                }
                //verifying the code entered manually
                verifyVerificationCode(code);
            }
        });

    }

    //the callback to detect the verification status automatically
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) { //code sent,verify it.

            //Getting the code sent by SMS
            String code = phoneAuthCredential.getSmsCode();

            //sometime the code is not detected automatically
            //in this case the code will be null
            //so user has to manually enter the code

            if (code != null) {
                editTextCode.setText(code);
                //verifying the code
                verifyVerificationCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) { // if SHA configuration is wrong,etc. Code likely couldn't be sent.
            Toast.makeText(OTPVerification.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);

            //storing the verification id that is sent to the user
            mVerificationId = s;
        }
    };

    //this method sends verification code
    //the country id is concatenated
    // can take the country id as user input as well

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + mobile,
                60,
                TimeUnit.SECONDS,
                OTPVerification.this,
                mCallbacks);
    }

    private void verifyVerificationCode(String code) {
        //creating the credential
        progressDialog.show();
        try {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);

                if (passcode.equals("44"))  //account linking case
                {
                    linkWithCredentials(credential);
                } else {   //signin/signup using phone
                    signInWithPhoneAuthCredential(credential);
                }
            }
            catch (Exception e)
            {
                progressDialog.dismiss();
                Toast toast = Toast.makeText(this, "An error occurred, Please try again.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(OTPVerification.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final String id = task.getResult().getUser().getUid();

                            database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override    //to check if the user has already signed up with that account
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if(!snapshot.hasChild(id)) //if not,add it to db.
                                    {
                                        binding2.verifyotp.setEnabled(false);
                                        Users users = new Users();

                                        ArrayList<HashMap<String,Object>> status = new ArrayList<>();
                                        ArrayList<String> muted = new ArrayList<>();
                                        ArrayList<String> blocked = new ArrayList<>();
                                        ArrayList<String> hidden = new ArrayList<>();

                                        HashMap<String,Object> s = new HashMap<>();
                                        s.put("dummy","");
                                        status.add(s);
                                        muted.add("");
                                        blocked.add("");
                                        hidden.add("");

                                        users.setMuted(muted);
                                        users.setStatus(status);
                                        users.setBlocked(blocked);
                                        users.setUserName("");
                                        users.setHidden(hidden);
                                     //   users.setPassword("");
                                        users.setLastSeen("");
                                        users.setBio("");
                                    //    users.setEmail("");
                                        String profilePic = getResources().getString(R.string.ic_user);
                                        users.setProfilePic(profilePic);//default

                                        users.setPhoneNumber("+91"+mobile);
                                        users.setUserId(id);

                                        database.getReference().child("Users").child(id).setValue(users);
                                        progressDialog.dismiss();

                                        Intent i = new Intent(OTPVerification.this, SettingsActivity.class);
                                        i.putExtra("disableHome","30"); //to disable home button
                                        startActivity(i);
                                    }
                                    else
                                    {
                                        SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                                        sp.edit().putInt("lc",1).apply();

                                        progressDialog.dismiss();
                                        Intent intent = new Intent(OTPVerification.this, MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    progressDialog.dismiss();
                                }
                            });

                        } else {

                            //verification unsuccessful.. display an error message
                            progressDialog.dismiss();
                            String message = "Something is wrong, we will fix it soon...";

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                message = "Invalid code entered!";
                            }

                            Snackbar snackbar = Snackbar.make(binding2.container, message, Snackbar.LENGTH_LONG);
                            snackbar.setAction("Dismiss", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                            snackbar.show();
                        }
                    }
                });
    }

    private void linkWithCredentials(PhoneAuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                      //      Log.d(TAG, "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            assert user != null;
                            database.getReference().child("Users").child(user.getUid()).child("phoneNumber").setValue(mobile);

                            SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                            sp.edit().putInt("lc",1).apply();

                            progressDialog.dismiss();
                            Intent i = new Intent(OTPVerification.this,MainActivity.class);
                               startActivity(i);

                        } else {
                         //   Log.w(TAG, "linkWithCredential:failure", task.getException());
                            progressDialog.dismiss();
                            Toast.makeText(OTPVerification.this, task.getException().toString(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

 //   @Override
//    public void onBackPressed() {
  //      moveTaskToBack(true);
  //  }
}