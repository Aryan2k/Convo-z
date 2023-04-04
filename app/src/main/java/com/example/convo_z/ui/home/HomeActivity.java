package com.example.convo_z.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.convo_z.R;
import com.example.convo_z.adapters.FragmentsAdapter;
import com.example.convo_z.databinding.ActivityHomeBinding;
import com.example.convo_z.repository.HomeRepository;
import com.example.convo_z.ui.authentication.LoginActivity;
import com.example.convo_z.ui.settings.ProfileSettingsActivity;
import com.example.convo_z.utils.Constants;
import com.example.convo_z.utils.FunctionUtils;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    Serializable setViewPager;

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new HomeRepository(); // to initialise firebase database instance.

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(getDrawable(R.color.colorAccent));
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        loadWindowParams();

        if (getIntent().hasExtra(getString(R.string.set_view_pager)))
            setViewPager = getIntent().getSerializableExtra(getString(R.string.set_view_pager));

        ActivityCompat.requestPermissions(HomeActivity.this,
                new String[]{Manifest.permission.READ_CONTACTS},
                1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadWindowParams() {
        Window window = HomeActivity.this.getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimaryVariant));
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(R.drawable.core_colour2));
        //<gradient android:startColor="#333440" android:endColor="#333440"
        //  binding.viewPager.setOffscreenPageLimit(1);
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
                startActivity(new Intent(HomeActivity.this, ProfileSettingsActivity.class)
                        .putExtra(getString(R.string.disable_home_button), Constants.CASE_DO_NOT_DISABLE_HOME));
                break;

            case R.id.security:
                //forgot password and Update phone number will be added here
                break;

            case R.id.logout:

                FirebaseAuth.getInstance().signOut();
                SharedPreferences sp = getSharedPreferences(getString(R.string.login), MODE_PRIVATE);
                sp.edit().putInt(getString(R.string.login_check), 0).apply();
                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {  // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted!.
                binding.viewPager.setAdapter((new FragmentsAdapter(getSupportFragmentManager(), getLifecycle())));
                new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                        (tab, position) -> {
                            String[] tabTitles = {getString(R.string.chats_all_capital), getString(R.string.status_all_capital), getString(R.string.calls_all_capital)};
                            tab.setText(tabTitles[position]);
                        }).attach();
                if (setViewPager != null && setViewPager.equals(Constants.CASE_STATUS_FRAGMENT))
                    binding.viewPager.setCurrentItem(1);
            }
        } else {
            // permission denied, boo! Disable the functionality that depends on this permission
            FunctionUtils.getSnackBar(getString(R.string.permission_denied_cannot_open_contacts), binding.getRoot()).show();
        }
        // other 'case' lines to check for other
        // permissions this app might request
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