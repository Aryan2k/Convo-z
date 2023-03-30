package com.example.convo_z.viewmodel.ui.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.adapters.ChatsAdapter;
import com.example.convo_z.databinding.ActivityChatDetailBinding;
import com.example.convo_z.model.MessagesModel;
import com.example.convo_z.repository.ChatRepository;
import com.example.convo_z.viewmodel.ui.home.HomeActivity;
import com.example.convo_z.viewmodel.chat.ChatActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    private ChatActivityViewModel viewModel;
    String senderId;
    String receiverId;
    String senderRoom;
    String receiverRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ChatActivityViewModel.class);
        new ChatRepository();  // to initialize firebase database instance in repository
        setUpClickListeners();
        loadUser();

        senderId = FirebaseAuth.getInstance().getUid();
        receiverId = getIntent().getStringExtra("userId");
        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        final ArrayList<MessagesModel> messagesList = new ArrayList<>();
        ChatsAdapter adapter = new ChatsAdapter(messagesList, this, receiverId);
        binding.chatRecyclerView.setAdapter(adapter);

        viewModel.loadChat(senderRoom, messagesList, adapter);
    }

    private void loadUser() {
        String receiverName = getIntent().getStringExtra("userName");
        String receiverPic = getIntent().getStringExtra("profilePic");
        binding.userName.setText(receiverName);
        Picasso.get().load(receiverPic).placeholder(R.drawable.ic_user).into(binding.profileImage);
    }

    private void setUpClickListeners() {
        binding.homeImg.setOnClickListener(v -> {
            Intent i = new Intent(ChatActivity.this, HomeActivity.class);
            i.putExtra("progressDialog", "14");
            startActivity(i);
        });

        binding.sendMsg.setOnClickListener(v -> {
            String msg = binding.editTextTextPersonName.getText().toString();
            if (msg.trim().isEmpty()) {
                binding.editTextTextPersonName.setError("Type something!");
                binding.editTextTextPersonName.requestFocus();
            } else {
                final MessagesModel model = new MessagesModel(senderId, msg);
                model.setTimestamp(new Date().getTime());
                model.setMessage_status("1"); // message active
                binding.editTextTextPersonName.setText("");

                viewModel.sendMessage(model, receiverRoom, senderRoom);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(ChatActivity.this, HomeActivity.class);
        i.putExtra("progressDialog", "14");
        startActivity(i);
    }
}
