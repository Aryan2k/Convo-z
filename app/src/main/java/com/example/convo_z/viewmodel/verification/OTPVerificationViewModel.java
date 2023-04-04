package com.example.convo_z.viewmodel.verification;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.repository.VerificationRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;

import java.io.Serializable;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OTPVerificationViewModel extends ViewModel {
    private final VerificationRepository repository;

    @Inject
    public OTPVerificationViewModel(VerificationRepository repository) {
        this.repository = repository;
    }

    public MutableLiveData<Resource<Data<Serializable>>> SignInWithPhoneAuthCredentialLiveData = new MutableLiveData<>();

    public void signInWithPhoneAuthCredential(String id, String mobile, Context context) {
        repository.signInWithPhoneAuthCredential(id, mobile, context, SignInWithPhoneAuthCredentialLiveData);
    }

    public void linkWithCredentials(String uid, String mobile, Context context) {
        repository.linkWithCredentials(uid, mobile, context);
    }
}
