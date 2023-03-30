package com.example.convo_z.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.R;
import com.example.convo_z.model.MessagesModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

@SuppressWarnings("rawtypes")
public class ChatsAdapter extends RecyclerView.Adapter {

    ArrayList<MessagesModel> messagesModels;
    Context context;

    int SENDER_VIEW_TYPE = 1;
    int RECEIVER_VIEW_TYPE = 2;
    String recID;

    public ChatsAdapter(ArrayList<MessagesModel> messagesModels, Context context, String recID) {
        this.messagesModels = messagesModels;
        this.context = context;
        this.recID = recID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sample_receiver, parent, false);
            return new ReceiverViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (messagesModels.get(position).getUid().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final MessagesModel messagesModel = messagesModels.get(position);

        holder.itemView.setOnLongClickListener(v -> {

            if (messagesModel.getUid().equals(FirebaseAuth.getInstance().getUid()) && messagesModel.getMessage_status().equals("1")) {
                //if the selected message was sent by the logged-in user and the message hasn't been deleted yet.
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Are you sure you want to delete this message?")
                        .setPositiveButton("Delete for me", (dialog, which) -> {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            String senderRoom = FirebaseAuth.getInstance().getUid() + recID;

                            //keep backup before deleting
                            database.getReference().child("Chats").child(senderRoom)
                                    .child(messagesModel.getMessageID())
                                    .child("deletedForMe")
                                    .setValue(messagesModel.getMessage());

                            database.getReference().child("Chats").child(senderRoom)
                                    .child(messagesModel.getMessageID())
                                    .child("message")
                                    .setValue("");

                            messagesModel.setMessage_status("0");   //message_status =0 and message="" tells that the message was deleted for me by the user

                            database.getReference().child("Chats").child(senderRoom)
                                    .child(messagesModel.getMessageID())
                                    .child("message_status")
                                    .setValue(messagesModel.getMessage_status());                      //updated in db
                        }).setNegativeButton("Don't Delete", (dialog, which) -> dialog.dismiss()).setNeutralButton("Delete for All", (dialog, which) -> {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            String senderRoom = FirebaseAuth.getInstance().getUid() + recID;
                            String receiverRoom = recID + FirebaseAuth.getInstance().getUid();

                            String senderDelete = "Deleted for everyone.";
                            String receiverDelete = "This message was deleted.";

                            //keep backup before deleting
                            database.getReference().child("Chats").child(senderRoom)
                                    .child(messagesModel.getMessageID())
                                    .child("deleted")
                                    .setValue(messagesModel.getMessage());

                            //delete for sender
                            database.getReference().child("Chats").child(senderRoom)
                                    .child(messagesModel.getMessageID())
                                    .child("message")
                                    .setValue(senderDelete);
                            //delete for receiver
                            database.getReference().child("Chats").child(receiverRoom)
                                    .child(messagesModel.getMessageID_receiver())
                                    .child("message")
                                    .setValue(receiverDelete);

                            messagesModel.setMessage_status("0"); //indicates that the message was deleted but we have not updated this in db yet.

                            database.getReference().child("Chats").child(senderRoom)  //updated in db
                                    .child(messagesModel.getMessageID())
                                    .child("message_status")
                                    .setValue(messagesModel.getMessage_status());

                            database.getReference().child("Chats").child(receiverRoom)
                                    .child(messagesModel.getMessageID_receiver())
                                    .child("message_status")
                                    .setValue(messagesModel.getMessage_status());  //message deleted for receiver as well
                        }).show();
            } else   //if the selected message was sent by other person
            {
                if (!messagesModel.getMessage_status().equals("0")) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setMessage("Are you sure you want to delete this message?")
                            .setPositiveButton("Delete for me", (dialog, which) -> {
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                String senderRoom = FirebaseAuth.getInstance().getUid() + recID;

                                //keep backup before deleting
                                database.getReference().child("Chats").child(senderRoom)
                                        .child(messagesModel.getMessageID())
                                        .child("deletedForMe")
                                        .setValue(messagesModel.getMessage());

                                database.getReference().child("Chats").child(senderRoom)
                                        .child(messagesModel.getMessageID())
                                        .child("message")
                                        .setValue("");

                                messagesModel.setMessage_status("0");   //message_status =0 and message="" tells that the message was deleted for me by the user

                                database.getReference().child("Chats").child(senderRoom)           //updated in db
                                        .child(messagesModel.getMessageID())
                                        .child("message_status")
                                        .setValue(messagesModel.getMessage_status());
                            }).setNegativeButton("Don't Delete", (dialog, which) -> dialog.dismiss()).show();
                }
            }

            return false;
        });

        if (holder.getClass() == SenderViewHolder.class) {   //message was sent by logged-in user

            if (!messagesModel.getMessage().equals("")) {
                if (messagesModel.getMessage_status().equals("1")) { //undeleted messages will show as usual
                    ((SenderViewHolder) (holder)).senderMsg.setText(messagesModel.getMessage());
                    @SuppressLint("SimpleDateFormat") String date = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new java.util.Date(messagesModel.getTimestamp()));
                    ((SenderViewHolder) (holder)).senderTime.setText(date);
                } else //deletedForAll messages will show as italics
                {
                    ((SenderViewHolder) (holder)).senderMsg.setText(messagesModel.getMessage());
                    ((SenderViewHolder) (holder)).senderMsg.setTypeface(null, Typeface.ITALIC);
                    ((SenderViewHolder) (holder)).senderMsg.setTextColor(Color.GRAY);
                    @SuppressLint("SimpleDateFormat") String date = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new java.util.Date(messagesModel.getTimestamp()));
                    ((SenderViewHolder) (holder)).senderTime.setText(date);
                }
            } else   //deleteForMe messages will be hardcoded
            {
                ((SenderViewHolder) (holder)).senderMsg.setText("Deleted for you");
                ((SenderViewHolder) (holder)).senderMsg.setTypeface(null, Typeface.ITALIC);
                ((SenderViewHolder) (holder)).senderMsg.setTextColor(Color.GRAY);
                @SuppressLint("SimpleDateFormat") String date = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new java.util.Date(messagesModel.getTimestamp()));
                ((SenderViewHolder) (holder)).senderTime.setText(date);
            }
        } else   //message was sent by the other user
        {
            if (!messagesModel.getMessage().equals("")) {
                if (messagesModel.getMessage_status().equals("1")) {         //undeleted messages will show as usual
                    ((ReceiverViewHolder) (holder)).receiverMsg.setText(messagesModel.getMessage());
                    @SuppressLint("SimpleDateFormat") String date = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new java.util.Date(messagesModel.getTimestamp()));
                    ((ReceiverViewHolder) (holder)).receiverTime.setText(date);
                } else  //deletedForAll messages will show as italics
                {
                    ((ReceiverViewHolder) (holder)).receiverMsg.setText(messagesModel.getMessage());
                    ((ReceiverViewHolder) (holder)).receiverMsg.setTextColor(Color.GRAY);
                    ((ReceiverViewHolder) (holder)).receiverMsg.setTypeface(null, Typeface.ITALIC);
                    @SuppressLint("SimpleDateFormat") String date = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new java.util.Date(messagesModel.getTimestamp()));
                    ((ReceiverViewHolder) (holder)).receiverTime.setText(date);
                }
            } else   //deleteForMe messages will be hardcoded
            {
                ((ReceiverViewHolder) (holder)).receiverMsg.setText("Deleted for you");
                ((ReceiverViewHolder) (holder)).receiverMsg.setTypeface(null, Typeface.ITALIC);
                ((ReceiverViewHolder) (holder)).receiverMsg.setTextColor(Color.GRAY);
                @SuppressLint("SimpleDateFormat") String date = new java.text.SimpleDateFormat("dd/MM/yyyy  HH:mm:ss").format(new java.util.Date(messagesModel.getTimestamp()));
                ((ReceiverViewHolder) (holder)).receiverTime.setText(date);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesModels.size();
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {

        TextView receiverMsg, receiverTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMsg = itemView.findViewById(R.id.receiverText);
            receiverTime = itemView.findViewById(R.id.textView4);
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {

        TextView senderMsg, senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMsg = itemView.findViewById(R.id.senderText);
            senderTime = itemView.findViewById(R.id.textView7);

        }
    }


}
