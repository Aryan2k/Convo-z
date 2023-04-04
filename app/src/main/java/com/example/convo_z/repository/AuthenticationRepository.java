package com.example.convo_z.repository;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.convo_z.R;
import com.example.convo_z.model.User;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.Resource;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

public class AuthenticationRepository {
    static FirebaseDatabase database;
    static FirebaseAuth auth;
    Resource<Data<Boolean>> resource;

    @Inject
    public AuthenticationRepository() {
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        resource = new Resource<>(null, null, null);
    }

    public void signInWithEmailAndPassword(String emailId, String password, MutableLiveData<Resource<Data<Boolean>>> EmailAndPasswordSignInLiveData) {
        auth.signInWithEmailAndPassword(emailId, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        EmailAndPasswordSignInLiveData.setValue(resource.success(new Data<>()));
                    } else {
                        EmailAndPasswordSignInLiveData.setValue(resource.exception(new Data<>(), Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });
    }

    public void signUpWithEmailAndPassword(String emailId, String password, String fullName, Context context,
                                           MutableLiveData<Resource<Data<Boolean>>> EmailAndPasswordSignUpLiveData) {
        auth.createUserWithEmailAndPassword(emailId, password).
                addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User newUser = new User(fullName, emailId, password);
                        String id = Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getUser()).getUid();
                        String profilePic = context.getResources().getString(R.string.ic_user);

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

                        newUser.setMuted(muted);
                        newUser.setStatus(status);
                        newUser.setBlocked(blocked);
                        newUser.setHidden(hidden);
                        newUser.setProfilePic(profilePic);
                        newUser.setUserId(id);
                        newUser.setBio(context.getResources().getString(R.string.empty_string));
                        newUser.setLastSeen(context.getResources().getString(R.string.empty_string));

                        database.getReference().child(context.getResources().getString(R.string.users)).child(id).setValue(newUser);

                        EmailAndPasswordSignUpLiveData.setValue(resource.success(new Data<>()));
                    } else {
                        EmailAndPasswordSignUpLiveData.setValue(resource.exception(new Data<>(), Objects.requireNonNull(task.getException()).getMessage()));
                    }
                });
    }

    public void checkProfileStatus(final String userId, Context context, MutableLiveData<Constants> ProfileStatusLiveData) {
        database.getReference().child(context.getResources().getString(R.string.users)).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(context.getResources().getString(R.string.phone_number)).exists() && snapshot.child(context.getResources().getString(R.string.user_name)).exists()) {  // profile already updated
                    SharedPreferences sp = context.getSharedPreferences(context.getResources().getString(R.string.login), Context.MODE_PRIVATE);
                    sp.edit().putInt(context.getResources().getString(R.string.login_check), 1).apply();
                    ProfileStatusLiveData.setValue(Constants.CASE_PROFILE_UP_TO_DATE);

                } else if (!snapshot.child(context.getResources().getString(R.string.phone_number)).exists() && snapshot.child(context.getResources().getString(R.string.user_name)).exists()) {  // google signup or email signup
                    ProfileStatusLiveData.setValue(Constants.CASE_GOOGLE_OR_EMAIL_SIGNUP);

                } else if (snapshot.child(context.getResources().getString(R.string.phone_number)).exists() && !snapshot.child(context.getResources().getString(R.string.user_name)).exists()) {  // signup using phone
                    ProfileStatusLiveData.setValue(Constants.CASE_PHONE_SIGNUP);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void googleSignIn(Context context, ActivityResultLauncher<IntentSenderRequest> activityResultLauncher, SignInClient oneTapClient, Boolean filter) {
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(context.getString(R.string.default_web_client_id))
                        // Only show accounts previously used to sign in = true. Show all accounts = false.
                        .setFilterByAuthorizedAccounts(filter)
                        .build())
                .build();

        oneTapClient.beginSignIn(signInRequest).addOnSuccessListener((Activity) context, result -> {
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(result.getPendingIntent().getIntentSender()).build();
                    activityResultLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener((Activity) context, e -> {
                    //  No saved credentials found. Launch the One Tap sign-up flow, or
                    //  do nothing and continue presenting the signed-out UI.
                    //   Log.d("aryan", Objects.requireNonNull(e.getLocalizedMessage()));
                });
    }
}
