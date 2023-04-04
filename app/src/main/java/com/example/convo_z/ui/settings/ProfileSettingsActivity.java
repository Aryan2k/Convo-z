package com.example.convo_z.ui.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityProfileSettingsBinding;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.SettingsRepository;
import com.example.convo_z.ui.home.HomeActivity;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.settings.ProfileSettingsActivityViewModel;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.HashMap;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileSettingsActivity extends AppCompatActivity {

    ActivityProfileSettingsBinding binding;
    private ProfileSettingsActivityViewModel viewModel;
    boolean photoChanged = false;
    Serializable disableHome;
    Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ProfileSettingsActivityViewModel.class);

        new SettingsRepository();  // to initialize various instances

        if (getIntent().hasExtra(getString(R.string.disable_home_button)))
            disableHome = getIntent().getSerializableExtra(getString(R.string.disable_home_button));

        if (disableHome != null && disableHome.equals(Constants.CASE_DISABLE_HOME)) {
            binding.backArrow.setVisibility(View.INVISIBLE);
            binding.backArrow.setEnabled(false);
        }

        setUpClickListeners();
        handleLoadCurrentUserLiveData();
        handleUpdateCurrentUserProfilePhoto();
        handleUpdateCurrentUserTextFields();
        loadCurrentUser();
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    photoUri = result.getData().getData();
                    binding.profileImage.setImageURI(photoUri);
                    photoChanged = true;
                }
            }
    );

    private void setUpClickListeners() {
        binding.backArrow.setOnClickListener(v -> startActivity(new Intent(ProfileSettingsActivity.this, HomeActivity.class)));

        binding.plus.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intent);
        });

        binding.save.setOnClickListener(v -> {
            if (!binding.etUsername.getText().toString().trim().isEmpty() && !binding.etBio.getText().toString().trim().isEmpty()) {

                String username = binding.etUsername.getText().toString().trim();
                String bio = binding.etBio.getText().toString().trim();

                HashMap<String, Object> details = new HashMap<>();
                details.put(getString(R.string.user_name), username);
                details.put(getString(R.string.bio), bio);

                if (photoChanged) {
                    viewModel.updateCurrentUserProfilePhoto(photoUri, this);
                }
                viewModel.updateCurrentUserTextFields(details, this);
            } else {
                if (binding.etUsername.getText().toString().trim().isEmpty() && !binding.etBio.getText().toString().trim().isEmpty()) {
                    binding.etUsername.setError(getString(R.string.enter_username));
                } else if (!binding.etUsername.getText().toString().trim().isEmpty() && binding.etBio.getText().toString().trim().isEmpty()) {
                    binding.etBio.setError(getString(R.string.enter_bio));
                } else {
                    binding.etUsername.setError(getString(R.string.enter_username));
                    binding.etBio.setError(getString(R.string.enter_bio));
                }
            }
        });
    }

    private void loadCurrentUser() {
        viewModel.loadCurrentUser(this);
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
                    FunctionUtils.getSnackBar(getString(R.string.profile_photo_updated), binding.getRoot()).show();
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
                    FunctionUtils.getSnackBar(getString(R.string.profile_updated_successfully), binding.getRoot()).show();
                    if (disableHome.equals(Constants.CASE_DISABLE_HOME)) {
                        SharedPreferences sp = getSharedPreferences(getString(R.string.login), MODE_PRIVATE);
                        sp.edit().putInt(getString(R.string.login_check), 1).apply();
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
        if (disableHome.equals(Constants.CASE_DO_NOT_DISABLE_HOME)) {
            startActivity(new Intent(ProfileSettingsActivity.this, HomeActivity.class));
        } else
            moveTaskToBack(true);
    }
}