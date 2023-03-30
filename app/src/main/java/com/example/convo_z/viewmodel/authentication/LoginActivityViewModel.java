package com.example.convo_z.viewmodel.authentication;

import android.content.Context;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.repository.AuthenticationRepository;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;
import com.google.android.gms.auth.api.identity.SignInClient;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LoginActivityViewModel extends ViewModel {

    private final AuthenticationRepository repository;
    Resource<Data<Boolean>> resource;

    @Inject
    public LoginActivityViewModel(AuthenticationRepository repository) {
        this.repository = repository;
        resource = new Resource<>(null, null, null);
    }

    public MutableLiveData<Constants> ProfileStatusLiveData = new MutableLiveData<>();

    public void checkProfileStatus(String userId, Context context) {
        repository.checkProfileStatus(userId, context, ProfileStatusLiveData);
    }

    public MutableLiveData<Resource<Data<Boolean>>> EmailAndPasswordSignInLiveData = new MutableLiveData<>();

    public void signInWithEmailAndPassword(String emailId, String password) {
        EmailAndPasswordSignInLiveData.setValue(resource.loading(new Data<>()));
        repository.signInWithEmailAndPassword(emailId, password, EmailAndPasswordSignInLiveData);
    }

    public void googleSignIn(Context context, ActivityResultLauncher<IntentSenderRequest> activityResultLauncher, SignInClient oneTapClient) {
        repository.googleSignIn(context, activityResultLauncher, oneTapClient, true);
    }
}