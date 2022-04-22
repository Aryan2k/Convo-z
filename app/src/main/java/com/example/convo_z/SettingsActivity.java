package com.example.convo_z;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.convo_z.Model.Users;
import com.example.convo_z.databinding.ActivitySettingsBinding;
import com.example.convo_z.databinding.ActivitySignupBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import okhttp3.internal.cache.DiskLruCache;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        getSupportActionBar().hide();

        Intent i = getIntent();
        final String disableHome = i.getStringExtra("disableHome");

        if(disableHome.equals("30"))
        {
            binding.backArrow.setVisibility(View.INVISIBLE);
            binding.backArrow.setEnabled(false);
        }

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(SettingsActivity.this,MainActivity.class);
                i.putExtra("progressDialog","14");
                startActivity(i);
            }
        });

        database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Users user = snapshot.getValue(Users.class);

                        Picasso.get()
                                .load(user.getProfilepic())
                                .placeholder(R.drawable.ic_user)
                                .into(binding.profileImage);

                        binding.etusername.setText(user.getUserName());
                        binding.etstatus.setText(user.getStatus());

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!binding.etusername.getText().toString().trim().isEmpty() && !binding.etstatus.getText().toString().trim().isEmpty())
                {

                    String username = binding.etusername.getText().toString().trim();
                    String status = binding.etstatus.getText().toString().trim();

                    HashMap<String,Object> obj = new HashMap<>();
                    obj.put("userName",username);
                    obj.put("status",status);

                    database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                            .updateChildren(obj).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"Profile updated successfully!",Toast.LENGTH_SHORT).show();

                            if(disableHome.equals("30"))
                            {
                                SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                                sp.edit().putInt("lc",1).apply();

                                Intent i = new Intent(SettingsActivity.this,MainActivity.class);
                                startActivity(i);
                            }
                        }
                    });
                }
                else
                {
                      if(binding.etusername.getText().toString().trim().isEmpty() && !binding.etstatus.getText().toString().trim().isEmpty())
                      {
                          binding.etusername.setError("Enter username");
                          return;
                      }
                      else if(!binding.etusername.getText().toString().trim().isEmpty() && binding.etstatus.getText().toString().trim().isEmpty())
                      {
                        binding.etstatus.setError("Enter status");
                        return;
                      }
                      else
                        {
                          binding.etusername.setError("Enter username");
                          binding.etstatus.setError("Enter status");
                          return;
                        }
                      //Toast.makeText(getApplicationContext(),"Please enter both the fields!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,33);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data.getData()!=null)
        {
            Uri sFile = data.getData();
            binding.profileImage.setImageURI(sFile);

            final StorageReference reference = storage.getReference().child("profile_pictures").child(FirebaseAuth.getInstance().getUid());

            reference.putFile(sFile).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                    .child("profilepic").setValue(uri.toString());
                             Toast.makeText(getApplicationContext(),"Profile Photo Updated!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}