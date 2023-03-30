package com.example.convo_z.viewmodel.ui.home.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.adapters.UsersAdapter;
import com.example.convo_z.databinding.FragmentChatsBinding;
import com.example.convo_z.model.User;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatsBinding.inflate(inflater, container, false);
        ChatsFragmentViewModel viewModel = new ViewModelProvider(this).get(ChatsFragmentViewModel.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UsersAdapter adapter = new UsersAdapter(list, getContext());
            binding.chatRecyclerView.setAdapter(adapter);
            viewModel.loadAllChats(adapter, list, getContext());
        }

        return binding.getRoot();
    }

}