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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final Users user = list.get(position);
        Picasso.get().load(user.getProfilepic()).placeholder(R.drawable.ic_user).into(holder.imageView);
        holder.userName.setText(user.getUserName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(context, ChatDetailActivity.class);
                i.putExtra("userId",user.getUserId());
                i.putExtra("profilePic",user.getProfilepic());
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
