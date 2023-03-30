package com.example.convo_z.viewmodel.ui.status;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.adapters.StatusPrivacyAdapter;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.StatusRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.status.StatusPrivacyViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StatusPrivacyPage extends AppCompatActivity {

    ActivityStatusPrivacyBinding binding;
    private StatusPrivacyViewModel viewModel;
    ArrayList<User> userList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStatusPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(StatusPrivacyViewModel.class);
        new StatusRepository(); // to initialize firebase Database instance;
        handleLoadCurrentUserLiveData();
        binding.homeImg.setOnClickListener(view -> startActivity(new Intent(StatusPrivacyPage.this, AddStatusPage.class)));
        viewModel.loadCurrentUser(FirebaseAuth.getInstance().getUid());
    }

    private void handleLoadCurrentUserLiveData() {
        androidx.lifecycle.Observer<Resource<Data<User>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    ArrayList<String> hidden = resource.getData().get().getHidden();
                    final StatusPrivacyAdapter adapter = new StatusPrivacyAdapter(userList, getApplicationContext(), hidden);
                    binding.hiddenRecyclerView.setAdapter(adapter);
                    viewModel.loadAllUsers(userList, hidden, adapter,this);
                    break;
                case EXCEPTION:
                    Toast.makeText(getApplicationContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        };
        viewModel.LoadCurrentUserLiveData.observe(this, observer);
    }
}