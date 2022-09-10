package com.example.convo_z.Adapters;

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

import com.example.convo_z.Adapters.StatusAdapters.RecentStatusAdapter;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SeenAdapter extends RecyclerView.Adapter<SeenAdapter.ViewHolder> {
    ArrayList <Users> list;
    Context context;

    public SeenAdapter(ArrayList<Users> list, Context context)
    {
        this.list=list;
        this.context=context;
    }

    @NonNull
    @Override
    public SeenAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_seen_user,parent,false);
        return new SeenAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SeenAdapter.ViewHolder holder, int position) {

        final Users user = list.get(position);

        String msg = "Seen by: ";

        if(user.getUserName().equals(msg))
        {

            Typeface typeface = ResourcesCompat.getFont(context, R.font.amethysta);

            holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(500,90));
            holder.userName.setText(msg);
            holder.userName.setTextSize(15);
            holder.userName.setTypeface(typeface);
       //     holder.userName.setGravity(Gravity.CENTER_HORIZONTAL);
            holder.userName.setTranslationX(210);
            holder.userName.setTranslationY(7);
            holder.imageView.setVisibility(View.GONE);
            holder.current.setVisibility(View.GONE);
        }
        else {
            Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(holder.imageView);
            holder.userName.setText(user.getUserName());
            holder.current.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView userName,current;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            current = itemView.findViewById(R.id.current);  //no significance as of now

        }
    }
}
