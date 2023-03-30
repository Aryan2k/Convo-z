package com.example.convo_z.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.model.User;
import com.example.convo_z.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class StatusPrivacyAdapter extends RecyclerView.Adapter<StatusPrivacyAdapter.ViewHolder> {

    ArrayList<User> list;
    Context context;
    User loggedInUser;
    FirebaseDatabase database;
    ArrayList<String> hidden;

    public StatusPrivacyAdapter(ArrayList<User> list, Context context, ArrayList<String> hidden) {
        this.list = list;
        this.context = context;
        this.hidden = hidden;
    }

    @NonNull
    @Override
    public StatusPrivacyAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_privacy_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatusPrivacyAdapter.ViewHolder holder, int position) {

        final User user = list.get(position);
        database = FirebaseDatabase.getInstance();

        database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loggedInUser = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //hidden = loggedInUser.getHidden();   crashes because db load into loggedInUser hasn't finished yet

        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(holder.imageView);
        holder.userName.setText(user.getUserName());

        if (hidden.contains(user.getUserId())) {
            holder.current.setText(R.string.this_user_cant_see_your_status_updates);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.red));
        } else {
            holder.current.setText(R.string.this_user_can_see_your_status_updates);
            holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.green));
        }

        holder.itemView.setOnClickListener(view -> {
            if (!hidden.contains(user.getUserId())) {
                hidden.add(user.getUserId());

                database.getReference().child("Users").child(loggedInUser.getUserId()).child("hidden").setValue(hidden);

                holder.current.setText(R.string.this_user_cant_see_your_status_updates);
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.green));
            } else {
                hidden.remove(user.getUserId());

                database.getReference().child("Users").child(loggedInUser.getUserId()).child("hidden").setValue(hidden);

                holder.current.setText(R.string.this_user_can_see_your_status_updates);
                holder.itemView.setBackgroundColor(context.getResources().getColor(R.color.red));
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView; //eye,eyeCrossed;
        TextView userName, current;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            current = itemView.findViewById(R.id.current);
        }
    }
}
