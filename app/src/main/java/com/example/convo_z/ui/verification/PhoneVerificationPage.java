package com.example.convo_z.ui.verification;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityPhoneVerificationBinding;

import java.io.Serializable;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhoneVerificationPage extends AppCompatActivity {

    ActivityPhoneVerificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPhoneVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Serializable loginStatus = getIntent().getSerializableExtra(getString(R.string.login_status));

        binding.verifyPhone.setOnClickListener(v -> {

            String phoneNumber = binding.editTextPhoneNumber.getText().toString().trim();

            if (phoneNumber.length() != 10) {
                binding.editTextPhoneNumber.setError(getString(R.string.enter_a_valid_phone_number));
                binding.editTextPhoneNumber.requestFocus();
                return;
            }

            startActivity(new Intent(PhoneVerificationPage.this, OTPVerificationPage.class)
                    .putExtra(getString(R.string.phone_number), phoneNumber)
                    .putExtra(getString(R.string.login_status), loginStatus));
        });
    }
}
