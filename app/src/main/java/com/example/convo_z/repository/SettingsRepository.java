package com.example.convo_z.repository;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.convo_z.R;
import com.example.convo_z.model.User;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

public class SettingsRepository {
    static FirebaseDatabase database;
    Resource<Data<User>> resource;
    FirebaseAuth auth;
    FirebaseStorage storage;

    @Inject
    public SettingsRepository() {
        database = FirebaseDatabase.getInstance();
        resource = new Resource<>(null, null, null);
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    public void loadCurrentUser(Context context, MutableLiveData<Resource<Data<User>>> LoadCurrentUserLiveData) {
        database.getReference().child(context.getResources().getString(R.string.users)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Data<User> data = new Data<>();
                        data.set(user);
                        LoadCurrentUserLiveData.setValue(resource.success(data));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    public void updateCurrentUserTextFields(HashMap<String, Object> details, Context context, MutableLiveData<Resource<Data<User>>> UpdateCurrentUserTextFieldsLiveData) {
        database.getReference().child((context.getResources().getString(R.string.users))).child(Objects.requireNonNull(auth.getUid()))
                .updateChildren(details).addOnSuccessListener(aVoid -> UpdateCurrentUserTextFieldsLiveData.setValue(resource.success(new Data<>())));
    }

    public void updateCurrentUserProfilePhoto(Uri sFile, Context context, MutableLiveData<Resource<Data<User>>> UpdateCurrentUserProfilePhotoLiveData) {
        final StorageReference reference = storage.getReference().child(context.getResources().getString(R.string.profile_pictures)).child(Objects.requireNonNull(auth.getUid()));
        reference.putFile(sFile).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> database.getReference()
                .child((context.getResources().getString(R.string.users))).child(auth.getUid()).child((context.getResources().getString(R.string.profile_pic))).setValue(uri.toString())));
        UpdateCurrentUserProfilePhotoLiveData.setValue(resource.success(new Data<>()));
    }
}
