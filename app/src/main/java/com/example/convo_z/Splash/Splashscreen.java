package com.example.convo_z.Splash;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.convo_z.Login.LoginClass;
import com.example.convo_z.MainActivity;
import com.example.convo_z.R;

public class Splashscreen extends AppCompatActivity {

    int SPLASH_TIME = 2000;

    Animation topAnim,bottomAnim;
    ImageView logo;
    TextView slogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        topAnim = AnimationUtils.loadAnimation(this,R.anim.top_anim);
        bottomAnim = AnimationUtils.loadAnimation(this,R.anim.bottom_anim);
        logo = (ImageView)(findViewById(R.id.imageView2));
        slogan = (TextView)(findViewById(R.id.textView6));

        logo.setAnimation(topAnim);
        slogan.setAnimation(bottomAnim);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
                int logincheck = sp.getInt("lc", 0);

                if(logincheck==1)
              {
                  Intent i = new Intent(Splashscreen.this, MainActivity.class);
                  startActivity(i);
              }
              else
              {
                  Intent j = new Intent(Splashscreen.this, LoginClass.class);
                  startActivity(j);
              }

                finish();

            }
        }, SPLASH_TIME);
    }
}
