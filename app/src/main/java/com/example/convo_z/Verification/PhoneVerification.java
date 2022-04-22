package com.example.convo_z.Verification;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convo_z.Adapters.FragmentsAdapter;
import com.example.convo_z.databinding.ActivityMainBinding;
import com.example.convo_z.databinding.ActivityPhoneVerificationBinding;
import com.google.firebase.auth.FirebaseAuth;

public class PhoneVerification extends AppCompatActivity {

    ActivityPhoneVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent i = getIntent();
        final String passcode = i.getStringExtra("code");

        binding.verifyphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mobile = binding.editTextMobile.getText().toString().trim();

                if(mobile.length() != 10){
                    binding.editTextMobile.setError("Enter a valid phone number");
                    binding.editTextMobile.requestFocus();
                    return;
                }

                Intent intent = new Intent(PhoneVerification.this, OTPVerification.class);
                intent.putExtra("mobile", mobile);
                intent.putExtra("code",passcode);
                startActivity(intent);
            }
        });
    }
   /* @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }*/

}
