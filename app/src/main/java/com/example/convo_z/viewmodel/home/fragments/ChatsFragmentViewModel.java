package com.example.convo_z.viewmodel.home.fragments;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.adapters.UsersAdapter;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.HomeRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatsFragmentViewModel extends ViewModel {

    private final HomeRepository repository;
    Resource<Data<Boolean>> resource;

    @Inject

    public ChatsFragmentViewModel(HomeRepository repository) {
        this.repository = repository;
        resource = new Resource<>(null, null, null);
    }

    public MutableLiveData<Resource<Data<Boolean>>> LoadAllChatsLiveData = new MutableLiveData<>();

    public void loadAllChats(UsersAdapter adapter, ArrayList<User> list, Context context) {
        LoadAllChatsLiveData.setValue(resource.loading(new Data<>()));
        repository.loadAllChats(adapter, list, context, LoadAllChatsLiveData);
    }
}
