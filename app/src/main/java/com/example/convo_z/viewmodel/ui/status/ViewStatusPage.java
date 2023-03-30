package com.example.convo_z.viewmodel.ui.status;

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
import androidx.lifecycle.ViewModelProvider;

import com.example.convo_z.R;
import com.example.convo_z.model.User;
import com.example.convo_z.repository.StatusRepository;
import com.example.convo_z.viewmodel.ui.home.HomeActivity;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.viewmodel.status.ViewStatusViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ViewStatusPage extends AppCompatActivity {

    ActivityViewStatusBinding binding;
    User user;
    private ViewStatusViewModel viewModel;
    int layout_width = 0;
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

    @SuppressWarnings("unchecked")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(ViewStatusViewModel.class);
        new StatusRepository(); // to initialize firebase database instance
        loadUI();
        setUpListeners();
        user = (User) getIntent().getSerializableExtra("user");
        assert user != null;
        statusList = user.getStatus();

        for (int i = statusList.size() - 1; i >= 1; i--) {

            HashMap<String, Object> hm = statusList.get(i);

            if (hm != null) {   // in case the story got deleted as soon as the status was opened

                seen = (ArrayList<String>) hm.get("seen");
                assert seen != null;
                if (!seen.contains(FirebaseAuth.getInstance().getUid()) || i == 1) {

                    item_to_pick = i;

                    Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
                    Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.statusImage);
                    binding.userName.setText(user.getUserName());
                    binding.captionTxt.setText(String.valueOf(hm.get("caption")));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        binding.time.setText(FunctionUtils.timeSetter((String.valueOf(hm.get("time")))));
                    }

                    if (!seen.contains(FirebaseAuth.getInstance().getUid())) {
                        seen.add(FirebaseAuth.getInstance().getUid());
                        hm.put("seen", seen);
                        statusList.set(i, hm);
                        viewModel.updateSeenList(user.getUserId(), statusList);
                    }
                    break;
                }
            } else {
                binding.captionTxt.setText(R.string.this_status_update_is_unavailable);
            }
        }

        binding.progressLinear.post(() -> {
            layout_width = binding.progressLinear.getMeasuredWidth();
            int progressBarWidth = layout_width / (statusList.size() - 1);  // minus 1 as first status is dummy.
            for (int i = 0; i < statusList.size() - 1; i++) {
                vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") View v = vi.inflate(R.layout.sample_progress_bar, null);
                if (i < item_to_pick - 1) // minus 1 as first status is dummy
                {
                    ProgressBar p = (ProgressBar) v;
                    p.setProgress(100);
                }
                insertPoint = findViewById(R.id.progressLinear);
                insertPoint.addView(v, i, new ViewGroup.LayoutParams(progressBarWidth - 3, 10));
            }
            runProgressBar(item_to_pick - 1, false);
        });
    }

    private void loadUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(colorPrimaryDark));
        }
        binding.li.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.captionTxt.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.linear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.captionTxt.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.replyMsgTxt.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.userName.setShadowLayer(1, 0, 0, Color.BLACK);
        binding.time.setShadowLayer(1, 0, 0, Color.BLACK);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpListeners() {
        binding.homeImg.setOnClickListener(view -> back());
        binding.rl.setOnClickListener(view -> back());
        binding.rl.setOnLongClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                disableLayout();
            }
            pauseTimer = true;
            isButtonLongPressed = true;
            return true;
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
        binding.buttonRight.setOnClickListener(view -> {
            pauseTimer = false;
            if (currentProgressBar + 1 < statusList.size() - 1) {
                ProgressBar p = (ProgressBar) insertPoint.getChildAt(currentProgressBar);
                p.setProgress(100);
                a.cancel();
                runProgressBar(currentProgressBar + 1, true);
            }
        });
        binding.rl.setOnTouchListener((view, motionEvent) -> {
            view.onTouchEvent(motionEvent);
            if (motionEvent.getAction() == MotionEvent.ACTION_UP)  // longPress over
            {
                if (isButtonLongPressed) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        enableLayout();
                        getWindow().setNavigationBarColor(getResources().getColor(R.color.usefulColor));
                    }
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
                    if (i + 1 <= statusList.size() - 1) {  // equal to just to update ui for the last status (do dry run for 2 viewed status)
                        if (DisplayForFirst) {
                            Executor mainExecutor = ContextCompat.getMainExecutor(getApplicationContext());
                            mainExecutor.execute(() -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    displayStatus(i + 1);  // i+1 coz first status is dummy
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

        if (hm != null) {  // in case the story got deleted as soon as the status was opened
            Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
            Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.statusImage);
            binding.userName.setText(user.getUserName());
            binding.time.setText(FunctionUtils.timeSetter(String.valueOf(hm.get("time"))));
            binding.captionTxt.setText(String.valueOf(hm.get("caption")));

            if (!seen.contains(FirebaseAuth.getInstance().getUid())) {
                seen.add(FirebaseAuth.getInstance().getUid());
                hm.put("seen", seen);
                statusList.set(i, hm);
                viewModel.updateSeenList(user.getUserId(), statusList);
            }
        } else {
            binding.captionTxt.setText(R.string.this_status_update_is_unavailable);
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
        binding.captionTxt.setTranslationY(44);
    }

    public void back() {
        Intent intent = new Intent(ViewStatusPage.this, HomeActivity.class);
        intent.putExtra("pager", 1.2);
        intent.putExtra("progressDialog", "14");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back();
    }

}