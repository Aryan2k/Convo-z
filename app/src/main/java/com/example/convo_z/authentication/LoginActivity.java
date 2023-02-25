package com.example.convo_z.authentication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.convo_z.MainActivity;
import com.example.convo_z.model.Users;
import com.example.convo_z.settings.ProfileSettingsActivity;
import com.example.convo_z.verification.PhoneVerification;
import com.example.convo_z.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    /**
     * private static final String ALGORITHM = "AES";
     * // private static final String KEY = "1Hbfh667adfDEJ78";
     **/

    GoogleSignInClient mGoogleSignInClient;

    ActivityLoginBinding loginBinding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setTitle("Logging in User");
        progressDialog.setMessage("We're signing you in!");

        loginBinding.signupTxt.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this,SignupActivity.class);
            startActivity(i);
        });

        loginBinding.signinPhone.setOnClickListener(v -> {
            Intent i = new Intent(LoginActivity.this, PhoneVerification.class);
            i.putExtra("code", "22"); //dummy value to avoid crash
            startActivity(i);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("938774708033-8pbshs2bhd4vokqqa2ob48mqsos077e4.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginBinding.signin.setOnClickListener(v -> {

            progressDialog.show();

            if (!loginBinding.email.getText().toString().isEmpty() && !loginBinding.password.getText().toString().isEmpty()) {

                auth.signInWithEmailAndPassword(loginBinding.email.getText().toString().trim(), loginBinding.password.getText().toString().trim())
                        .addOnCompleteListener(task -> {

                            if (task.isSuccessful()) {
                                checkProfileStatus(FirebaseAuth.getInstance().getUid());
                            } else {
                                Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        });
            } else {
                if (loginBinding.email.getText().toString().trim().isEmpty() && !loginBinding.password.getText().toString().trim().isEmpty()) {
                    loginBinding.email.setError("Enter your email");
                    loginBinding.email.requestFocus();
                    progressDialog.dismiss();
                } else if (!loginBinding.email.getText().toString().trim().isEmpty() && loginBinding.password.getText().toString().trim().isEmpty()) {
                    loginBinding.password.setError("Enter your password");
                    loginBinding.password.requestFocus();
                    progressDialog.dismiss();
                } else {
                    loginBinding.email.setError("Enter your email");
                    loginBinding.password.setError("Enter your password");
                    loginBinding.email.requestFocus();
                    loginBinding.password.requestFocus();
                    progressDialog.dismiss();
                }
            }
        });

        loginBinding.google.setOnClickListener(v -> signIn());

    }

    /**
     * public static String encrypt(String value) throws Exception
     * {
     * Key key = generateKey();
     * Cipher cipher = Cipher.getInstance(LoginClass.ALGORITHM);
     * cipher.init(Cipher.ENCRYPT_MODE, key);
     * byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
     * String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
     * return encryptedValue64;
     * <p>
     * }
     * private static Key generateKey() throws Exception
     * {
     * Key key = new SecretKeySpec(LoginClass.KEY.getBytes(),LoginClass.ALGORITHM);
     * return key;
     * }
     **/

    int RC_SIGN_IN = 65;

    private void signIn() {
        progressDialog.show();
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful.
                GoogleSignInAccount account = task.getResult(ApiException.class);
                assert account != null;
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                // Google Sign In failed.
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {

        final AuthCredential[] credential = {GoogleAuthProvider.getCredential(idToken, null)};
        auth.signInWithCredential(credential[0])
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {

                        //   progressDialog.dismiss();
                        final FirebaseUser user = auth.getCurrentUser();

                        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override                 //to check if the user has already signed up with that google account
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                assert user != null;
                                if (!snapshot.hasChild(user.getUid())) //if not,add it to db
                                {
                                    Users users = new Users();

                                    ArrayList<HashMap<String, Object>> status = new ArrayList<>();
                                    ArrayList<String> muted = new ArrayList<>();
                                    ArrayList<String> blocked = new ArrayList<>();
                                    ArrayList<String> hidden = new ArrayList<>();

                                    HashMap<String, Object> s = new HashMap<>();
                                    s.put("dummy", "");
                                    status.add(s);
                                    muted.add("");
                                    blocked.add("");
                                    hidden.add("");

                                    users.setMuted(muted);
                                    users.setStatus(status);
                                    users.setBlocked(blocked);
                                    users.setBio("");
                                    users.setLastSeen("");
                                    users.setEmail(user.getEmail());
                                    //users.setPassword("");
                                    users.setUserId(user.getUid());
                                    users.setUserName(user.getDisplayName());
                                    users.setProfilePic(Objects.requireNonNull(user.getPhotoUrl()).toString());
                                    users.setHidden(hidden);

                                    database.getReference().child("Users").child(user.getUid()).setValue(users);
                                    Toast.makeText(getApplicationContext(), "Signed in with google!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        assert user != null;
                        checkProfileStatus(user.getUid());
                    } else {
                        // If sign in fails, display a message to the user.
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void checkProfileStatus(final String userID) {
        database.getReference().child("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressDialog.dismiss();

                if (snapshot.child("phoneNumber").exists() && snapshot.child("userName").exists()) //profile already updated
                {
                    Toast.makeText(getApplicationContext(), "Sign in successful!", Toast.LENGTH_SHORT).show();
                    SharedPreferences sp = getSharedPreferences("login", MODE_PRIVATE);
                    sp.edit().putInt("lc", 1).apply();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                } else if (!snapshot.child("phoneNumber").exists() && snapshot.child("userName").exists()) //google signup or email signup
                {
                    Intent i = new Intent(LoginActivity.this, PhoneVerification.class);
                    i.putExtra("code", "44"); //determines if this is an account linking case or new independent signin/up using phone
                    startActivity(i);

                } else if (snapshot.child("phoneNumber").exists() && !snapshot.child("userName").exists())  //signup using phone
                {
                    startActivity(new Intent(LoginActivity.this, ProfileSettingsActivity.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
