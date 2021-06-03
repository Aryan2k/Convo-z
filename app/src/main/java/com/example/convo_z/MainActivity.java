package com.example.convo_z;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.convo_z.Adapters.FragmentsAdapter;
import com.example.convo_z.Login.LoginClass;
import com.example.convo_z.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
        binding.tablayout.setupWithViewPager(binding.viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu Menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,Menu);
        return super.onCreateOptionsMenu(Menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.settings:
                break;

            case R.id.logout:

                auth.signOut();

                SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                sp.edit().putInt("lc",0).apply();

                Intent i = new Intent(MainActivity.this, LoginClass.class);
                startActivity(i);

                break;
        }



        return true;
    }
}