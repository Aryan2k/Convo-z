package com.example.convo_z.status;

import static com.example.convo_z.R.color.colorPrimary;
import static com.example.convo_z.R.color.colorPrimaryDark;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.convo_z.MainActivity;
import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityViewStatusBinding;
import com.example.convo_z.model.Users;
import com.example.convo_z.utils.FunctionUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class ViewStatus extends AppCompatActivity {

    ActivityViewStatusBinding binding;
    Users user;
    FirebaseDatabase database;
    int layout_width = 0;
    //    TreeSet<Integer> nonNull = new TreeSet<>();
    LayoutInflater vi;
    ViewGroup insertPoint;
    boolean isButtonLongPressed = false;
    int item_to_pick = 1;
    int counter = 0;
    int currentProgressBar = 0;
    boolean pauseTimer = false, pauseSupport = false;
    Timer a = new Timer();
    ArrayList<HashMap<String, Object>> statusList = new ArrayList<>();
    ArrayList<String> seen;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewStatusBinding.inflate(getLayoutInflater());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setNavigationBarColor(getResources().getColor(colorPrimaryDark));

        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        binding.li.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.textView15.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.linear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.textView15.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.editTextTextPersonName.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.userName.setShadowLayer(1, 0, 0, Color.BLACK);
        binding.time.setShadowLayer(1, 0, 0, Color.BLACK);

        user = (Users) getIntent().getSerializableExtra("user");

        assert user != null;
        statusList = user.getStatus();

        binding.imageView10.setOnClickListener(view -> back());

        binding.rl.setOnClickListener(view -> back());

        binding.rl.setOnLongClickListener(view -> {
            disableLayout();
            pauseTimer = true;
            isButtonLongPressed = true;
            return true;
        });

        binding.rl.setOnTouchListener((view, motionEvent) -> {
            view.onTouchEvent(motionEvent);

            if (motionEvent.getAction() == MotionEvent.ACTION_UP)  //longPress over
            {
                if (isButtonLongPressed) {
                    enableLayout();
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.usefulColor));
                    isButtonLongPressed = false;
                    pauseTimer = false;
                }
            }
            return true;
        });

        binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(
                () -> {

                    Rect r = new Rect();
                    binding.getRoot().getWindowVisibleDisplayFrame(r);
                    int screenHeight = binding.getRoot().getRootView().getHeight();
                    int keypadHeight = screenHeight - r.bottom;

                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                        // keyboard is opened
                        pauseTimer = true;
                        pauseSupport = true;
                    } else {
                        // keyboard is closed
                        if (pauseSupport) {
                            pauseTimer = false;
                            pauseSupport = false;
                        }
                    }
                });

        for (int i = statusList.size() - 1; i >= 1; i--) {

            HashMap<String, Object> hm = statusList.get(i);

            if (hm != null) {   //in case the story got deleted as soon as the status was opened

                seen = (ArrayList<String>) hm.get("seen");

                assert seen != null;

                if (!seen.contains(FirebaseAuth.getInstance().getUid()) || i == 1) {

                    item_to_pick = i;

                    Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
                    Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.imageView9);

                    binding.userName.setText(user.getUserName());

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        binding.time.setText(FunctionUtils.timeSetter((String.valueOf(hm.get("time")))));
                    }

                    binding.textView15.setText(String.valueOf(hm.get("caption")));

                    if (!seen.contains(FirebaseAuth.getInstance().getUid())) {
                        seen.add(FirebaseAuth.getInstance().getUid());
                        hm.put("seen", seen);

                        statusList.set(i, hm);
                        database.getReference().child("Users").child(user.getUserId()).child("status").setValue(statusList);
                    }
                    break;
                }
            } else {
                binding.textView15.setText(R.string.this_status_update_is_unavailable);
            }
        }

        binding.progressLinear.post(() -> {

            layout_width = binding.progressLinear.getMeasuredWidth();
            int progressBarWidth = layout_width / (statusList.size() - 1);  //minus 1 because first status is dummy.

            for (int i = 0; i < statusList.size() - 1; i++) {

                vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") View v = vi.inflate(R.layout.sample_progress_bar, null);

                if (i < item_to_pick - 1) //minus 1 first status is dummy
                {
                    ProgressBar p = (ProgressBar) v;
                    p.setProgress(100);
                }

                insertPoint = findViewById(R.id.progressLinear);
                insertPoint.addView(v, i, new ViewGroup.LayoutParams(progressBarWidth - 3, 10));

            }

            runProgressBar(item_to_pick - 1, false);
        });

        binding.buttonRight.setOnClickListener(view -> {
            pauseTimer = false;
            if (currentProgressBar + 1 < statusList.size() - 1) {
                ProgressBar p = (ProgressBar) insertPoint.getChildAt(currentProgressBar);
                p.setProgress(100);
                a.cancel();
                runProgressBar(currentProgressBar + 1, true);
            }
        });

        binding.buttonLeft.setOnClickListener(view -> {
            pauseTimer = false;
            if (currentProgressBar - 1 >= 0) {
                ProgressBar p = (ProgressBar) insertPoint.getChildAt(currentProgressBar);
                p.setProgress(0);
                a.cancel();
                runProgressBar(currentProgressBar - 1, true);
            }
        });
    }

    public void runProgressBar(int i, boolean DisplayForFirst) {
        ProgressBar p = (ProgressBar) insertPoint.getChildAt(i);
        counter = 0;
        currentProgressBar = i;
        a = new Timer();
        TimerTask b = new TimerTask() {
            @Override
            public void run() {

                if (!pauseTimer)
                    counter++;

                p.setProgress(counter);

                if (counter == 100 || DisplayForFirst) {

                    if (counter == 100)
                        a.cancel();

                    if (i + 1 <= statusList.size() - 1) {  //equal to just to update ui for the last status (do dry run for 2 viewed status)

                        if (DisplayForFirst) {
                            Executor mainExecutor = ContextCompat.getMainExecutor(getApplicationContext());
                            mainExecutor.execute(() -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    displayStatus(i + 1);  //i+1 coz first status is dummy
                                }
                            });
                        }

                        if (i + 1 < statusList.size() - 1 && counter == 100)
                            runProgressBar(i + 1, true);
                    }
                }
            }
        };
        a.schedule(b, 0, 40);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void displayStatus(int i) {
        HashMap<String, Object> hm = statusList.get(i);

        if (hm != null) {  //in case the story got deleted as soon as the status was opened
            Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
            Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.imageView9);

            binding.userName.setText(user.getUserName());
            binding.time.setText(FunctionUtils.timeSetter(String.valueOf(hm.get("time"))));
            binding.textView15.setText(String.valueOf(hm.get("caption")));

            if (!seen.contains(FirebaseAuth.getInstance().getUid())) {
                seen.add(FirebaseAuth.getInstance().getUid());
                hm.put("seen", seen);

                statusList.set(i, hm);
                database.getReference().child("Users").child(user.getUserId()).child("status").setValue(statusList);
            }
        } else {
            binding.textView15.setText(R.string.this_status_update_is_unavailable);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void disableLayout() {
        binding.li.setVisibility(View.GONE);
        binding.linear.setVisibility(View.GONE);
        FunctionUtils.hideSystemUI(binding);
        binding.progressLinear.setBackgroundColor(0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void enableLayout() {
        FunctionUtils.showSystemUI(binding);
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.li.setVisibility(View.VISIBLE);
        binding.linear.setVisibility(View.VISIBLE);
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.textView15.setTranslationY(44);
    }

    public void back() {
        Intent intent = new Intent(ViewStatus.this, MainActivity.class);
        intent.putExtra("pager", 1.2);
        intent.putExtra("progressDialog", "14");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back();
    }

}