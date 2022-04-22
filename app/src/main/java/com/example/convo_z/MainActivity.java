package com.example.convo_z;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.convo_z.Adapters.FragmentsAdapter;
import com.example.convo_z.Login.LoginClass;
import com.example.convo_z.Splash.Splashscreen;
import com.example.convo_z.Verification.OTPVerification;
import com.example.convo_z.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CONTACTS},
                1);

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

                Intent in = new Intent(MainActivity.this,SettingsActivity.class);
                in.putExtra("disableHome","10"); //dummy value to avoid crash
                startActivity(in);

                break;

            case R.id.security:
                //forgot password and Update phone number will be added here
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    binding.viewPager.setAdapter(new FragmentsAdapter(getSupportFragmentManager()));
                    binding.tablayout.setupWithViewPager(binding.viewPager);

                    Intent i = getIntent();
                    String progressDialogRemover = i.getStringExtra("progressDialog");

                    if(progressDialogRemover==null) {  //better way to prevent crashes than passing dummy values

                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setTitle("Loading");
                        progressDialog.setMessage("Getting things ready asap!");

                        progressDialog.show();

                        Runnable progressRunnable = new Runnable() {

                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        };

                        Handler pdCanceller = new Handler();
                        pdCanceller.postDelayed(progressRunnable, 2500);
                    }

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission
                    Toast.makeText(MainActivity.this, "PERMISSION DENIED: Cannot Open Contacts", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}