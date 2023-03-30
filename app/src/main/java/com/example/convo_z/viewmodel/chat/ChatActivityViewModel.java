package com.example.convo_z.viewmodel.chat;

import androidx.lifecycle.ViewModel;

import com.example.convo_z.adapters.ChatsAdapter;
import com.example.convo_z.model.MessagesModel;
import com.example.convo_z.repository.ChatRepository;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatActivityViewModel extends ViewModel {
    private final ChatRepository repository;

    @Inject
    public ChatActivityViewModel(ChatRepository repository) {
        this.repository = repository;
    }

    public void loadChat(String senderRoom, ArrayList<MessagesModel> messagesList, ChatsAdapter adapter) {
        repository.loadChat(senderRoom, messagesList, adapter);
    }

    public void sendMessage(MessagesModel model, String receiverRoom, String senderRoom) {
        repository.sendMessage(model, receiverRoom, senderRoom);
    }
}