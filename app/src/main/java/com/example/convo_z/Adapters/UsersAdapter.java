package com.example.convo_z.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.ChatDetailActivity;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    ArrayList <Users> list;
    Context context;

    public UsersAdapter(ArrayList<Users> list,Context context)
    {
        this.list=list;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user,parent,false);
        return new ViewHolder(view);
    }

    //automatically updates the recyclerview when new entries are made specified by position
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Users user = list.get(position); //user here is a receiver
        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(holder.imageView);
        holder.userName.setText(user.getUserName());

        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(FirebaseAuth.getInstance().getUid() + user.getUserId())
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.hasChildren())
                        {
                            for(DataSnapshot snapshot1: snapshot.getChildren())
                            {
                             //     Log.w("aryan",snapshot1.child("message").getValue().toString());
                                holder.lastMessage.setText(snapshot1.child("message").getValue().toString());

                                //Here the datasnapshot has only one child so the loop runs only once always. OnBindViewHolder is
                                //called for all the items in the list so it updates the last messages of all the items,one in each call.
                                // It does not update last
                                //messages of all the items in one loop,rather it updates the last message of only the item specified
                                //by position in one for loop. Datasnapshot can only be referenced using a loop that's why we are using
                                //a loop or else a loop wouldn't be needed.
                                //Items in the list refers to receivers,i.e last message of conversation between the logged in user and the
                                //receiver specified by position which is stored in Users 'user'. {final Users user = list.get(position);}

                            }
                          //  Log.w("aryan","1");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, ChatDetailActivity.class);
                i.putExtra("userId",user.getUserId());
                i.putExtra("profilePic",user.getProfilePic());
                i.putExtra("userName",user.getUserName());
                context.startActivity(i);
            }
        });

    }

   @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView userName,lastMessage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userNameList);
            lastMessage = itemView.findViewById(R.id.lastMessage);

        }
    }

}
