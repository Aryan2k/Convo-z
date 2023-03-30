package com.example.convo_z.viewmodel.settings;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.model.User;
import com.example.convo_z.repository.SettingsRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileSettingsActivityViewModel extends ViewModel {
    private final SettingsRepository repository;

    @Inject
    public ProfileSettingsActivityViewModel(SettingsRepository repository) {
        this.repository = repository;
    }

    public MutableLiveData<Resource<Data<User>>> LoadCurrentUserLiveData = new MutableLiveData<>();

    public void loadCurrentUser() {
        repository.loadCurrentUser(LoadCurrentUserLiveData);
    }

    public MutableLiveData<Resource<Data<User>>> UpdateCurrentUserTextFieldsLiveData = new MutableLiveData<>();

    public void updateCurrentUserTextFields(HashMap<String, Object> details) {
        repository.updateCurrentUserTextFields(details, UpdateCurrentUserTextFieldsLiveData);
    }

    public MutableLiveData<Resource<Data<User>>> UpdateCurrentUserProfilePhotoLiveData = new MutableLiveData<>();

    public void updateCurrentUserProfilePhoto(Uri sFile) {
        repository.updateCurrentUserProfilePhoto(sFile, UpdateCurrentUserProfilePhotoLiveData);
    }
}
