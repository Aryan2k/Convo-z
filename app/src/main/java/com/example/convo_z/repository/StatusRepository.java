package com.example.convo_z.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.convo_z.R;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

public class StatusRepository {
    static FirebaseDatabase database;
    Resource<Data<User>> resource;
    FirebaseStorage storage;

    @Inject
    public StatusRepository() {
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        resource = new Resource<>(null, null, "");
    }

    public void loadCurrentUser(String uId, Context context, MutableLiveData<Resource<Data<User>>> LoadCurrentUserLiveData) {
        database.getReference().child(context.getResources().getString(R.string.users)).child(uId).addValueEventListener(new ValueEventListener() {
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
        database.getReference().child((context.getResources().getString(R.string.users))).addValueEventListener(new ValueEventListener() {
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

    public void updateSeenList(String userId, ArrayList<HashMap<String, Object>> statusList, Context context) {
        database.getReference().child(context.getResources().getString(R.string.users)).child(userId).child(context.getResources().getString(R.string.status)).setValue(statusList);
    }

    public void backupDeletedStatus(String userId, HashMap<String, Object> status, Context context) {
        database.getReference().child(context.getResources().getString(R.string.users)).child(userId).child(context.getResources().getString(R.string.deleted_status)).push().setValue(status);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void loadSeenList(ArrayList<String> seen, ArrayList<User> seenList, SeenListAdapter adapter, Context context) {
        for (int i = 1; i < Objects.requireNonNull(seen).size(); i++) {
            database.getReference().child((context.getResources().getString(R.string.users))).child(seen.get(i)).addValueEventListener(new ValueEventListener() {
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

    public void updateStatusList(String userId, ArrayList<HashMap<String, Object>> statusList, Context context, MutableLiveData<Resource<Data<User>>> UpdateStatusListLiveData) {
        database.getReference().child(context.getResources().getString(R.string.users)).child(userId).child(context.getResources().getString(R.string.status))
                .setValue(statusList).addOnSuccessListener(unused -> UpdateStatusListLiveData.setValue(resource.success(new Data<>())));
    }

    public void addStatus(Uri sFile, String caption, Context context, MutableLiveData<Resource<Data<User>>> AddStatusLiveData) {
        final StorageReference reference = storage.getReference().child(context.getResources().getString(R.string.status_updates)).child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        String time = String.valueOf(new Date().getTime());
        reference.child(time).putFile(sFile).addOnSuccessListener(taskSnapshot -> reference.child(time).getDownloadUrl().addOnSuccessListener(uri -> database.getReference().child(context.getResources().getString(R.string.users)).child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);

                        assert user != null;

                        ArrayList<HashMap<String, Object>> status = user.getStatus();
                        HashMap<String, Object> newStatus = new HashMap<>();
                        ArrayList<String> seen = new ArrayList<>();
                        seen.add(context.getResources().getString(R.string.dummy));

                        newStatus.put(context.getResources().getString(R.string.link), uri.toString());
                        newStatus.put(context.getResources().getString(R.string.caption), caption);
                        newStatus.put(context.getResources().getString(R.string.time), time);
                        newStatus.put(context.getResources().getString(R.string.seen), seen);

                        status.add(newStatus);

                        database.getReference().child(context.getResources().getString(R.string.users)).child(FirebaseAuth.getInstance().getUid())
                                .child(context.getResources().getString(R.string.status)).setValue(status);

                        AddStatusLiveData.setValue(resource.success(new Data<>()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        AddStatusLiveData.setValue(resource.exception(new Data<>(), error.getMessage()));
                    }
                })));
    }
}
