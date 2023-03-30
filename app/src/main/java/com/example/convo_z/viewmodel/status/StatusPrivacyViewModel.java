package com.example.convo_z.viewmodel.status;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.adapters.StatusPrivacyAdapter;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.StatusRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StatusPrivacyViewModel extends ViewModel {

    private final StatusRepository repository;

    @Inject
    public StatusPrivacyViewModel(StatusRepository repository) {
        this.repository = repository;
    }

    public MutableLiveData<Resource<Data<User>>> LoadCurrentUserLiveData = new MutableLiveData<>();

    public void loadCurrentUser(String uId) {
        repository.loadCurrentUser(uId, LoadCurrentUserLiveData);
    }

    public void loadAllUsers(ArrayList<User> userList, ArrayList<String> hidden, StatusPrivacyAdapter adapter, Context context) {
        repository.loadAllUsers(userList, hidden, adapter, context);
    }
}
