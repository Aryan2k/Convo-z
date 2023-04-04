package com.example.convo_z.viewmodel.status;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.convo_z.adapters.SeenListAdapter;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.StatusRepository;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OwnStatusViewModel extends ViewModel {
    private final StatusRepository repository;

    @Inject
    public OwnStatusViewModel(StatusRepository repository) {
        this.repository = repository;
    }

    public void backupDeletedStatus(String userId, HashMap<String, Object> status, Context context) {
        repository.backupDeletedStatus(userId, status, context);
    }

    public void loadSeenList(ArrayList<String> seen, ArrayList<User> seenList, SeenListAdapter adapter, Context context) {
        repository.loadSeenList(seen, seenList, adapter, context);
    }

    public MutableLiveData<Resource<Data<User>>> UpdateStatusListLiveData = new MutableLiveData<>();

    public void updateStatusList(String userId, ArrayList<HashMap<String, Object>> statusList, Context context) {
        repository.updateStatusList(userId, statusList, context, UpdateStatusListLiveData);
    }
}
