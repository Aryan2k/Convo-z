package com.example.convo_z.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.MutableLiveData;

import com.example.convo_z.adapters.UsersAdapter;
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

public class HomeRepository { // Database calls will take place here
    static FirebaseDatabase database;
    User loggedInUser;

    @Inject
    public HomeRepository() {
        database = FirebaseDatabase.getInstance();
    }

    public void loadAllChats(UsersAdapter adapter, ArrayList<User> list, Context context) {
        //this adds users to the list whenever there's a change in firebase db.
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> contacts = FunctionUtils.getContactsList(context);
                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    assert user != null;
                    user.setUserId(dataSnapshot.getKey());

                    if (user.getPhoneNumber() != null) {
                        if (!user.getPhoneNumber().startsWith("+91")) {
                            user.setPhoneNumber("+91" + user.getPhoneNumber());
                            database.getReference().child("Users").child(user.getUserId()).child("phoneNumber").setValue(user.getPhoneNumber());
                        }
                        if (!Objects.equals(FirebaseAuth.getInstance().getUid(), user.getUserId()) && contacts.contains(user.getPhoneNumber())) {
                            list.add(user); // changes to the list are made here but they are reflected in recyclerView in the adapter's onBindViewHolderMethod
                        }
                    }
                }
                adapter.notifyDataSetChanged(); // notifies the adapter that a change has been made (onBindViewHolder)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void loadAllStatus(ArrayList<User> recentList, ArrayList<User> viewedList, ArrayList<User> mutedList, Context context,
                              MutableLiveData<Resource<Data<Boolean>>> LoadAllStatusLiveData) {//, MutableLiveData<Resource<Data<ArrayList<ArrayList<User>>>>> UpdateAllStatusListsLiveData) {

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @SuppressWarnings("unchecked")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot allUsersSnapshot) {

                database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot loggedInUserSnapshot) {
                        loggedInUser = loggedInUserSnapshot.getValue(User.class);

                        assert loggedInUser != null;
                        Log.e("aryan", "onDataChange: " + loggedInUser.getMuted().size());

                        ArrayList<String> contacts = FunctionUtils.getContactsList(context);
                        Data<Boolean> state = new Data<>();
                        Resource<Data<Boolean>> resource = new Resource<>(null, null, "");

                        viewedList.clear();
                        recentList.clear();
                        mutedList.clear();

                        for (DataSnapshot dataSnapshot : allUsersSnapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            assert user != null;

                            if (user.getPhoneNumber() != null) {
                                if (!Objects.equals(FirebaseAuth.getInstance().getUid(), user.getUserId()) && contacts.contains(user.getPhoneNumber()) && !user.getHidden().contains(loggedInUser.getUserId())) {

                                    ArrayList<HashMap<String, Object>> s = user.getStatus();

                                    if (s.size() > 1) {
                                        HashMap<String, Object> hm = s.get(s.size() - 1); // if the last story is seen,all the stories are seen.
                                        ArrayList<String> seen = (ArrayList<String>) hm.get("seen");

                                        assert seen != null;
                                        if (loggedInUser.getMuted().contains(user.getUserId())) {
                                            mutedList.add(user);
                                            state.set(Boolean.FALSE);  // to invoke handleMuted(1)
                                            LoadAllStatusLiveData.setValue(resource.success(state));
                                        } else {
                                            if (!seen.contains(loggedInUser.getUserId())) {
                                                recentList.add(user);
                                            } else {
                                                viewedList.add(user);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        state.set(Boolean.TRUE);   // all the three lists have been loaded.
                        LoadAllStatusLiveData.setValue(resource.success(state));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void getUser(String uId, MutableLiveData<Resource<Data<User>>> LoadUserLiveData) {
        Data<User> userData = new Data<>();
        Resource<Data<User>> resource = new Resource<>(null, null, "");
        database.getReference().child("Users").child(uId).get().addOnCompleteListener(task -> {
            userData.set((task.getResult()).getValue(User.class));
            LoadUserLiveData.setValue(resource.success(userData));
        });
    }

}
