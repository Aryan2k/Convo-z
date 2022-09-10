package com.example.convo_z.Status;

import static com.example.convo_z.R.color.colorPrimary;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.example.convo_z.Adapters.SeenAdapter;
import com.example.convo_z.MainActivity;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityOwnStatusBinding;
import com.example.convo_z.databinding.FragmentStatusBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.Executor;

public class OwnStatus extends AppCompatActivity {

    Users user;
    FirebaseDatabase database;
    int layout_width=0;
    TreeSet<Integer> nonNull = new TreeSet<>();
    LayoutInflater vi;
    ViewGroup insertPoint;
    boolean isButtonLongPressed;
    int counter=0;
    PopupWindow popupWindow;
    int currentProgressBar=0;
    int currentStatus=1;
    boolean pausetimer=false,pauseSupport=false;
    int call=1; //progressBar to call.
    Timer a=new Timer();
    ArrayList<HashMap<String,Object>> statusList = new ArrayList<>();
    ArrayList<Users> seenList = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    ActivityOwnStatusBinding binding;
    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOwnStatusBinding.inflate(getLayoutInflater());

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

        binding.textView15.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.userName.setShadowLayer(1, 0, 0, Color.BLACK);
        binding.time.setShadowLayer(1, 0, 0, Color.BLACK);

        getSupportActionBar().hide();

        user = (Users) getIntent().getSerializableExtra("user");

        assert user != null;
        statusList = user.getStatus();

     /*   binding.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = "Deleted status updates cannot be recovered.";
                new AlertDialog.Builder(OwnStatus.this)
                        .setTitle("Delete this status update?")
                        //    .setMessage(message)
                        .setMessage(Html.fromHtml("<font color='#808080'>" + message + "</font>"))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //updated in db
                                dialog.dismiss();

                                statusList.remove(currentStatus);
                                user.setStatus(statusList);
                                database.getReference().child("Users").child(user.getUserId()).setValue(user);
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });*/

        binding.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!pausetimer)
                     back();
                else
                {
                    if(popupWindow!=null)
                    popupWindow.dismiss();

                    pausetimer=false;
                }
            }
        });

        binding.newStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(OwnStatus.this,StatusPage.class);
                i.putExtra("code",8.8);
                i.putExtra("user",user);
                startActivity(i);
            }
        });

        binding.seen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!pausetimer) {
                    if (popupWindow != null)
                        popupWindow.dismiss();

                    //     binding.seen.setEnabled(false);

                    pausetimer = true;

                    HashMap<String, Object> hm = statusList.get(currentStatus);
                    ArrayList<String> seen = (ArrayList<String>) hm.get("seen");

                    RecyclerView rv = new RecyclerView(OwnStatus.this);
                    RecyclerView.LayoutParams params = new
                            RecyclerView.LayoutParams(
                            RecyclerView.LayoutParams.MATCH_PARENT,
                            RecyclerView.LayoutParams.WRAP_CONTENT
                    );

                    rv.setLayoutParams(params);
                    rv.setBackgroundColor(getResources().getColor(colorPrimary));

                    LinearLayoutManager llm = new LinearLayoutManager(OwnStatus.this);
                    SeenAdapter adapter = new SeenAdapter(seenList, getApplicationContext());
                    rv.setAdapter(adapter);
                    rv.setLayoutManager(llm);
                    rv.setVisibility(View.VISIBLE);

                    seenList.clear();

                    Users dummy = new Users();

                    String msg = "Seen by: ";
                    dummy.setUserName(msg);

                    seenList.add(dummy);

                    for (int i = 1; i < seen.size(); i++) {
                        database.getReference().child("Users").child(seen.get(i)).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Users currentUser = snapshot.getValue(Users.class);
                                seenList.add(currentUser);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            }
                        });
                    }

                    adapter.notifyDataSetChanged();
                    // create the popup window
                    int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    boolean focusable = false; // lets taps outside the popup also dismiss it
                    popupWindow = new PopupWindow(rv, width, height, focusable);

                    // show the popup window
                    // which view you pass in doesn't matter, it is only used for the window tolken
                    popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                    // dismiss the popup window when touched
                    rv.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            popupWindow.dismiss();

                            pausetimer = false;
                            return true;
                        }
                    });
                }
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

                popupWindow.dismiss();
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

        HashMap<String, Object> hm = new HashMap<>();

        for (int i =1; i < statusList.size(); i++) {
             hm = statusList.get(i);
            if (hm != null) {
                nonNull.add(i);
            }
        }

            call = nonNull.size()-1;

            Picasso.get()
                    .load(user.getProfilePic())
                    .placeholder(R.drawable.ic_user)
                    .into(binding.profileImage);

            Picasso.get()
                    .load(String.valueOf(hm.get("link")))
                    .placeholder(R.drawable.ic_user)
                    .into(binding.imageView9);

            binding.userName.setText("You");
            binding.time.setText(String.valueOf(hm.get("time")));
            binding.textView15.setText(String.valueOf(hm.get("caption")));

        binding.progressLinear.post(new Runnable() {
            @Override
            public void run() {

                layout_width = binding.progressLinear.getMeasuredWidth();
                int progressBarWidth = layout_width/nonNull.size();

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
                currentStatus=call;
                runProgressBar(call,false);
            }
        });

        binding.buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(popupWindow!=null)
                popupWindow.dismiss();

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

                if(popupWindow!=null)
                popupWindow.dismiss();

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
        if(popupWindow!=null)
        popupWindow.dismiss();

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

                        if (DisplayForFirst) {
                            Executor mainExecutor = ContextCompat.getMainExecutor(getApplicationContext());
                            mainExecutor.execute(new Runnable() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void run() {
                                    displayStatus(nonNull.ceiling(i+1));  //status[0] is dummy
                                }
                            });
                        }

                        if (i + 1 < nonNull.size() && counter==100) //last progress bar already displayed,display the corresponding status and end
                            runProgressBar(i + 1,true);
                    }
                }
            }
        };
        a.schedule(b,0,40);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void displayStatus(int i)
    {
        //  Log.d("bhaiya", String.valueOf(i));
        currentStatus=i;
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

            binding.userName.setText("You");
            binding.time.setText(String.valueOf(hm.get("time")));
            binding.textView15.setText(String.valueOf(hm.get("caption")));
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
        Intent intent = new Intent(OwnStatus.this, MainActivity.class);
        intent.putExtra("pager",1.2);
        intent.putExtra("progressDialog","14");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void hideSystemUI() {

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