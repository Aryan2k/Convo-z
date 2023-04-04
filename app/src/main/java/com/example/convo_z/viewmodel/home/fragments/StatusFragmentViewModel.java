package com.example.convo_z.viewmodel.home.fragments;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.model.User;
import com.example.convo_z.repository.HomeRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatusFragmentViewModel extends ViewModel {

    private final HomeRepository repository;

    @Inject
    public StatusFragmentViewModel(HomeRepository repository) {
        this.repository = repository;
    }

    public MutableLiveData<Resource<Data<User>>> LoadUserLiveData = new MutableLiveData<>();

    public void getUser(String uId, Context context) {
        repository.getUser(uId, context, LoadUserLiveData);
    }

    public MutableLiveData<Resource<Data<Boolean>>> LoadAllStatusLiveData = new MutableLiveData<>();

    public void loadAllStatus(ArrayList<User> recentList, ArrayList<User> viewedList, ArrayList<User> mutedList, Context context) {
        repository.loadAllStatus(recentList, viewedList, mutedList, context, LoadAllStatusLiveData);
    }

}
