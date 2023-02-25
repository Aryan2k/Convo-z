package com.example.convo_z.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.convo_z.MainActivity;
import com.example.convo_z.R;
import com.example.convo_z.databinding.ActivityProfileSettingsBinding;
import com.example.convo_z.model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Objects;

public class ProfileSettingsActivity extends AppCompatActivity {

    ActivityProfileSettingsBinding binding;
    FirebaseAuth auth;
    Uri sFile;
    FirebaseStorage storage;
    FirebaseDatabase database;
    boolean photoChanged = false;
    String disableHome = "2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfileSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        Intent i = getIntent();
        if (i.hasExtra("disableHome"))
            disableHome = i.getStringExtra("disableHome");

        assert disableHome != null;
        if (disableHome.equals("30")) {
            binding.backArrow.setVisibility(View.INVISIBLE);
            binding.backArrow.setEnabled(false);
        }

        binding.backArrow.setOnClickListener(v -> {
            Intent i1 = new Intent(ProfileSettingsActivity.this, MainActivity.class);
            i1.putExtra("progressDialog", "14");
            startActivity(i1);
        });

        database.getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users user = snapshot.getValue(Users.class);

                        assert user != null;
                        Picasso.get()
                                .load(user.getProfilePic())
                                .placeholder(R.drawable.ic_user)
                                .into(binding.profileImage);

                        binding.etUsername.setText(user.getUserName());
                        binding.etBio.setText(user.getBio());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.save.setOnClickListener(v -> {
            if (!binding.etUsername.getText().toString().trim().isEmpty() && !binding.etBio.getText().toString().trim().isEmpty()) {

                String username = binding.etUsername.getText().toString().trim();
                String bio = binding.etBio.getText().toString().trim();

                HashMap<String, Object> obj = new HashMap<>();
                obj.put("userName", username);
                obj.put("bio", bio);

                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .updateChildren(obj).addOnSuccessListener(aVoid -> {
                            Toast.makeText(getApplicationContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                            if (disableHome.equals("30")) {
                                SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
                                sp.edit().putInt("lc", 1).apply();

                                startActivity(new Intent(ProfileSettingsActivity.this, MainActivity.class));
                            }
                        });

                if (photoChanged) {
                    final StorageReference reference = storage.getReference().child("profile_pictures").child(FirebaseAuth.getInstance().getUid());

                    reference.putFile(sFile).addOnSuccessListener(taskSnapshot -> reference.getDownloadUrl().addOnSuccessListener(uri -> {
                        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                .child("profilePic").setValue(uri.toString());
                        //    Toast.makeText(getApplicationContext(), "Profile Photo Updated!", Toast.LENGTH_SHORT).show();
                    }));
                }
            } else {
                if (binding.etUsername.getText().toString().trim().isEmpty() && !binding.etBio.getText().toString().trim().isEmpty()) {
                    binding.etUsername.setError("Enter username");
                } else if (!binding.etUsername.getText().toString().trim().isEmpty() && binding.etBio.getText().toString().trim().isEmpty()) {
                    binding.etBio.setError("Enter bio");
                } else {
                    binding.etUsername.setError("Enter username");
                    binding.etBio.setError("Enter bio");
                }
                //Toast.makeText(getApplicationContext(),"Please enter both the fields!",Toast.LENGTH_SHORT).show();
            }
        });

        binding.plus.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 33);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            sFile = data.getData();
            binding.profileImage.setImageURI(sFile);
            photoChanged = true;
        }
    }

    @Override
    public void onBackPressed() {
        if (disableHome.equals("10")) {
            Intent i = new Intent(ProfileSettingsActivity.this, MainActivity.class);
            i.putExtra("progressDialog", "14");
            startActivity(i);
        } else
            moveTaskToBack(true);
    }
}