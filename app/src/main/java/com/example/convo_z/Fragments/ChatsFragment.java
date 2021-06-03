package com.example.convo_z.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.convo_z.Adapters.UsersAdapter;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.databinding.FragmentChatsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ChatsFragment extends Fragment {

    public ChatsFragment() {
        // Required empty public constructor
    }

    FragmentChatsBinding binding;
    ArrayList<Users> list= new ArrayList<>();
    FirebaseDatabase database;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       database = FirebaseDatabase.getInstance();
       binding = FragmentChatsBinding.inflate(inflater,container,false);

        final UsersAdapter adapter = new UsersAdapter(list,getContext());
        binding.chatRecyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        //this adds users to the list whenever there's a change in firebase db (order of code doesn't matter here)
        database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Users user = dataSnapshot.getValue(Users.class);
                    user.setUserId(dataSnapshot.getKey());
                    if(!FirebaseAuth.getInstance().getUid().equals(user.getUserId())) {
                        list.add(user);
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