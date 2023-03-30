package com.example.convo_z.viewmodel.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.SettingsRepository;
import com.example.convo_z.viewmodel.ui.home.HomeActivity;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.settings.ProfileSettingsActivityViewModel;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileSettingsActivity extends AppCompatActivity {

    ActivityProfileSettingsBinding binding;
    private ProfileSettingsActivityViewModel viewModel;
    boolean photoChanged = false;
    String disableHome = "2";
    Uri sFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ProfileSettingsActivityViewModel.class);

        new SettingsRepository();  // to initialize various instances

        if (getIntent().hasExtra("disableHome"))
            disableHome = getIntent().getStringExtra("disableHome");

        assert disableHome != null;
        if (disableHome.equals("30")) {
            binding.backArrow.setVisibility(View.INVISIBLE);
            binding.backArrow.setEnabled(false);
        }

        setUpClickListeners();
        handleLoadCurrentUserLiveData();
        handleUpdateCurrentUserProfilePhoto();
        handleUpdateCurrentUserTextFields();
        loadCurrentUser();
    }

    private void setUpClickListeners() {
        binding.backArrow.setOnClickListener(v -> startActivity(new Intent(ProfileSettingsActivity.this, HomeActivity.class).putExtra("progressDialog", "14")));
        binding.plus.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 33);
        });
        binding.save.setOnClickListener(v -> {
            if (!binding.etUsername.getText().toString().trim().isEmpty() && !binding.etBio.getText().toString().trim().isEmpty()) {

                String username = binding.etUsername.getText().toString().trim();
                String bio = binding.etBio.getText().toString().trim();

                HashMap<String, Object> details = new HashMap<>();
                details.put("userName", username);
                details.put("bio", bio);

                if (photoChanged) {
                    viewModel.updateCurrentUserProfilePhoto(sFile);
                }
                viewModel.updateCurrentUserTextFields(details);

            } else {
                if (binding.etUsername.getText().toString().trim().isEmpty() && !binding.etBio.getText().toString().trim().isEmpty()) {
                    binding.etUsername.setError("Enter username");
                } else if (!binding.etUsername.getText().toString().trim().isEmpty() && binding.etBio.getText().toString().trim().isEmpty()) {
                    binding.etBio.setError("Enter bio");
                } else {
                    binding.etUsername.setError("Enter username");
                    binding.etBio.setError("Enter bio");
                }
            }
        });
    }

    private void loadCurrentUser() {
        viewModel.loadCurrentUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null) {
            sFile = data.getData();
            binding.profileImage.setImageURI(sFile);
            photoChanged = true;
        }
    }

    private void handleLoadCurrentUserLiveData() {
        androidx.lifecycle.Observer<Resource<Data<User>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    User currentUser = resource.getData().get();
                    Picasso.get().load(currentUser.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
                    binding.etUsername.setText(currentUser.getUserName());
                    binding.etBio.setText(currentUser.getBio());
                    break;
                case EXCEPTION:
                    //  Toast.makeText(getApplicationContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        };
        viewModel.LoadCurrentUserLiveData.observe(this, observer);
    }

    private void handleUpdateCurrentUserProfilePhoto() {
        androidx.lifecycle.Observer<Resource<Data<User>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    Toast.makeText(getApplicationContext(), "Profile Photo Updated!", Toast.LENGTH_SHORT).show();
                    break;
                case EXCEPTION:
                    //  Toast.makeText(getApplicationContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        };
        viewModel.UpdateCurrentUserProfilePhotoLiveData.observe(this, observer);
    }

    private void handleUpdateCurrentUserTextFields() {
        androidx.lifecycle.Observer<Resource<Data<User>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    Toast.makeText(getApplicationContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                    if (disableHome.equals("30")) {
                        SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
                        sp.edit().putInt("lc", 1).apply();
                        startActivity(new Intent(ProfileSettingsActivity.this, HomeActivity.class));
                    }
                    break;
                case EXCEPTION:
                    //  Toast.makeText(getApplicationContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        };
        viewModel.UpdateCurrentUserTextFieldsLiveData.observe(this, observer);
    }

    @Override
    public void onBackPressed() {
        if (disableHome.equals("10")) {
            startActivity(new Intent(ProfileSettingsActivity.this, HomeActivity.class).putExtra("progressDialog", "14"));
        } else
            moveTaskToBack(true);
    }
}