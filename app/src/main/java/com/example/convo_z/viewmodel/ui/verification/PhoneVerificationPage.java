package com.example.convo_z.viewmodel.ui.verification;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhoneVerificationPage extends AppCompatActivity {

    ActivityPhoneVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final String passcode = getIntent().getStringExtra("code");

        binding.verifyPhone.setOnClickListener(v -> {

            String mobile = binding.editTextMobile.getText().toString().trim();

            if (mobile.length() != 10) {
                binding.editTextMobile.setError("Enter a valid phone number");
                binding.editTextMobile.requestFocus();
                return;
            }

            startActivity(new Intent(PhoneVerificationPage.this, OTPVerificationPage.class)
                    .putExtra("mobile", mobile)
                    .putExtra("code", passcode));
        });
    }
}
