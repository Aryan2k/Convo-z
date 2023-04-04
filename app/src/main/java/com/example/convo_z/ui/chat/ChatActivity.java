package com.example.convo_z.ui.chat;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.adapters.ChatsAdapter;
import com.example.convo_z.databinding.ActivityChatBinding;
import com.example.convo_z.model.MessagesModel;
import com.example.convo_z.repository.ChatRepository;
import com.example.convo_z.ui.home.HomeActivity;
import com.example.convo_z.viewmodel.chat.ChatActivityViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity {

    ActivityChatBinding binding;
    private ChatActivityViewModel viewModel;
    String senderId;
    String receiverId;
    String senderRoom;
    String receiverRoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ChatActivityViewModel.class);
        new ChatRepository();  // to initialize firebase database instance in repository
        setUpClickListeners();
        loadUser();

        senderId = FirebaseAuth.getInstance().getUid();
        receiverId = getIntent().getStringExtra(getString(R.string.user_id));
        senderRoom = senderId + receiverId;
        receiverRoom = receiverId + senderId;

        final ArrayList<MessagesModel> messagesList = new ArrayList<>();
        ChatsAdapter adapter = new ChatsAdapter(messagesList, this, receiverId);
        binding.chatRecyclerView.setAdapter(adapter);

        viewModel.loadChat(senderRoom, messagesList, adapter, this);
    }

    private void loadUser() {
        String receiverName = getIntent().getStringExtra(getString(R.string.user_name));
        String receiverPic = getIntent().getStringExtra(getString(R.string.profile_pic));
        binding.userName.setText(receiverName);
        Picasso.get().load(receiverPic).placeholder(R.drawable.ic_user).into(binding.profileImage);
    }

    private void setUpClickListeners() {
        binding.homeImg.setOnClickListener(v -> startActivity(new Intent(ChatActivity.this, HomeActivity.class)));

        binding.sendMsg.setOnClickListener(v -> {
            String msg = binding.editTextTextPersonName.getText().toString();
            if (msg.trim().isEmpty()) {
                binding.editTextTextPersonName.setError(getString(R.string.type_something));
                binding.editTextTextPersonName.requestFocus();
            } else {
                final MessagesModel model = new MessagesModel(senderId, msg);
                model.setTimestamp(new Date().getTime());
                model.setMessage_status(getString(R.string.one)); // message active
                binding.editTextTextPersonName.setText(getString(R.string.empty_string));

                viewModel.sendMessage(model, receiverRoom, senderRoom, this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(ChatActivity.this, HomeActivity.class));
    }
}
