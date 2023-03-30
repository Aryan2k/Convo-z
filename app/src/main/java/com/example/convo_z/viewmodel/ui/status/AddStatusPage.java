package com.example.convo_z.viewmodel.ui.status;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.convo_z.viewmodel.ui.home.HomeActivity;
import com.example.convo_z.model.User;
import com.example.convo_z.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddStatusPage extends AppCompatActivity {

    ActivityStatusPageBinding binding;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;
    Uri sFile;
    double code = 0;
    User user = new User();
    boolean photoPicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();

        if (i.hasExtra("code"))
            code = i.getDoubleExtra("code", 0.0);

        if (i.hasExtra("user"))
            user = (User) i.getSerializableExtra("user");

        binding = ActivityStatusPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        binding.imageView7.setOnClickListener(view -> startActivity(new Intent(AddStatusPage.this, StatusPrivacyPage.class)));

        binding.share.setOnClickListener(view -> {

            if (photoPicked) {

                final StorageReference reference = storage.getReference().child("status_updates").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                String time = String.valueOf(new Date().getTime());

                reference.child(time).putFile(sFile).addOnSuccessListener(taskSnapshot -> reference.child(time).getDownloadUrl().addOnSuccessListener(uri -> database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                String caption = binding.caption.getText().toString();
                                assert user != null;

                                ArrayList<HashMap<String, Object>> status = user.getStatus();

                                HashMap<String, Object> newStatus = new HashMap<>();

                                ArrayList<String> seen = new ArrayList<>();
                                seen.add("dummy");

                                newStatus.put("link", uri.toString());
                                newStatus.put("caption", caption);
                                newStatus.put("time", time);
                                newStatus.put("seen", seen);

                                status.add(newStatus);

                                database.getReference().child("Users").child(FirebaseAuth.getInstance().getUid())
                                        .child("status").setValue(status);

                                binding.caption.setText("");
                                binding.camera.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_camera));
                                binding.camera.setClickable(true);

                                Toast.makeText(getApplicationContext(), "Status Updated!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        })));
            } else {
                Toast.makeText(getApplicationContext(), "Please pick a photo first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onCameraClick(View v) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 33);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null && data.getData() != null) {
            sFile = data.getData();
            photoPicked = true;
            binding.camera.setImageURI(sFile);
            binding.camera.setClickable(false);
        }
    }

    @Override
    public void onBackPressed() {
        if (code == 0) {
            Intent intent = new Intent(AddStatusPage.this, HomeActivity.class);
            intent.putExtra("pager", 1.2);
            intent.putExtra("progressDialog", "14");
            startActivity(intent);
        } else {
            Intent i = new Intent(AddStatusPage.this, OwnStatusPage.class);
            i.putExtra("user", user);
            startActivity(i);
        }
    }
}
