package com.example.convo_z.status;

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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.convo_z.MainActivity;
import com.example.convo_z.R;
import com.example.convo_z.adapters.SeenAdapter;
import com.example.convo_z.databinding.ActivityOwnStatusBinding;
import com.example.convo_z.model.Users;
import com.example.convo_z.utils.FunctionUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;

public class OwnStatus extends AppCompatActivity {

    Users user;
    FirebaseDatabase database;
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
    ArrayList<HashMap<String, Object>> statusList = new ArrayList<>();
    ArrayList<Users> seenList = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    ActivityOwnStatusBinding binding;

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n", "NotifyDataSetChanged", "UseCompatLoadingForDrawables"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOwnStatusBinding.inflate(getLayoutInflater());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        binding.li.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.textView15.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.linear.setBackgroundColor(getResources().getColor(colorPrimary));
        binding.progressLinear.setBackgroundColor(getResources().getColor(colorPrimary));

        binding.textView15.setShadowLayer(2, 0, 0, Color.BLACK);
        binding.userName.setShadowLayer(1, 0, 0, Color.BLACK);
        binding.time.setShadowLayer(1, 0, 0, Color.BLACK);

        user = (Users) getIntent().getSerializableExtra("user");

        assert user != null;
        statusList = user.getStatus();

        HashMap<String, Object> hm = statusList.get(statusList.size() - 1);

        Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
        Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.imageView9);

        binding.userName.setText("You");
        String time = String.valueOf(hm.get("time"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.time.setText(FunctionUtils.timeSetter(time));
        }
        binding.textView15.setText(String.valueOf(hm.get("caption")));

        binding.imageView10.setOnClickListener(view -> back());

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

        binding.delete.setOnClickListener(view -> {

            pauseTimer = true;
            String message = "Deleted status updates cannot be recovered.";
            AlertDialog alertDialog = new AlertDialog.Builder(OwnStatus.this)
                    .setTitle("Delete this status update?")
                    //    .setMessage(message)
                    .setMessage(Html.fromHtml("<font color='#808080'>" + message + "</font>"))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        //updated in db
                        dialog.dismiss();
                        database.getReference().child("Users").child(user.getUserId()).child("deletedStatus").push().setValue(statusList.get(currentStatus));

                        statusList.remove(currentStatus);
                        user.setStatus(statusList);

                        database.getReference().child("Users").child(user.getUserId()).child("status").
                                setValue(statusList).addOnSuccessListener(unused -> {
                                    Toast.makeText(getApplicationContext(), "Status Deleted!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(OwnStatus.this, MainActivity.class);
                                    intent.putExtra("pager", 1.2);
                                    intent.putExtra("progressDialog", "14");
                                    startActivity(intent);
                                });
                    }).setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                        pauseTimer = false;
                    }).show();
            alertDialog.setOnDismissListener(dialogInterface -> pauseTimer = false);
        });

        binding.rl.setOnClickListener(view -> {

            if (!pauseTimer)
                back();
            else {
                if (popupWindow != null)
                    popupWindow.dismiss();

                pauseTimer = false;
            }
        });

        binding.newStatus.setOnClickListener(view -> {
            Intent i = new Intent(OwnStatus.this, StatusPage.class);
            i.putExtra("code", 8.8);
            i.putExtra("user", user);
            startActivity(i);
        });

        binding.seen.setOnClickListener(view -> {

            if (!pauseTimer) {
                if (popupWindow != null)
                    popupWindow.dismiss();

                pauseTimer = true;

                HashMap<String, Object> hm1 = statusList.get(currentStatus);
                ArrayList<String> seen = (ArrayList<String>) hm1.get("seen");

                RecyclerView rv = new RecyclerView(OwnStatus.this);
                RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);

                rv.setLayoutParams(params);
                rv.setBackground(getDrawable(R.drawable.seen_recycler_view_bg));

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

                for (int i = 1; i < Objects.requireNonNull(seen).size(); i++) {
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
                boolean focusable = false; // true = tapping outside the popup also dismisses it
                popupWindow = new PopupWindow(rv, width, height, focusable);

                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

                // dismiss the popup window when touched
                rv.setOnTouchListener((v, event) -> {

                    if (popupWindow != null)
                        popupWindow.dismiss();

                    pauseTimer = false;
                    return true;
                });
            }
        });

        isButtonLongPressed = false;

        binding.rl.setOnLongClickListener(view -> {
            disableLayout();
            pauseTimer = true;
            isButtonLongPressed = true;
            return true;
        });

        binding.rl.setOnTouchListener((view, motionEvent) -> {
            view.onTouchEvent(motionEvent);

            if (popupWindow != null)
                popupWindow.dismiss();

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

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void displayStatus(int i) {
        currentStatus = i;
        HashMap<String, Object> hm = statusList.get(i);

        if (hm != null) {
            Picasso.get().load(user.getProfilePic()).placeholder(R.drawable.ic_user).into(binding.profileImage);
            Picasso.get().load(String.valueOf(hm.get("link"))).placeholder(R.drawable.ic_user).into(binding.imageView9);

            binding.userName.setText("You");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.time.setText(FunctionUtils.timeSetter(String.valueOf(hm.get("time"))));
            }
            binding.textView15.setText(String.valueOf(hm.get("caption")));
        } else {
            binding.textView15.setText("This status update is unavailable.");
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
        Intent intent = new Intent(OwnStatus.this, MainActivity.class);
        intent.putExtra("pager", 1.2);
        intent.putExtra("progressDialog", "14");
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        back();
    }

}