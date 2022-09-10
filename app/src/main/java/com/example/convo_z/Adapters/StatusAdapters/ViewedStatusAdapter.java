package com.example.convo_z.Adapters.StatusAdapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.Status.ViewStatus;
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

public class ViewedStatusAdapter extends RecyclerView.Adapter<ViewedStatusAdapter.ViewHolder>{

    ArrayList<Users> list;
    Context context;
    Users loggedInUser;
    FirebaseDatabase database;

    public ViewedStatusAdapter(ArrayList<Users> list, Context context)
    {
        this.list=list;
        this.context=context;
    }

    @NonNull
    @Override
    public ViewedStatusAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewed_status_user,parent,false);
        return new ViewedStatusAdapter.ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewedStatusAdapter.ViewHolder holder, int position) {

        final Users user = list.get(position);

        database= FirebaseDatabase.getInstance();

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loggedInUser = snapshot.getValue(Users.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       // Log.d("hi",loggedInUser.getUserId());

        ArrayList<HashMap<String, Object>> s = user.getStatus();

            HashMap<String, Object> hm = s.get(s.size()-1);

                String link = String.valueOf(hm.get("link"));
                String time = String.valueOf(hm.get("time"));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
        Date sdate = new Date(Long.parseLong(time));
        Date eDate = new Date(new Date().getTime());

        String startTime = simpleDateFormat.format(sdate);;
        String endTime = simpleDateFormat.format(eDate);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

        LocalTime start = LocalTime.parse(startTime, timeFormatter);
        LocalTime end = LocalTime.parse(endTime, timeFormatter);
        Duration diff = Duration.between(start, end);

        long hours = diff.toHours();
        long minutes = diff.minusHours(hours).toMinutes();
        String totalTimeString = String.format("%02d:%02d", hours, minutes);

        Log.d("samay","TotalTime in Hours and Mins Format is " + totalTimeString);

        String result="";

        if(hours<1)
        {
            if(minutes==0)
                result+="Just Now";
            else if(minutes==1)
                result+="A minute ago";
            else
                result+= minutes +" minutes ago";
        }
        else {
            if (start.isAfter(end))
                result += "Yesterday, " + start.getHour() + ":" + start.getMinute();
            else
                result += "Today, " + start.getHour() + ":" + start.getMinute();;
        }

                Picasso.get().load(link).placeholder(R.drawable.ic_user).into(holder.imageView);
                holder.userName.setText(user.getUserName());
                holder.time.setText(result);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                String message = "New status updates from "+user.getUserName()+" won't appear under recent updates anymore.";
                new AlertDialog.Builder(context)
                        .setTitle("Mute " + user.getUserName()+"'s status updates?")
                        //    .setMessage(message)
                        .setMessage(Html.fromHtml("<font color='#808080'>" + message + "</font>"))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //updated in db
                                dialog.dismiss();

                                ArrayList<String> muted = loggedInUser.getMuted();
                                muted.add(user.getUserId());
                                loggedInUser.setMuted(muted);
                                database.getReference().child("Users").child(loggedInUser.getUserId()).setValue(loggedInUser);

                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return true;
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(context, ViewStatus.class);
                i.putExtra("user",user);
            //    i.putExtra("code",1);
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
        TextView userName,time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            time = itemView.findViewById(R.id.time);

        }
    }
}
