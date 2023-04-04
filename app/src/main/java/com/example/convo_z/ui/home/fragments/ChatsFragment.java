package com.example.convo_z.ui.home.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.adapters.UsersAdapter;
import com.example.convo_z.databinding.FragmentChatsBinding;
import com.example.convo_z.model.User;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.home.fragments.ChatsFragmentViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatsFragment extends Fragment {

    public ChatsFragment() {
        // Required empty public constructor
    }

    FragmentChatsBinding binding;
    ArrayList<User> list = new ArrayList<>();
    ProgressDialog progressDialog;
    private ChatsFragmentViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(ChatsFragmentViewModel.class);
        progressDialog = FunctionUtils.getProgressDialog(getString(R.string.loading), getString(R.string.getting_things_ready_asap), this.getContext());
        handleLoadAllChatsLiveData();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UsersAdapter adapter = new UsersAdapter(list, getContext());
            binding.chatRecyclerView.setAdapter(adapter);
            viewModel.loadAllChats(adapter, list, getContext());
        }
        return binding.getRoot();
    }

    private void handleLoadAllChatsLiveData() {
        @SuppressLint("NotifyDataSetChanged") androidx.lifecycle.Observer<Resource<Data<Boolean>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    progressDialog.show();
                    break;
                case SUCCESS:
                case EXCEPTION:
                    progressDialog.dismiss();
                    break;
            }
        };
        viewModel.LoadAllChatsLiveData.observe(getViewLifecycleOwner(), observer);
    }
}