package com.example.convo_z;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.convo_z.Adapters.ChatAdapter;
import com.example.convo_z.Model.MessagesModel;
import com.example.convo_z.databinding.ActivityChatDetailBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        final String senderId = auth.getUid();
        final String receiverId = getIntent().getStringExtra("userId");
        String receiverName = getIntent().getStringExtra("userName");
        String receiverPic = getIntent().getStringExtra("profilePic");

        binding.userName.setText(receiverName);
        Picasso.get().load(receiverPic).placeholder(R.drawable.ic_user).into(binding.profileImage);

        binding.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(ChatDetailActivity.this,MainActivity.class);
                startActivity(i);
            }
        });

        final ArrayList<MessagesModel> messagesModels = new ArrayList<>();

        final ChatAdapter chatAdapter = new ChatAdapter(messagesModels,this);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        //this adds messages to messagesModels(list) whenever there's a change in firebase db (order of code doesn't matter here)
        database.getReference().child("Chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        messagesModels.clear();
                        for(DataSnapshot snapshot1 : snapshot.getChildren())
                        {
                            MessagesModel messagesModel = snapshot1.getValue(MessagesModel.class);
                            messagesModels.add(messagesModel);
                        }
                        chatAdapter.notifyDataSetChanged(); //notifies the adapter that a change has been made (onBindViewHolder)
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String msg = binding.editTextTextPersonName.getText().toString();

                if (msg.trim().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Type something!", Toast.LENGTH_SHORT).show();
                }
                else {
                    final MessagesModel model = new MessagesModel(senderId, msg);
                    model.setTimestamp(new Date().getTime());
                    binding.editTextTextPersonName.setText("");

                    database.getReference().child("Chats").child(senderRoom)
                            .push()
                            .setValue(model)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    database.getReference().child("Chats").child(receiverRoom)
                                            .push()
                                            .setValue(model)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                }
                            });

                }
            }
        });

    }
}
