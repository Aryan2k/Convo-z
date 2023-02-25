package com.example.convo_z.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.convo_z.adapters.UsersAdapter;
import com.example.convo_z.model.Users;
import com.example.convo_z.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class ChatsFragment extends Fragment {

    public ChatsFragment() {
        // Required empty public constructor
    }

    FragmentChatsBinding binding;
    ArrayList<Users> list = new ArrayList<>();
    FirebaseDatabase database;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        database = FirebaseDatabase.getInstance();
        binding = FragmentChatsBinding.inflate(inflater, container, false);

        final UsersAdapter adapter = new UsersAdapter(list, getContext());
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        //this adds users to the list whenever there's a change in firebase db.
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
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
                list.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    assert user != null;
                    user.setUserId(dataSnapshot.getKey());

                    if (user.getPhoneNumber() != null) {
                        if (!user.getPhoneNumber().startsWith("+91")) {
                            user.setPhoneNumber("+91" + user.getPhoneNumber());
                            database.getReference().child("Users").child(user.getUserId())
                                    .child("phoneNumber").setValue(user.getPhoneNumber());
                        }
                        if (!Objects.equals(FirebaseAuth.getInstance().getUid(), user.getUserId()) && contacts.contains(user.getPhoneNumber())) {
                            list.add(user); //changes to the list are made here but they are reflected in recyclerView in the adapter's onBindViewHolderMethod
                        }
                    }
                }
                adapter.notifyDataSetChanged(); //notifies the adapter that a change has been made (onBindViewHolder)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return binding.getRoot();
    }

}