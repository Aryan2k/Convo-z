package com.example.convo_z.adapters.StatusAdapters;

import android.annotation.SuppressLint;
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

public class MutedStatusAdapter extends RecyclerView.Adapter<MutedStatusAdapter.ViewHolder> {

    ArrayList<User> list;
    Context context;
    User loggedInUser;
    FirebaseDatabase database;
    MutedStatusAdapter.ViewHolder holder;
    User user = new User();

    public MutedStatusAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MutedStatusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.muted_status_user, parent, false);
        return new ViewHolder(view);
    }

    @SuppressWarnings("unchecked")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MutedStatusAdapter.ViewHolder Holder, @SuppressLint("RecyclerView") int position) {

        user = list.get(position);
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

        holder = Holder;

        ArrayList<HashMap<String, Object>> s = user.getStatus();

        for (int i = 1; i < s.size(); i++) {
            HashMap<String, Object> hm = s.get(i);
            if (hm != null) {
                ArrayList<String> seen = (ArrayList<String>) hm.get("seen");
                assert seen != null;
                if (!seen.contains(FirebaseAuth.getInstance().getUid())) {        // display the first unseen status
                    display(hm);
                    break;
                }
                if (i == s.size() - 1) {        // all status updates are viewed. display the first one.
                    hm = s.get(1);
                    display(hm);
                }
            }
        }

        holder.itemView.setOnLongClickListener(v -> {

            String message = "New status updates from " + user.getUserName() + " will appear under recent updates.";
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.unmute) + user.getUserName() + "'s status updates?")
                    .setMessage(Html.fromHtml("<font color='#808080'>" + message + "</font>"))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // update in db
                        dialog.dismiss();

                        ArrayList<String> muted = loggedInUser.getMuted();
                        muted.remove(user.getUserId());
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

    private void display(HashMap<String, Object> hm) {
        String link = String.valueOf(hm.get("link"));
        String time = String.valueOf(hm.get("time"));

        Picasso.get().load(link).placeholder(R.drawable.ic_user).into(holder.imageView);
        holder.userName.setText(user.getUserName());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.time.setText(FunctionUtils.timeSetter(time));
        }
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
