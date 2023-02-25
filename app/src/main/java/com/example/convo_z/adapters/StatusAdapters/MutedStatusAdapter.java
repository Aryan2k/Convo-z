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

import com.example.convo_z.model.Users;
import com.example.convo_z.R;
import com.example.convo_z.status.ViewStatus;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class MutedStatusAdapter extends RecyclerView.Adapter<MutedStatusAdapter.ViewHolder> {

    ArrayList<Users> list;
    Context context;
    Users loggedInUser;
    FirebaseDatabase database;
    MutedStatusAdapter.ViewHolder holder;
    Users user = new Users();
    int last_nonnull = 1;

    public MutedStatusAdapter(ArrayList<Users> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MutedStatusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.muted_status_user, parent, false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MutedStatusAdapter.ViewHolder Holder, @SuppressLint("RecyclerView") int position) {

        user = list.get(position);
        database = FirebaseDatabase.getInstance();

        database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loggedInUser = snapshot.getValue(Users.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder = Holder;

        ArrayList<HashMap<String, Object>> s = user.getStatus();

        if (!display(1, s.size(), false))//if all status are viewed. display the first one.
        {
            display(last_nonnull, last_nonnull + 1, true); //to display the last nonnull status
        }

        holder.itemView.setOnLongClickListener(v -> {

            String message = "New status updates from " + user.getUserName() + " will appear under recent updates.";

            new AlertDialog.Builder(context)
                    .setTitle("Unmute " + user.getUserName() + "'s status updates?")
                    //   .setMessage(message)
                    .setMessage(Html.fromHtml("<font color='#808080'>" + message + "</font>"))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        //updated in db
                        dialog.dismiss();

                        ArrayList<String> muted = loggedInUser.getMuted();
                        muted.remove(user.getUserId());
                        loggedInUser.setMuted(muted);
                        database.getReference().child("Users").child(loggedInUser.getUserId()).setValue(loggedInUser);

                        //      notifyDataSetChanged();
                    }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
            return true;
        });

        holder.itemView.setOnClickListener(view -> {

            Intent i = new Intent(context, ViewStatus.class);
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean display(int j, int k, boolean check) {
        ArrayList<HashMap<String, Object>> s = user.getStatus();

        boolean run = false;

        for (int i = j; i < k; i++) {
            HashMap<String, Object> hm = s.get(i);

            if (hm != null) {

                last_nonnull = i; //in case all status are viewed display the last one

                ArrayList<String> seen = (ArrayList<String>) hm.get("seen");

                assert seen != null;
                if (!seen.contains(FirebaseAuth.getInstance().getUid()) || check)      //display the last unseen status
                {                                     //if check=true, all status are already viewed and the last one is displayed
                    run = true;

                    String link = String.valueOf(hm.get("link"));
                    String time = String.valueOf(hm.get("time"));

                    @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                    Date sDate = new Date(Long.parseLong(time));
                    Date eDate = new Date(new Date().getTime());

                    String startTime = simpleDateFormat.format(sDate);
                    String endTime = simpleDateFormat.format(eDate);

                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

                    LocalTime start = LocalTime.parse(startTime, timeFormatter);
                    LocalTime end = LocalTime.parse(endTime, timeFormatter);
                    Duration diff = Duration.between(start, end);

                    long hours = Math.abs(diff.toHours());
                    long minutes = Math.abs(diff.minusHours(hours).toMinutes());
                    //  @SuppressLint("DefaultLocale") String totalTimeString = String.format("%02d:%02d", hours, minutes);

                    String result = "";

                    if (hours < 1) {
                        if (minutes == 0)
                            result += "Just Now";
                        else if (minutes == 1)
                            result += "A minute ago";
                        else
                            result += minutes + " minutes ago";
                    } else {
                        String minute;
                        String hour;

                        int min = start.getMinute();
                        if (min < 10) {
                            minute = "0" + min;
                        } else {
                            minute = String.valueOf(min);
                        }

                        int hr = start.getHour();
                        if (hr < 10) {
                            hour = "0" + hr;
                        } else {
                            hour = String.valueOf(hr);
                        }

                        if (start.isAfter(end))
                            result += "Yesterday, " + hour + ":" + minute;
                        else
                            result += "Today, " + hour + ":" + minute;
                    }

                    Picasso.get().load(link).placeholder(R.drawable.ic_user).into(holder.imageView);
                    holder.userName.setText(user.getUserName());
                    holder.time.setText(result);
                    break;
                }
            }
        }
        return run;
    }
}
