package com.example.convo_z.Fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.convo_z.Adapters.StatusAdapters.MutedStatusAdapter;
import com.example.convo_z.Adapters.StatusAdapters.RecentStatusAdapter;
import com.example.convo_z.Adapters.StatusAdapters.ViewedStatusAdapter;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.Status.OwnStatus;
import com.example.convo_z.Status.StatusPage;

import com.example.convo_z.databinding.FragmentStatusBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class StatusFragment extends Fragment {

    public StatusFragment() {
        // Required empty public constructor
    }

    FragmentStatusBinding binding;
    ArrayList<Users> recentList= new ArrayList<>();
    ArrayList<Users> viewedList= new ArrayList<>();
    ArrayList<Users> mutedList= new ArrayList<>();
    FirebaseDatabase database;
    Users loggedInUser;
    FirebaseAuth auth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStatusBinding.inflate(inflater,container,false);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        //binding.mutedStatusRecyclerView.setActivated(false);
        binding.mutedStatusRecyclerView.setVisibility(View.GONE);
        binding.ll1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;

                if(loggedInUser.getStatus().size()>=1)
                    intent = new Intent(StatusFragment.this.getActivity(), OwnStatus.class);
                else
                    intent = new Intent(StatusFragment.this.getActivity(), StatusPage.class);

                intent.putExtra("user",loggedInUser);
                startActivity(intent);
            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                 loggedInUser = snapshot.getValue(Users.class);

                Picasso.get()
                        .load(loggedInUser.getProfilePic())
                        .placeholder(R.drawable.ic_user)
                        .into(binding.profileImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final RecentStatusAdapter recentStatusAdapter = new RecentStatusAdapter(recentList,getContext());
        final ViewedStatusAdapter viewedStatusAdapter = new ViewedStatusAdapter(viewedList,getContext());
        final MutedStatusAdapter mutedStatusAdapter = new MutedStatusAdapter(mutedList,getContext());

        binding.recentStatusRecyclerView.setAdapter(recentStatusAdapter);
        binding.viewedStatusRecyclerView.setAdapter(viewedStatusAdapter);
        binding.mutedStatusRecyclerView.setAdapter(mutedStatusAdapter);

        LinearLayoutManager recentLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager viewedLayoutManager = new LinearLayoutManager(getContext());
        LinearLayoutManager mutedLayoutManager = new LinearLayoutManager(getContext());

        binding.recentStatusRecyclerView.setLayoutManager(recentLayoutManager);
        binding.viewedStatusRecyclerView.setLayoutManager(viewedLayoutManager);
        binding.mutedStatusRecyclerView.setLayoutManager(mutedLayoutManager);

        binding.muted.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
               handleMuted(0);
            }
        });

           //loggedInUser isn't being used before this point coz it hasn't loaded info from db yet which causes crashes.
            database.getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                ArrayList<String> contacts= new ArrayList<>();

             //   Log.d("aryan", "onDataChange: ");

                Cursor phones = null;
                try {
                    String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
                    phones = getContext().getContentResolver().query
                            (ContactsContract.CommonDataKinds.Phone.CONTENT_URI,  null, SELECTION, null, null);
                }catch(Exception e)
                {
                    Toast.makeText(getContext(),"An error occurred,please try again",Toast.LENGTH_SHORT).show();
                }

                while (phones.moveToNext() && phones != null && phones.getCount() > 0) {
                    //  String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneNumber = phoneNumber.replaceAll("\\s","");

                    if(phoneNumber.length()>=10) {

                        if  (phoneNumber.length() == 10) {
                            phoneNumber = "+91"+phoneNumber; //gets the last 10 characters of string
                        }
                        else if(phoneNumber.startsWith("91"))
                        {
                            phoneNumber = "+"+phoneNumber;
                        }
                        contacts.add(phoneNumber);
                    }
                }

                phones.close();
                viewedList.clear();
                recentList.clear();
                mutedList.clear();

                if(loggedInUser.getStatus().size()>1) //1 becoz 0th entry is dummy
                {
                    ArrayList<HashMap<String,Object>> status = loggedInUser.getStatus();
                    HashMap<String,Object> hmap = null;

                    for (int i=status.size()-1; i>=1; i--)
                    {
                        hmap = status.get(i);

                        if(hmap!=null)
                        {
                            break;
                        }
                    }

                    binding.userNameList.setText("My status");
                    assert hmap != null;
                    String time = (String) hmap.get("time");
                    binding.lastMessage.setText(time);
                    binding.profileImage.setBorderColor(Color.WHITE);
                    binding.plus.setVisibility(View.GONE);
                    binding.profileImage.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.mystatusborder));
                }

                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                      Users user = dataSnapshot.getValue(Users.class);
                      assert user != null;

                    if (user.getPhoneNumber() != null) {
                        if (!FirebaseAuth.getInstance().getUid().equals(user.getUserId()) && contacts.contains(user.getPhoneNumber()) &&
                                !user.getHidden().contains(loggedInUser.getUserId())) {

                       //     Log.d("hillo", loggedInUser.getHidden().toString());
                            ArrayList<HashMap<String, Object>> s = user.getStatus();

                            if (s.size() > 1) {
                                HashMap<String, Object> hm = s.get(s.size() - 1); //if the last story is seen,all the stories are seen.
                                ArrayList<String> seen = (ArrayList<String>) hm.get("seen");

                                Log.d("aryan", loggedInUser.getMuted().toString());
                                assert seen != null;

                                if (loggedInUser.getMuted().contains(user.getUserId())) {
                                    mutedList.add(user);
                                    handleMuted(1);
                                } else {
                                    if (!seen.contains(loggedInUser.getUserId())) {
                                        recentList.add(user);
                                    } else {
                                        viewedList.add(user);
                                    }
                                }
                            }
                        }
                    }
                }

                if(recentList.isEmpty())
                    binding.recent.setText("No recent updates");
                else
                    binding.recent.setText("New updates");

                if(viewedList.isEmpty())
                    binding.seen.setText("No viewed updates");
                else
                    binding.seen.setText("Viewed updates");

                if(mutedList.isEmpty()){
                    binding.muted.setText("No muted updates");
                    binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                }
                else
                {
                    binding.muted.setText("Muted updates");
                    binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_keyboard_arrow_down_24) , null);
                }

                recentStatusAdapter.notifyDataSetChanged(); //notifies the adapter that a change has been made (onBindViewHolder)
                viewedStatusAdapter.notifyDataSetChanged();
                mutedStatusAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return binding.getRoot();
    }

     void handleMuted(int calledFrom)
     {
         if (!mutedList.isEmpty()) {   //dont view up/down arrow on tapping muted if mutedlist is empty

             if (binding.mutedStatusRecyclerView.getVisibility() != View.VISIBLE && calledFrom!=1) {
                 binding.mutedStatusRecyclerView.setVisibility(View.VISIBLE);
                 // binding.scrollview.fullScroll(View.FOCUS_DOWN);

                 binding.scrollview.post(new Runnable() {
                     @Override
                     public void run() {
                         binding.scrollview.fullScroll(ScrollView.FOCUS_DOWN);
                     }
                 });

                 binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_keyboard_arrow_up_24), null);
             } else {
                 binding.mutedStatusRecyclerView.setVisibility(View.GONE);
                 //     binding.scrollview.fullScroll(View.FOCUS_UP);
                 binding.muted.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_keyboard_arrow_down_24), null);
             }
         }
     }
}