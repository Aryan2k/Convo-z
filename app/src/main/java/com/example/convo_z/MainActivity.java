package com.example.convo_z;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.convo_z.adapters.FragmentsAdapter;
import com.example.convo_z.authentication.LoginActivity;
import com.example.convo_z.databinding.ActivityMainBinding;
import com.example.convo_z.settings.ProfileSettingsActivity;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    Double pager = 0.0;

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(getDrawable(R.color.colorAccent));
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        Window window = MainActivity.this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimaryVariant));
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(R.drawable.core_colour2));
        //<gradient android:startColor="#333440" android:endColor="#333440"
        //  binding.viewPager.setOffscreenPageLimit(1);

        auth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        if (i.hasExtra("pager"))
            pager = i.getDoubleExtra("pager", 0.0);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_CONTACTS},
                1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu Menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, Menu);
        return super.onCreateOptionsMenu(Menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.settings:

                Intent in = new Intent(MainActivity.this, ProfileSettingsActivity.class);
                in.putExtra("disableHome", "10");
                startActivity(in);
                break;

            case R.id.security:
                //forgot password and Update phone number will be added here
                break;

            case R.id.logout:

                auth.signOut();
                SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
                sp.edit().putInt("lc", 0).apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted!.

                binding.viewPager.setAdapter((new FragmentsAdapter(getSupportFragmentManager(), getLifecycle())));

                new TabLayoutMediator(binding.tablayout, binding.viewPager,
                        (tab, position) -> {
                            String[] tabTitles = {"CHATS", "STATUS", "CALLS"};
                            tab.setText(tabTitles[position]);
                        }).attach();

                if (pager == 1.2)
                    binding.viewPager.setCurrentItem(1);

                Intent i = getIntent();
                String progressDialogRemover = i.getStringExtra("progressDialog");

                if (progressDialogRemover == null) {  //better way to prevent crashes than passing dummy values

                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setTitle("Loading");
                    progressDialog.setMessage("Getting things ready asap!");

                    progressDialog.show();

                    Runnable progressRunnable = () -> progressDialog.dismiss();

                    Handler pdCanceller = new Handler();
                    pdCanceller.postDelayed(progressRunnable, 2000);
                }

            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission
                Toast.makeText(MainActivity.this, "PERMISSION DENIED: Cannot Open Contacts", Toast.LENGTH_SHORT).show();
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.viewPager.getCurrentItem() != 0)
            binding.viewPager.setCurrentItem(0);
        else {
            finishAffinity();
            super.onBackPressed();
        }
    }

}