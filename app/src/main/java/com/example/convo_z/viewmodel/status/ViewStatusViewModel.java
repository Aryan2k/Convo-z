package com.example.convo_z.viewmodel.status;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.convo_z.repository.StatusRepository;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ViewStatusViewModel extends ViewModel {
    private final StatusRepository repository;

    @Inject
    public ViewStatusViewModel(StatusRepository repository) {
        this.repository = repository;
    }

    public void updateSeenList(String userId, ArrayList<HashMap<String, Object>> statusList, Context context) {
        repository.updateSeenList(userId, statusList, context);
    }
}
