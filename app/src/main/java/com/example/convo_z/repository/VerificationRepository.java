package com.example.convo_z.repository;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.convo_z.R;
import com.example.convo_z.model.User;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

public class VerificationRepository {
    static FirebaseDatabase database;
    static FirebaseAuth auth;
    Resource<Data<Serializable>> resource;
    Data<Serializable> data;

    @Inject
    public VerificationRepository() {
        database = FirebaseDatabase.getInstance();
        resource = new Resource<>(null, null, null);
        auth = FirebaseAuth.getInstance();
        data = new Data<>();
    }

    public void signInWithPhoneAuthCredential(String id, String phoneNumber, Context context, MutableLiveData<Resource<Data<Serializable>>> SignInWithPhoneAuthCredentialLiveData) {
        database.getReference().child(context.getResources().getString(R.string.users)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override    // to check if the user has already signed up with that account
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.hasChild(id)) // if not,add it to db.
                {
                    SignInWithPhoneAuthCredentialLiveData.setValue(resource.loading(new Data<>()));
                    User users = new User();

                    ArrayList<HashMap<String, Object>> status = new ArrayList<>();
                    ArrayList<String> muted = new ArrayList<>();
                    ArrayList<String> blocked = new ArrayList<>();
                    ArrayList<String> hidden = new ArrayList<>();

                    HashMap<String, Object> s = new HashMap<>();
                    s.put(context.getResources().getString(R.string.dummy), context.getResources().getString(R.string.empty_string));
                    status.add(s);
                    muted.add(context.getResources().getString(R.string.empty_string));
                    blocked.add(context.getResources().getString(R.string.empty_string));
                    hidden.add(context.getResources().getString(R.string.empty_string));

                    users.setMuted(muted);
                    users.setStatus(status);
                    users.setBlocked(blocked);
                    users.setUserName(context.getResources().getString(R.string.empty_string));
                    users.setHidden(hidden);
                    //   users.setPassword("");
                    users.setLastSeen(context.getResources().getString(R.string.empty_string));
                    users.setBio(context.getResources().getString(R.string.empty_string));
                    //    users.setEmail("");
                    String profilePic = context.getResources().getString(R.string.ic_user);
                    users.setProfilePic(profilePic);//default

                    users.setPhoneNumber("+91" + phoneNumber);
                    users.setUserId(id);

                    database.getReference().child(context.getResources().getString(R.string.users)).child(id).setValue(users);
                    data.set(Constants.CASE_NEW_USER);
                    SignInWithPhoneAuthCredentialLiveData.setValue(resource.success(data));
                } else {
                    if (!Objects.requireNonNull(snapshot.child(id).getValue(User.class)).getUserName().isEmpty()) {
                        SharedPreferences sp = context.getSharedPreferences(context.getResources().getString(R.string.login), MODE_PRIVATE);
                        sp.edit().putInt(context.getResources().getString(R.string.login_check), 1).apply();
                        data.set(Constants.CASE_OLD_USER);
                        SignInWithPhoneAuthCredentialLiveData.setValue(resource.success(data));
                    } else {
                        data.set(Constants.CASE_NEW_USER);
                        SignInWithPhoneAuthCredentialLiveData.setValue(resource.success(data));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                SignInWithPhoneAuthCredentialLiveData.setValue(resource.exception(new Data<>(), context.getResources().getString(R.string.empty_string)));
            }
        });
    }

    public void linkWithCredentials(String uid, String phoneNumber, Context context) {
        database.getReference().child(context.getResources().getString(R.string.users)).child(uid).child(context.getResources().getString(R.string.phone_number)).setValue(phoneNumber);
    }
}
