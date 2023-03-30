package com.example.convo_z.viewmodel.ui.status;

import static com.example.convo_z.R.color.colorPrimary;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.R;
import com.example.convo_z.adapters.SeenListAdapter;
import com.example.convo_z.databinding.ActivityOwnStatusBinding;
import com.example.convo_z.model.User;
import com.example.convo_z.viewmodel.ui.home.HomeActivity;
import com.example.convo_z.utils.Data;
import com.example.convo_z.utils.FunctionUtils;
import com.example.convo_z.utils.Resource;
import com.example.convo_z.viewmodel.status.OwnStatusViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OwnStatusPage extends AppCompatActivity {

    User user;
    private OwnStatusViewModel viewModel;
    int layout_width = 0;
    LayoutInflater vi;
    ViewGroup insertPoint;
    boolean isButtonLongPressed;
    int counter = 0;
    PopupWindow popupWindow;
    int currentProgressBar = 0;
    int currentStatus = 1;
    boolean pauseTimer = false, pauseSupport = false;
    Timer a = new Timer();
    RecyclerView rv;
    ArrayList<HashMap<String, Object>> statusList = new ArrayList<>();
    ArrayList<User> seenList = new ArrayList<>();
    ActivityOwnStatusBinding binding;

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged", "UseCompatLoadingForDrawables"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOwnStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewModel = new ViewModelProvider(this).get(OwnStatusViewModel.class);
        loadUI();
        setUpListeners();
        handleUpdateStatusListLiveData();

        user = (User) getIntent().getSerializableExtra("user");
        assert user != null;
        statusList = user.getStatus();
        HashMap<String, Object> hm = statusList.get(statusList.size() - 1);
        binding.captionTxt.setText(String.valueOf(hm.get("caption")));
        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
        Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.statusImage);
        binding.userName.setText("You");
        String time = String.valueOf(hm.get("time"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.time.setText(FunctionUtils.timeSetter(time));
        }

        binding.progressLinear.post(() -> {

            layout_width = binding.progressLinear.getMeasuredWidth();
            int progressBarWidth = layout_width / (statusList.size() - 1);

            for (int i = 0; i < statusList.size() - 1; i++) {

                vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                @SuppressLint("InflateParams") View v = vi.inflate(R.layout.sample_progress_bar, null);

                if (i < statusList.size() - 1) {
                    ProgressBar p = (ProgressBar) v;
                    p.setProgress(100);
                }

                insertPoint = findViewById(R.id.progressLinear);
                insertPoint.addView(v, i, new ViewGroup.LayoutParams(progressBarWidth - 3, 10));

            }
            currentStatus = statusList.size() - 1;
            runProgressBar(statusList.size() - 2, false);
        });
        isButtonLongPressed = false;
    }

    private void loadUI() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        binding.li.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.captionTxt.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.linear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.captionTxt.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.userName.setShadowLayer(1, 0, 0, Color.BLACK);
        binding.time.setShadowLayer(1, 0, 0, Color.BLACK);
    }

    @SuppressWarnings("unchecked")
    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables", "NotifyDataSetChanged"})
    private void setUpListeners() {
        binding.homeImg.setOnClickListener(view -> back());
        binding.rl.setOnClickListener(view -> {
            if (!pauseTimer)
                back();
            else {
                if (popupWindow != null)
                    popupWindow.dismiss();

                pauseTimer = false;
            }
        });
        binding.delete.setOnClickListener(view -> {
            pauseTimer = true;
            String message = "Deleted status updates cannot be recovered.";
            AlertDialog alertDialog = new AlertDialog.Builder(OwnStatusPage.this)
                    .setTitle("Delete this status update?")
                    .setMessage(Html.fromHtml("<font color='#808080'>" + message + "</font>"))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // updated in db
                        dialog.dismiss();
                        viewModel.backupDeletedStatus(user.getUserId(), statusList.get(currentStatus));
                        statusList.remove(currentStatus);
                        user.setStatus(statusList);
                        viewModel.updateStatusList(user.getUserId(), statusList);

                    }).setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        pauseTimer = false;
                    }).show();
            alertDialog.setOnDismissListener(dialogInterface -> pauseTimer = false);
        });
        binding.seen.setOnClickListener(view -> {

            if (!pauseTimer) {
                if (popupWindow != null)
                    popupWindow.dismiss();

                pauseTimer = true;

                HashMap<String, Object> hm1 = statusList.get(currentStatus);
                ArrayList<String> seen = (ArrayList<String>) hm1.get("seen");

                rv = new RecyclerView(OwnStatusPage.this);
                RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);

                rv.setLayoutParams(params);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    rv.setBackground(getDrawable(R.drawable.seen_recycler_view_bg));
                }

                LinearLayoutManager llm = new LinearLayoutManager(OwnStatusPage.this);
                SeenListAdapter adapter = new SeenListAdapter(seenList, getApplicationContext());
                rv.setAdapter(adapter);
                rv.setLayoutManager(llm);
                rv.setVisibility(View.VISIBLE);

                seenList.clear();

                User dummy = new User();

                String msg = "Seen by: ";
                dummy.setUserName(msg);
                seenList.add(dummy);
                viewModel.loadSeenList(seen, seenList, adapter);
                // create the popup window
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = false; // true = tapping outside the popup also dismisses it
                popupWindow = new PopupWindow(rv, width, height, focusable);
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        });
        binding.newStatus.setOnClickListener(view -> {
            Intent i = new Intent(OwnStatusPage.this, AddStatusPage.class);
            i.putExtra("code", 8.8);
            i.putExtra("user", user);
            startActivity(i);
        });
        binding.buttonLeft.setOnClickListener(view -> {

            if (popupWindow != null)
                popupWindow.dismiss();
            pauseTimer = false;

            if (currentProgressBar - 1 >= 0) {
                ProgressBar p = (ProgressBar) insertPoint.getChildAt(currentProgressBar);
                p.setProgress(0);
                a.cancel();
                runProgressBar(currentProgressBar - 1, true);
            }
        });
        binding.buttonRight.setOnClickListener(view -> {

            if (popupWindow != null)
                popupWindow.dismiss();

            pauseTimer = false;

            if (currentProgressBar + 1 < statusList.size() - 1) {
                ProgressBar p = (ProgressBar) insertPoint.getChildAt(currentProgressBar);
                p.setProgress(100);
                a.cancel();
                runProgressBar(currentProgressBar + 1, true);
            }
        });
        // dismiss the popup window when touched
        rv.setOnTouchListener((v, event) -> {

            if (popupWindow != null)
                popupWindow.dismiss();

            pauseTimer = false;
            return true;
        });
        binding.rl.setOnLongClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                disableLayout();
            }
            pauseTimer = true;
            isButtonLongPressed = true;
            return true;
        });
        binding.rl.setOnTouchListener((view, motionEvent) -> {
            view.onTouchEvent(motionEvent);

            if (popupWindow != null)
                popupWindow.dismiss();

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
        if (popupWindow != null)
            popupWindow.dismiss();

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

                    if (i + 1 <= statusList.size() - 1) {  //equal to just to update ui for last status (do dry run for 2 viewed status)

                        if (DisplayForFirst) {
                            Executor mainExecutor = ContextCompat.getMainExecutor(getApplicationContext());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                mainExecutor.execute(() -> displayStatus(i + 1));
                            }
                        }

                        if (i + 1 < statusList.size() - 1 && counter == 100)
                            runProgressBar(i + 1, true);
                    }
                }
            }
        };
        a.schedule(b, 0, 40);
    }

    private void handleUpdateStatusListLiveData() {
        androidx.lifecycle.Observer<Resource<Data<User>>> observer = resource -> {
            switch (resource.getStatus()) {
                case LOADING:
                    break;
                case SUCCESS:
                    Toast.makeText(getApplicationContext(), "Status Deleted!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(OwnStatusPage.this, HomeActivity.class);
                    intent.putExtra("pager", 1.2);
                    intent.putExtra("progressDialog", "14");
                    startActivity(intent);
                    break;
                case EXCEPTION:
                    Toast.makeText(getApplicationContext(), resource.getMessage(), Toast.LENGTH_SHORT).show();
                    break;
            }
        };
        viewModel.UpdateStatusListLiveData.observe(this, observer);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void displayStatus(int i) {
        currentStatus = i;
        HashMap<String, Object> hm = statusList.get(i);

        if (hm != null) {
            Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
            Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.statusImage);
            binding.userName.setText("You");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.time.setText(FunctionUtils.timeSetter(String.valueOf(hm.get("time"))));
            }
            binding.captionTxt.setText(String.valueOf(hm.get("caption")));
        } else {
            binding.captionTxt.setText("This status update is unavailable.");
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
        Intent intent = new Intent(OwnStatusPage.this, HomeActivity.class);
        intent.putExtra("pager", 1.2);
        intent.putExtra("progressDialog", "14");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back();
    }

}