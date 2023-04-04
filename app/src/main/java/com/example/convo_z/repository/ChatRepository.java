package com.example.convo_z.repository;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;

import com.example.convo_z.R;
import com.example.convo_z.adapters.ChatsAdapter;
import com.example.convo_z.model.MessagesModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import javax.inject.Inject;

public class ChatRepository {
    static FirebaseDatabase database;

    @Inject
    public ChatRepository() {
        database = FirebaseDatabase.getInstance();
    }

    public void loadChat(String senderRoom, ArrayList<MessagesModel> messagesList, ChatsAdapter adapter, Context context) {
        // this adds messages to messagesModels(list) whenever there's a change in firebase db.
        database.getReference().child(context.getResources().getString(R.string.chats)).child(senderRoom)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messagesList.clear();
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            MessagesModel messagesModel = snapshot1.getValue(MessagesModel.class);
                            assert messagesModel != null;
                            messagesModel.setMessageID(snapshot1.getKey());
                            messagesList.add(messagesModel);
                        }
                        adapter.notifyDataSetChanged(); // notifies the adapter that a change has been made (onBindViewHolder)
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void sendMessage(MessagesModel model, String receiverRoom, String senderRoom, Context context) {
        final DatabaseReference pushedPostRef = database.getReference().child(context.getResources().getString(R.string.chats)).child(receiverRoom).push();
        // pushedPostRef stores the entire link of where data will be
        //-pushed: Chats-receiverRoom-messageIdOfReceiver
        pushedPostRef.setValue(model)
                .addOnSuccessListener(aVoid -> {
                    model.setMessageID_receiver(pushedPostRef.getKey()); // gets only the messageIdOfReceiver
                    database.getReference().child(context.getResources().getString(R.string.chats)).child(senderRoom)
                            .push()
                            .setValue(model).addOnSuccessListener(aVoid1 -> {
                            });
                });
    }
}
