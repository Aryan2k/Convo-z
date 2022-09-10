package com.example.convo_z.Status;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
import static com.example.convo_z.R.color.colorPrimary;
import static com.example.convo_z.R.color.colorPrimaryDark;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;

import com.example.convo_z.MainActivity;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityViewStatusBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.Executor;

public class ViewStatus extends AppCompatActivity {

    ActivityViewStatusBinding binding;
    Users user;
    FirebaseDatabase database;
    int layout_width=0;
    TreeSet<Integer> nonNull = new TreeSet<>();
    LayoutInflater vi;
    ViewGroup insertPoint;
    boolean isButtonLongPressed;
    int counter=0;
    int currentProgressBar=0;
    boolean pausetimer=false,pauseSupport=false;
    int call=1;
    Timer a=new Timer();
    ArrayList<HashMap<String,Object>> statusList = new ArrayList<>();
    ArrayList<String> seen;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityViewStatusBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setContentView(binding.getRoot());

        database= FirebaseDatabase.getInstance();

        binding.li.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.textView15.setBackgroundColor(getResources().getColor(colorPrimary));
      //  binding.lii.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.linear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
        //binding.editTextTextPersonName.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
     //   binding.lii.setVisibility(View.GONE);

        binding.textView15.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.editTextTextPersonName.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.userName.setShadowLayer(1, 0, 0, Color.BLACK);
        binding.time.setShadowLayer(1, 0, 0, Color.BLACK);

        getSupportActionBar().hide();

           user = (Users) getIntent().getSerializableExtra("user");

           assert user != null;
           statusList = user.getStatus();

           binding.rl.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   back();
               }
           });

           isButtonLongPressed = false;

           binding.rl.setOnLongClickListener(new View.OnLongClickListener() {
               @Override
               public boolean onLongClick(View view) {
                   disableLayout();
                   pausetimer=true;
                   isButtonLongPressed=true;
                   return true;
               }
           });

       binding.rl.setOnTouchListener(new View.OnTouchListener() {
               @SuppressLint("ClickableViewAccessibility")
               @Override
               public boolean onTouch(View view, MotionEvent motionEvent) {
                   view.onTouchEvent(motionEvent);

                   if(motionEvent.getAction()==MotionEvent.ACTION_UP)  //longPress over
                   {
                       if(isButtonLongPressed)
                       {
                           enableLayout();
                           getWindow().setNavigationBarColor(getResources().getColor(R.color.usefulColor));
                           isButtonLongPressed=false;
                           pausetimer=false;
                       }
                   }
                   return true;
               }
           });

             binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        binding.getRoot().getWindowVisibleDisplayFrame(r);
                        int screenHeight = binding.getRoot().getRootView().getHeight();

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        int keypadHeight = screenHeight - r.bottom;

                        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                            // keyboard is opened
                           pausetimer=true;
                           pauseSupport=true;
                        }
                        else {
                            // keyboard is closed
                           if(pauseSupport) {
                               pausetimer = false;
                               pauseSupport = false;
                           }
                        }
                    }
                });

           boolean check=false,checker=true;

           int item_to_pick=1; //list item to display (coz there can be null(deleted) items)

            for (int i = 1; i < statusList.size(); i++) {

                HashMap<String, Object> hm = statusList.get(i);

                if (hm != null) {

                    nonNull.add(i);  //since one or more status in between may be deleted by the user

                    if(checker) {
                        call = 0;  //progress bar index to run
                        item_to_pick=i;
                        checker=false;  //helps checking for first nonnull status so that it can be viewed if all the status are already seen
                    }

                //    Log.d("mj", String.valueOf(call));

                    seen = (ArrayList<String>) hm.get("seen");

                    assert seen != null;
                    if (!seen.contains(FirebaseAuth.getInstance().getUid()) && !check) {

                        if(!check) //to display the first unseen status
                            call=nonNull.size()-1;

                        check=true;  //check if there are any seen status or not

                        Picasso.get()
                                .load(user.getProfilePic())
                                .placeholder(R.drawable.ic_user)
                                .into(binding.profileImage);

                        Picasso.get()
                                .load(String.valueOf(hm.get("link")))
                                .placeholder(R.drawable.ic_user)
                                .into(binding.imageView9);

                        binding.userName.setText(user.getUserName());
                        binding.time.setText(String.valueOf(hm.get("time")));
                        binding.textView15.setText(String.valueOf(hm.get("caption")));

                          seen.add(FirebaseAuth.getInstance().getUid());
                          hm.put("seen", seen);

                          statusList.set(i, hm);
                          database.getReference().child("Users").child(user.getUserId()).child("status").setValue(statusList);

                    }
                }
            }

            if(!check) //all status seen so display the first nonnull status
            {
                HashMap<String, Object> hm = statusList.get(item_to_pick);

                Picasso.get()
                        .load(user.getProfilePic())
                        .placeholder(R.drawable.ic_user)
                        .into(binding.profileImage);

                Picasso.get()
                        .load(String.valueOf(hm.get("link")))
                        .placeholder(R.drawable.ic_user)
                        .into(binding.imageView9);

                binding.userName.setText(user.getUserName());
                binding.time.setText(String.valueOf(hm.get("time")));
                binding.textView15.setText(String.valueOf(hm.get("caption")));

            //    runProgressBar(first_nonnull);
            }

        binding.progressLinear.post(new Runnable() {
            @Override
            public void run() {

                 layout_width = binding.progressLinear.getMeasuredWidth();
                 int progressBarWidth = layout_width/nonNull.size();

              //  Log.d("aryan", String.valueOf(progressBarWidth));

                for(int i=0; i<nonNull.size(); i++) {

                    vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = vi.inflate(R.layout.sample_progress_bar, null);

                    if(i<call)
                    {
                        ProgressBar p = (ProgressBar) v;
                        p.setProgress(100);
                    }

                    insertPoint = (ViewGroup) findViewById(R.id.progressLinear);
                    insertPoint.addView(v, i, new ViewGroup.LayoutParams(progressBarWidth-3, 10));

                }

              //  Log.d("brother", String.valueOf(call));
                runProgressBar(call,false);
            }
        });

            binding.buttonRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pausetimer=false;
                    if(currentProgressBar+1<nonNull.size()) {
                        ProgressBar p = (ProgressBar) insertPoint.getChildAt(currentProgressBar);
                        p.setProgress(100);
                        a.cancel();
                        runProgressBar(currentProgressBar + 1, true);
                    }
                }
            });

            binding.buttonLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    pausetimer=false;
                    if(currentProgressBar-1>=0) {
                        ProgressBar p = (ProgressBar) insertPoint.getChildAt(currentProgressBar);
                        p.setProgress(0);
                        a.cancel();
                        runProgressBar(currentProgressBar - 1, true);
                    }
                }
            });
    }

    public void runProgressBar(int i,boolean DisplayForFirst)
    {
        ProgressBar p = (ProgressBar) insertPoint.getChildAt(i);
        counter=0;
        currentProgressBar=i;
        a = new Timer();
        TimerTask b = new TimerTask() {
            @Override
            public void run() {

                if(!pausetimer)
                    counter++;

                p.setProgress(counter);

                if(counter == 100 || DisplayForFirst) {

                    if(counter==100)
                        a.cancel();

                    if(i+1<=nonNull.size()) {  //equal to just to update ui for last status (do dry run for 2 viewed status)
                      //  Log.d("bhaisab", String.valueOf(i));
                      //  Log.d("bhaiyar", String.valueOf(nonNull.size()));

                        if (DisplayForFirst) {
                            Executor mainExecutor = ContextCompat.getMainExecutor(getApplicationContext());
                            mainExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                //    Log.d("bhai", String.valueOf(i));
                                    displayStatus(nonNull.ceiling(i+1));  //status[0] is dummy
                                }
                            });
                        }

                        if (i + 1 < nonNull.size() && counter==100) //last progress bar already displayed,display the corresponding status and end
                            runProgressBar(i + 1,true);
                    //    else
                        //   back();
                    }
                }
            }
        };
        a.schedule(b,0,40);
    }

    public void displayStatus(int i)
    {
      //  Log.d("bhaiya", String.valueOf(i));
        HashMap<String, Object> hm = statusList.get(i);

        if(hm!=null) {  //in case the story got deleted as soon as the status was opened
            Picasso.get()
                    .load(user.getProfilePic())
                    .placeholder(R.drawable.ic_user)
                    .into(binding.profileImage);

            Picasso.get()
                    .load(String.valueOf(hm.get("link")))
                    .placeholder(R.drawable.ic_user)
                    .into(binding.imageView9);

            binding.userName.setText(user.getUserName());
            binding.time.setText(String.valueOf(hm.get("time")));
            binding.textView15.setText(String.valueOf(hm.get("caption")));

            if (!seen.contains(FirebaseAuth.getInstance().getUid())) {
                seen.add(FirebaseAuth.getInstance().getUid());
                hm.put("seen", seen);

                statusList.set(i, hm);
                database.getReference().child("Users").child(user.getUserId()).child("status").setValue(statusList);
            }
        }
        else {
            binding.textView15.setText("This status update is unavailable.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void disableLayout()
    {
        binding.li.setVisibility(View.GONE);
        binding.linear.setVisibility(View.GONE);
        hideSystemUI();
        binding.progressLinear.setBackgroundColor(0);
       // getWindow().setNavigationBarColor(getResources().getColor(R.color.usefulColor));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void enableLayout()
    {
        showSystemUI();

        binding.li.setVisibility(View.VISIBLE);
        binding.linear.setVisibility(View.VISIBLE);
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
       // binding.linear.setTranslationY(-84);
        binding.textView15.setTranslationY(44);

       // getWindow().setNavigationBarColor(getResources().getColor(R.color.usefulColor));
    }

    public void back()
    {
        Intent intent = new Intent(ViewStatus.this, MainActivity.class);
        intent.putExtra("pager",1.2);
        intent.putExtra("progressDialog","14");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        binding.getRoot().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showSystemUI() {
        binding.getRoot().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
       // binding.lii.setVisibility(View.VISIBLE);
       // getWindow().setNavigationBarColor(getResources().getColor(colorPrimary));
    }
}