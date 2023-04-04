package com.example.convo_z.viewmodel.status;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.model.User;
import com.example.convo_z.repository.StatusRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddStatusViewModel extends ViewModel {
    private final StatusRepository repository;

    @Inject
    public AddStatusViewModel(StatusRepository repository) {
        this.repository = repository;
    }

    public MutableLiveData<Resource<Data<User>>> AddStatusLiveData = new MutableLiveData<>();

    public void addStatus(Uri sFile, String caption, Context context) {
        repository.addStatus(sFile, caption, context, AddStatusLiveData);
    }
}
