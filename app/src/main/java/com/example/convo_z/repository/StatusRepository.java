package com.example.convo_z.repository;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.convo_z.adapters.SeenListAdapter;
import com.example.convo_z.adapters.StatusPrivacyAdapter;
import com.example.convo_z.model.User;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

public class StatusRepository {
    static FirebaseDatabase database;
    Resource<Data<User>> resource;

    @Inject
    public StatusRepository() {
        database = FirebaseDatabase.getInstance();
        resource = new Resource<>(null, null, "");
    }

    public void loadCurrentUser(String uId, MutableLiveData<Resource<Data<User>>> LoadCurrentUserLiveData) {
        database.getReference().child("Users").child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Data<User> data = new Data<>();
                data.set(snapshot.getValue(User.class));
                LoadCurrentUserLiveData.setValue(resource.success(data));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void loadAllUsers(ArrayList<User> userList, ArrayList<String> hidden, StatusPrivacyAdapter adapter, Context context) {
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> contacts = FunctionUtils.getContactsList(context);
                userList.clear();
                ArrayList<User> unhidden = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;

                    if (user.getPhoneNumber() != null) {
                        if (!Objects.equals(FirebaseAuth.getInstance().getUid(), user.getUserId()) && contacts.contains(user.getPhoneNumber())) {
                            if (hidden.contains(user.getUserId()))
                                userList.add(user);
                            else
                                unhidden.add(user);
                        }
                    }
                }
                userList.addAll(unhidden);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void updateSeenList(String userId, ArrayList<HashMap<String, Object>> statusList) {
        database.getReference().child("Users").child(userId).child("status").setValue(statusList);
    }

    public void backupDeletedStatus(String userId, HashMap<String, Object> status) {
        database.getReference().child("Users").child(userId).child("deletedStatus").push().setValue(status);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadSeenList(ArrayList<String> seen, ArrayList<User> seenList, SeenListAdapter adapter) {
        for (int i = 1; i < Objects.requireNonNull(seen).size(); i++) {
            database.getReference().child("Users").child(seen.get(i)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User currentUser = snapshot.getValue(User.class);
                    seenList.add(currentUser);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    public void updateStatusList(String userId, ArrayList<HashMap<String, Object>> statusList, MutableLiveData<Resource<Data<User>>> UpdateStatusListLiveData) {
        database.getReference().child("Users").child(userId).child("status").
                setValue(statusList).addOnSuccessListener(unused -> UpdateStatusListLiveData.setValue(resource.success(new Data<>())));
    }
}
