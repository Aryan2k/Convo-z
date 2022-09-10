package com.example.convo_z.Status;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.convo_z.Adapters.StatusAdapters.MutedStatusAdapter;
import com.example.convo_z.Adapters.StatusAdapters.RecentStatusAdapter;
import com.example.convo_z.Adapters.StatusAdapters.ViewedStatusAdapter;
import com.example.convo_z.Adapters.StatusPrivacyAdapter;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityStatusPageBinding;
import com.example.convo_z.databinding.ActivityStatusPrivacyBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class StatusPrivacy extends AppCompatActivity {

   ActivityStatusPrivacyBinding binding;
    FirebaseDatabase database;
    ArrayList<Users> list= new ArrayList<>();
    Users loggedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityStatusPrivacyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        database= FirebaseDatabase.getInstance();

        binding.imageView10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(StatusPrivacy.this,StatusPage.class);
                startActivity(i);
            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loggedInUser = snapshot.getValue(Users.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> contacts= new ArrayList<>();

                // Log.d("aryan", "onDataChange: ");

                Cursor phones = null;
                try {
                    String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
                    phones = getApplicationContext().getContentResolver().query
                            (ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  null, SELECTION, null, null);
                }catch(Exception e)
                {
                    Toast.makeText(getApplicationContext(),"An error occurred,please try again",Toast.LENGTH_SHORT).show();
                }

                while (phones.moveToNext() && phones != null && phones.getCount() > 0) {
                    //  String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = phoneNumber.replaceAll("\\s","");

                    if(phoneNumber.length()>=10) {

                        if  (phoneNumber.length() == 10) {
                            phoneNumber = "+91"+phoneNumber; //gets the last 10 characters of string
                        }
                        else if(phoneNumber.startsWith("91"))
                        {
                            phoneNumber = "+"+phoneNumber;
                        }
                        contacts.add(phoneNumber);
                    }
                }

                phones.close();
                list.clear();

                ArrayList<String> hidden = loggedInUser.getHidden();
                ArrayList<Users> unhidden = new ArrayList<>();

                final StatusPrivacyAdapter adapter = new StatusPrivacyAdapter(list,getApplicationContext(),hidden);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());

                binding.hiddenRecyclerView.setAdapter(adapter);
                binding.hiddenRecyclerView.setLayoutManager(layoutManager);

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    assert user != null;

                    if (user.getPhoneNumber() != null) {
                        if (!FirebaseAuth.getInstance().getUid().equals(user.getUserId()) && contacts.contains(user.getPhoneNumber())) {
                               if (hidden.contains(user.getUserId()))
                                        list.add(user);
                               else
                                        unhidden.add(user);
                           }
                    }
                }
                list.addAll(unhidden);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}