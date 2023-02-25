package com.example.convo_z.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.R;
import com.example.convo_z.model.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SeenAdapter extends RecyclerView.Adapter<SeenAdapter.ViewHolder> {
    ArrayList<Users> list;
    Context context;

    public SeenAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public SeenAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_seen_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeenAdapter.ViewHolder holder, int position) {

        final Users user = list.get(position);

        String msg = "Seen by: ";
        Typeface typeface = ResourcesCompat.getFont(context, R.font.poppins);
        holder.userName.setTextSize(15);
        holder.userName.setTypeface(typeface);

        if (user.getUserName().equals(msg)) {
            holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(600, 100));
            holder.userName.setGravity(Gravity.CENTER_HORIZONTAL);
            if (list.size() > 1) {
                holder.userName.setText(msg);
            } else {
                holder.userName.setText(R.string.no_views_yet);
            }
            holder.imageView.setVisibility(View.GONE);
            holder.current.setVisibility(View.GONE);
        } else {
            Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(holder.imageView);
            holder.userName.setText(user.getUserName());
            holder.current.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView userName, current;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            current = itemView.findViewById(R.id.current);  //no significance as of now

        }
    }
}
