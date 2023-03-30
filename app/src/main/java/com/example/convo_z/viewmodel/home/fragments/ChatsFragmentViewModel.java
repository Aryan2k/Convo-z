package com.example.convo_z.viewmodel.home.fragments;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.convo_z.adapters.UsersAdapter;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.HomeRepository;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatsFragmentViewModel extends ViewModel {

    private final HomeRepository repository;

    @Inject
    public ChatsFragmentViewModel(HomeRepository repository) {
        this.repository = repository;
    }

    public void loadAllChats(UsersAdapter adapter, ArrayList<User> list, Context context) {
        repository.loadAllChats(adapter, list, context);
    }
}
