package com.example.convo_z.verification;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convo_z.databinding.ActivityPhoneVerificationBinding;

public class PhoneVerification extends AppCompatActivity {

    ActivityPhoneVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent i = getIntent();
        final String passcode = i.getStringExtra("code");

        binding.verifyPhone.setOnClickListener(v -> {

            String mobile = binding.editTextMobile.getText().toString().trim();

            if (mobile.length() != 10) {
                binding.editTextMobile.setError("Enter a valid phone number");
                binding.editTextMobile.requestFocus();
                return;
            }

            Intent intent = new Intent(PhoneVerification.this, OTPVerification.class);
            intent.putExtra("mobile", mobile);
            intent.putExtra("code", passcode);
            startActivity(intent);
        });
    }
}
