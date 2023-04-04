package com.example.convo_z.ui.status;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityStatusPageBinding;
import com.example.convo_z.model.User;
import com.example.convo_z.ui.home.HomeActivity;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.status.AddStatusViewModel;

import java.io.Serializable;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddStatusPage extends AppCompatActivity {

    ActivityStatusPageBinding binding;
    private AddStatusViewModel viewModel;
    Uri photoUri;
    Serializable previousActivity;
    User user = new User();
    boolean photoPicked = false;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStatusPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(AddStatusViewModel.class);
        progressDialog = FunctionUtils.getProgressDialog(getString(R.string.uploading_status), getString(R.string.this_will_take_a_few_moments), this);

        handleAddStatusLiveData();
        Intent intent = getIntent();

        if (intent.hasExtra(getString(R.string.previous_activity)))
            previousActivity = intent.getSerializableExtra(getString(R.string.previous_activity));

        if (intent.hasExtra(getString(R.string.user)))
            user = (User) intent.getSerializableExtra(getString(R.string.user));

        binding.eyeIcon.setOnClickListener(view -> startActivity(new Intent(AddStatusPage.this, StatusPrivacyPage.class)));

        binding.share.setOnClickListener(view -> {
            if (photoPicked) {
                String caption = binding.caption.getText().toString();
                progressDialog.show();
                viewModel.addStatus(photoUri, caption, this);
            } else {
                FunctionUtils.getSnackBar(getString(R.string.please_pick_a_photo_first), binding.getRoot()).show();
            }
        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    photoUri = result.getData().getData();
                    binding.camera.setImageURI(photoUri);
                    binding.camera.setClickable(false);
                    photoPicked = true;
                }
            }
    );

    public void onCameraClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    private void handleAddStatusLiveData() {
        androidx.lifecycle.Observer<Resource<Data<User>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    progressDialog.dismiss();
                    binding.caption.setText(getString(R.string.empty_string));
                    binding.camera.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_camera));
                    binding.camera.setClickable(true);
                    FunctionUtils.getSnackBar(getString(R.string.status_updated), binding.getRoot()).show();
                    startActivity(new Intent(this, HomeActivity.class)
                            .putExtra(getString(R.string.set_view_pager), Constants.CASE_STATUS_FRAGMENT));
                    break;
                case EXCEPTION:
                    progressDialog.dismiss();
                    FunctionUtils.getSnackBar(resource.getMessage(), binding.getRoot()).show();
                    break;
            }
        };
        viewModel.AddStatusLiveData.observe(this, observer);
    }

    @Override
    public void onBackPressed() {
        if (previousActivity != null && previousActivity.equals(Constants.CASE_HOME_ACTIVITY)) {
            startActivity(new Intent(AddStatusPage.this, HomeActivity.class)
                    .putExtra(getString(R.string.set_view_pager), Constants.CASE_STATUS_FRAGMENT));
        } else {
            startActivity(new Intent(AddStatusPage.this, OwnStatusPage.class)
                    .putExtra(getString(R.string.user), user));
        }
    }
}
