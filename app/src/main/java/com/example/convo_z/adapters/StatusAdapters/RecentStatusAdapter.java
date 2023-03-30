package com.example.convo_z.adapters.StatusAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.R;
import com.example.convo_z.model.User;
import com.example.convo_z.viewmodel.ui.status.ViewStatusPage;
import com.example.convo_z.utils.FunctionUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class RecentStatusAdapter extends RecyclerView.Adapter<RecentStatusAdapter.ViewHolder> {

    ArrayList<User> list;
    Context context;
    User loggedInUser;
    FirebaseDatabase database;

    public RecentStatusAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_status_user, parent, false);
        return new ViewHolder(view);
    }

    @SuppressWarnings("unchecked")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecentStatusAdapter.ViewHolder holder, int position) {

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

        ArrayList<HashMap<String, Object>> s = user.getStatus();

        for (int i = 1; i < s.size(); i++) {
            HashMap<String, Object> hm = s.get(i);

            if (hm != null) {
                ArrayList<String> seen = (ArrayList<String>) hm.get("seen");

                assert seen != null;
                if (!seen.contains(FirebaseAuth.getInstance().getUid()))      //display the first unseen status coz at least one status update is unseen
                {
                    String link = String.valueOf(hm.get("link"));
                    String time = String.valueOf(hm.get("time"));
                    Picasso.get().load(link).placeholder(R.drawable.ic_user).into(holder.imageView);
                    holder.userName.setText(user.getUserName());
                    holder.time.setText(FunctionUtils.timeSetter(time));
                    break;
                }
            }
        }

        holder.itemView.setOnLongClickListener(v -> {

            String message = "New status updates from " + user.getUserName() + " won't appear under recent updates anymore.";
            new AlertDialog.Builder(context)
                    .setTitle("Mute " + user.getUserName() + "'s status updates?")
                    .setMessage(Html.fromHtml("<font color='#808080'>" + message + "</font>"))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        //update in db
                        dialog.dismiss();
                        ArrayList<String> muted = loggedInUser.getMuted();
                        muted.add(user.getUserId());
                        loggedInUser.setMuted(muted);
                        database.getReference().child("Users").child(loggedInUser.getUserId()).setValue(loggedInUser);
                    }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
            return true;
        });

        holder.itemView.setOnClickListener(view -> {
            Intent i = new Intent(context, ViewStatusPage.class);
            i.putExtra("user", user);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView userName, time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            time = itemView.findViewById(R.id.time);
        }
    }
}
