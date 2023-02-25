package com.example.convo_z.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.convo_z.MainActivity;
import com.example.convo_z.R;
import com.example.convo_z.adapters.ChatAdapter;
import com.example.convo_z.model.MessagesModel;
import com.example.convo_z.databinding.ActivityChatDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    //    Objects.requireNonNull(getSupportActionBar()).hide();

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

        binding.imageView.setOnClickListener(v -> {
            Intent i = new Intent(ChatDetailActivity.this, MainActivity.class);
            i.putExtra("progressDialog", "14");
            startActivity(i);
        });

        final ArrayList<MessagesModel> messagesModels = new ArrayList<>();

        final ChatAdapter chatAdapter = new ChatAdapter(messagesModels, this, receiverId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom = senderId + receiverId;
        final String receiverRoom = receiverId + senderId;

        //this adds messages to messagesModels(list) whenever there's a change in firebase db.
        database.getReference().child("Chats").child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesModels.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessagesModel messagesModel = snapshot1.getValue(MessagesModel.class);
                            assert messagesModel != null;
                            messagesModel.setMessageID(snapshot1.getKey());
                            messagesModels.add(messagesModel);
                        }
                        chatAdapter.notifyDataSetChanged(); //notifies the adapter that a change has been made (onBindViewHolder)
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.imageView6.setOnClickListener(v -> {

            String msg = binding.editTextTextPersonName.getText().toString();

            if (msg.trim().isEmpty()) {
                //Toast.makeText(getApplicationContext(), "Type something!", Toast.LENGTH_SHORT).show();
                binding.editTextTextPersonName.setError("Type something!");
                binding.editTextTextPersonName.requestFocus();
            } else {

                final MessagesModel model = new MessagesModel(senderId, msg);
                model.setTimestamp(new Date().getTime());
                model.setMessage_status("1"); //message active
                binding.editTextTextPersonName.setText("");

                final DatabaseReference pushedPostRef = database.getReference().child("Chats").child(receiverRoom).push();
                //pushedPostRef stores the entire link of where data will be
                //-pushed: Chats-receiverRoom-messageIdOfReceiver
                pushedPostRef
                        .setValue(model)
                        .addOnSuccessListener(aVoid -> {
                            model.setMessageID_receiver(pushedPostRef.getKey()); //gets only the messageIdOfReceiver
                            database.getReference().child("Chats").child(senderRoom)
                                    .push()
                                    .setValue(model).addOnSuccessListener(aVoid1 -> {
                                    });
                        });
            }
        });

    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ChatDetailActivity.this, MainActivity.class);
        i.putExtra("progressDialog", "14");
        startActivity(i);
    }
}
