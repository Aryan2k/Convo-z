package com.example.convo_z.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.convo_z.R;
import com.example.convo_z.adapters.StatusAdapters.MutedStatusAdapter;
import com.example.convo_z.adapters.StatusAdapters.RecentStatusAdapter;
import com.example.convo_z.adapters.StatusAdapters.ViewedStatusAdapter;
import com.example.convo_z.databinding.FragmentStatusBinding;
import com.example.convo_z.model.Users;
import com.example.convo_z.status.OwnStatus;
import com.example.convo_z.status.StatusPage;
import com.example.convo_z.utils.FunctionUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class StatusFragment extends Fragment {

    public StatusFragment() {
        // Required empty public constructor
    }

    FragmentStatusBinding binding;
    ArrayList<Users> recentList = new ArrayList<>();
    ArrayList<Users> viewedList = new ArrayList<>();
    ArrayList<Users> mutedList = new ArrayList<>();
    FirebaseDatabase database;
    Users loggedInUser;
    FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater, container, false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //binding.mutedStatusRecyclerView.setActivated(false);
        binding.mutedStatusRecyclerView.setVisibility(View.GONE);
        binding.ll1.setOnClickListener(view -> {
            Intent intent;
            if (loggedInUser.getStatus().size() > 1)
                intent = new Intent(StatusFragment.this.getActivity(), OwnStatus.class);
            else
                intent = new Intent(StatusFragment.this.getActivity(), StatusPage.class);

            intent.putExtra("user", loggedInUser);
            startActivity(intent);
        });

        database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loggedInUser = snapshot.getValue(Users.class);
                Picasso.get().load(loggedInUser.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        binding.recentStatusRecyclerView.setAdapter(new RecentStatusAdapter(recentList, getContext()));
        binding.viewedStatusRecyclerView.setAdapter(new ViewedStatusAdapter(viewedList, getContext()));
        binding.mutedStatusRecyclerView.setAdapter(new MutedStatusAdapter(mutedList, getContext()));

        binding.muted.setOnClickListener(view -> handleMuted(0));

        //loggedInUser isn't being used before this point coz it hasn't loaded info from db yet which causes crashes.
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> contacts = new ArrayList<>();

                Cursor phones = null;
                try {
                    String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
                    phones = Objects.requireNonNull(getContext()).getContentResolver().query
                            (ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, SELECTION, null, null);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "An error occurred,please try again", Toast.LENGTH_SHORT).show();
                }

                while (true) {
                    assert phones != null;
                    if (!(phones.moveToNext() && phones.getCount() > 0)) break;
                    //  String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = phoneNumber.replaceAll("\\s", "");

                    if (phoneNumber.length() >= 10) {
                        if (phoneNumber.length() == 10) {
                            phoneNumber = "+91" + phoneNumber; //gets the last 10 characters of string
                        } else if (phoneNumber.startsWith("91")) {
                            phoneNumber = "+" + phoneNumber;
                        }
                        contacts.add(phoneNumber);
                    }
                }

                phones.close();
                viewedList.clear();
                recentList.clear();
                mutedList.clear();

                if (loggedInUser.getStatus().size() > 1) //1 coz 0th entry is dummy
                {
                    ArrayList<HashMap<String, Object>> status = loggedInUser.getStatus();
                    HashMap<String, Object> hMap = status.get(status.size() - 1);

                    binding.userNameList.setText(R.string.my_status);
                    assert hMap != null;
                    String time = (String) hMap.get("time");

                    binding.lastMessage.setText(FunctionUtils.timeSetter(time));

                    Picasso.get().load((String) hMap.get("link")).placeholder(R.drawable.ic_user).into(binding.profileImage);

                    binding.profileImage.setBorderColor(Color.WHITE);
                    binding.plus.setVisibility(View.GONE);
                    binding.profileImage.setBackground(ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.my_status_border));
                }

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    assert user != null;

                    if (user.getPhoneNumber() != null) {
                        if (!FirebaseAuth.getInstance().getUid().equals(user.getUserId()) && contacts.contains(user.getPhoneNumber()) &&
                                !user.getHidden().contains(loggedInUser.getUserId())) {

                            ArrayList<HashMap<String, Object>> s = user.getStatus();

                            if (s.size() > 1) {
                                HashMap<String, Object> hm = s.get(s.size() - 1); //if the last story is seen,all the stories are seen.
                                ArrayList<String> seen = (ArrayList<String>) hm.get("seen");

                                assert seen != null;
                                if (loggedInUser.getMuted().contains(user.getUserId())) {
                                    mutedList.add(user);
                                    handleMuted(1);
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

                if (recentList.isEmpty())
                    binding.recent.setText(R.string.no_recent_updates);
                else
                    binding.recent.setText(R.string.new_updates);

                if (viewedList.isEmpty())
                    binding.seen.setText(R.string.no_viewed_updates);
                else
                    binding.seen.setText(R.string.viewed_updates);

                if (mutedList.isEmpty()) {
                    binding.muted.setText(R.string.no_muted_updates);
                    binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                } else {
                    binding.muted.setText(R.string.muted_updates);
                    binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_baseline_keyboard_arrow_down_24), null);
                }

                Objects.requireNonNull(binding.recentStatusRecyclerView.getAdapter()).notifyDataSetChanged(); //notifies the adapter that a change has been made (onBindViewHolder)
                Objects.requireNonNull(binding.viewedStatusRecyclerView.getAdapter()).notifyDataSetChanged();
                Objects.requireNonNull(binding.mutedStatusRecyclerView.getAdapter()).notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return binding.getRoot();
    }

    void handleMuted(int calledFrom) {
        if (!mutedList.isEmpty()) {   //don't view up/down arrow on tapping muted if mutedList is empty

            if (binding.mutedStatusRecyclerView.getVisibility() != View.VISIBLE && calledFrom != 1) {
                binding.mutedStatusRecyclerView.setVisibility(View.VISIBLE);

                binding.scrollview.post(() -> binding.scrollview.fullScroll(ScrollView.FOCUS_DOWN));

                binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_baseline_keyboard_arrow_up_24), null);
            } else {
                binding.mutedStatusRecyclerView.setVisibility(View.GONE);
                binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(Objects.requireNonNull(getContext()), R.drawable.ic_baseline_keyboard_arrow_down_24), null);
            }
        }
    }

}