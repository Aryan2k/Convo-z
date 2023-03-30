package com.example.convo_z.viewmodel.ui.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.convo_z.R;
import com.example.convo_z.viewmodel.ui.authentication.LoginActivity;
import com.example.convo_z.viewmodel.ui.home.HomeActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
@SuppressLint("CustomSplashScreen")
public class SplashScreenActivity extends AppCompatActivity {

    int SPLASH_TIME = 2000;

    Animation topAnim, bottomAnim;
    ImageView logo;
    TextView slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        topAnim = AnimationUtils.loadAnimation(this, R.anim.top_anim);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);
        logo = findViewById(R.id.imageView2);
        slogan = findViewById(R.id.textView6);

        logo.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);

        new Handler().postDelayed(() -> {

            SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
            int loginCheck = sp.getInt("lc", 0);

            if (loginCheck == 1) {
                Intent i = new Intent(SplashScreenActivity.this, HomeActivity.class);
                startActivity(i);
            } else {
                Intent j = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(j);
            }
            finish();

        }, SPLASH_TIME);
    }
}