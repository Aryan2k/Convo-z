package com.example.convo_z.Signup;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.convo_z.Login.LoginClass;
import com.example.convo_z.MainActivity;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.SettingsActivity;
import com.example.convo_z.Verification.PhoneVerification;
import com.example.convo_z.databinding.ActivitySignupBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class SignupClass extends AppCompatActivity {

 //   private static final String ALGORITHM = "AES";
   // private static final String KEY = "1Hbfh667adfDEJ78";

    GoogleSignInClient mGoogleSignInClient;

    ActivitySignupBinding binding;

    private FirebaseAuth auth;
    FirebaseDatabase database;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(SignupClass.this);
        progressDialog.setTitle("Registering User");
        progressDialog.setMessage("We're signing you up!");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("938774708033-8pbshs2bhd4vokqqa2ob48mqsos077e4.apps.googleusercontent.co")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        binding.signupPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SignupClass.this, PhoneVerification.class);
                i.putExtra("code","22"); //dummy value to avoid crash
                startActivity(i);
            }
        });

        binding.signinn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignupClass.this, LoginClass.class);
                startActivity(i);
            }
        });

        binding.google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        binding.signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                if (!binding.fullname.getText().toString().isEmpty() && !binding.email.getText().toString().isEmpty()
                        && !binding.password.getText().toString().isEmpty() && !binding.repassword.getText().toString().isEmpty()) {

                    if (binding.password.getText().toString().trim().equals(binding.repassword.getText().toString().trim())) {

                       auth.createUserWithEmailAndPassword(binding.email.getText().toString().trim(),binding.password.getText().toString().trim()).
                               addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {

                           //    progressDialog.dismiss();

                               if(task.isSuccessful())
                               {
                                   Users user = new Users(binding.fullname.getText().toString().trim(),
                                           binding.email.getText().toString().trim(),binding.password.getText().toString().trim());

                                   String id = task.getResult().getUser().getUid();

                                   ArrayList<HashMap<String,Object>> status = new ArrayList<>();
                                   ArrayList<String> muted = new ArrayList<>();
                                   ArrayList<String> blocked = new ArrayList<>();
                                   ArrayList<String> hidden = new ArrayList<>();

                                   String profilePic = getResources().getString(R.string.ic_user);

                                   HashMap<String,Object> s = new HashMap<>();
                                   s.put("dummy","");
                                   status.add(s);
                                   muted.add("");
                                   blocked.add("");
                                   hidden.add("");

                                   user.setMuted(muted);
                                   user.setStatus(status);
                                   user.setBlocked(blocked);
                                   user.setProfilePic(profilePic);
                                   user.setUserId(id);
                                   user.setBio("");
                                   user.setLastSeen("");
                                   user.setHidden(hidden);

                                   database.getReference().child("Users").child(id).setValue(user);

                                   Toast.makeText(getApplicationContext(), "Successfully Signed up!", Toast.LENGTH_SHORT).show();

                                   checkProfileStatus(id);
                               }
                               else
                               {
                                   Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                   progressDialog.dismiss();
                               }
                           }
                       });
                    }
                    else
                    {
                        binding.repassword.setError("The Passwords don't match!");
                        binding.repassword.requestFocus();
                       // Toast.makeText(getApplicationContext(),"The Passwords don't match!",Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                        return;
                    }
                } else {

                    String result = emptydetector(binding.fullname.getText().toString().trim(), binding.email.getText().toString().trim(),
                            binding.password.getText().toString().trim() ,binding.repassword.getText().toString().trim());

                    //0=empty, 1= not empty
                    //1111,1110,1101,1100,1011,1010,1001,1000,0111,0110,0101,0100,0011,0010,0001,0000

                    switch (result) { //1111 not poss here

                        case "1110":
                            binding.repassword.setError("Enter your password again");

                            binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "1101":
                            binding.password.setError("Enter your password");

                            binding.password.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "1100":
                            binding.password.setError("Enter your password");
                            binding.repassword.setError("Enter your password again");

                            binding.password.requestFocus();
                            binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "1011":
                            binding.email.setError("Enter your email");

                            binding.email.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "1010":
                            binding.email.setError("Enter your email");
                            binding.repassword.setError("Enter your password again");

                            binding.email.requestFocus();
                            binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "1001":
                            binding.email.setError("Enter your email");
                            binding.password.setError("Enter your password");

                            binding.email.requestFocus();
                            binding.password.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "1000":
                            binding.email.setError("Enter your email");
                            binding.password.setError("Enter your password");
                            binding.repassword.setError("Enter your password again");

                            binding.email.requestFocus();
                            binding.password.requestFocus();
                            binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "0111":
                            binding.fullname.setError("Enter your full name");

                            binding.fullname.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "0110":
                            binding.fullname.setError("Enter your full name");
                            binding.repassword.setError("Enter your password again");

                            binding.fullname.requestFocus();
                            binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "0101":
                            binding.fullname.setError("Enter your full name");
                            binding.password.setError("Enter your password");

                            binding.fullname.requestFocus();
                            binding.password.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "0100":
                            binding.fullname.setError("Enter your full name");
                            binding.password.setError("Enter your password");
                            binding.repassword.setError("Enter your password again");

                            binding.fullname.requestFocus();
                            binding.password.requestFocus();
                            binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "0011":
                            binding.fullname.setError("Enter your full name");
                            binding.email.setError("Enter your email");

                            binding.fullname.requestFocus();
                            binding.email.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "0010":
                            binding.fullname.setError("Enter your full name");
                            binding.email.setError("Enter your email");
                            binding.repassword.setError("Enter your password again");

                            binding.fullname.requestFocus();
                            binding.email.requestFocus();
                            binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;

                        case "0001":
                            binding.fullname.setError("Enter your full name");
                            binding.email.setError("Enter your email");
                            binding.password.setError("Enter your password");

                            binding.fullname.requestFocus();
                            binding.email.requestFocus();
                            binding.password.requestFocus();

                            progressDialog.dismiss();
                            return;

                            default: //0000
                            binding.fullname.setError("Enter your full name");
                            binding.email.setError("Enter your email");
                            binding.password.setError("Enter your password");
                            binding.repassword.setError("Enter your password again");

                                binding.fullname.requestFocus();
                                binding.email.requestFocus();
                                binding.password.requestFocus();
                                binding.repassword.requestFocus();

                            progressDialog.dismiss();
                            return;
                    }

                }

                         // Toast.makeText(getApplicationContext(), "Please enter all the fields!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public String emptydetector(String fullname, String email, String password, String repassword)
    {
        String result="";
      if(fullname.isEmpty())
      {
          result+=0;
      }
      else{
          result+=1;
      }
      if(email.isEmpty())
      {
          result+=0;
      }
      else
      {
          result+=1;
      }
      if(password.isEmpty())
      {
          result+=0;
      }
      else
      {
          result+=1;
      }
      if(repassword.isEmpty())
      {
          result+=0;
      }
      else
      {
          result+=1;
      }
      return result;
    }

 /*       public static String encrypt (String value) throws Exception
        {
            Key key = generateKey();
            Cipher cipher = Cipher.getInstance(signupClass.ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
            String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
            return encryptedValue64;

        }

        private static Key generateKey () throws Exception
        {
            Key key = new SecretKeySpec(signupClass.KEY.getBytes(), signupClass.ALGORITHM);
            return key;
        }*/

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
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d("TAG", "signInWithCredential:success");
                            final FirebaseUser user = auth.getCurrentUser();

                            database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override                 //to check if the user has already signed up with that google account
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    assert user != null;
                                    if(!snapshot.hasChild(user.getUid())) //if not,add it to db
                                    {
                                        Users users = new Users();

                                        ArrayList<HashMap<String,Object>> status = new ArrayList<>();
                                        ArrayList<String> muted = new ArrayList<>();
                                        ArrayList<String> blocked = new ArrayList<>();
                                        ArrayList<String> hidden = new ArrayList<>();

                                        HashMap<String,Object> s = new HashMap<>();
                                        s.put("dummy","");
                                        status.add(s);
                                        muted.add("");
                                        blocked.add("");
                                        hidden.add("");

                                        users.setMuted(muted);
                                        users.setStatus(status);
                                        users.setBlocked(blocked);
                                        users.setEmail(user.getEmail());
                                        users.setBio("");
                                        //users.setPassword("");
                                        users.setLastSeen("");
                                        users.setUserId(user.getUid());
                                        users.setUserName(user.getDisplayName());
                                        users.setProfilePic(user.getPhotoUrl().toString());
                                        users.setHidden(hidden);

                                        database.getReference().child("Users").child(user.getUid()).setValue(users);
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
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void checkProfileStatus(final String userID)
    {

        database.getReference().child("Users").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                progressDialog.dismiss();

                if(snapshot.child("phoneNumber").exists() && snapshot.child("userName").exists()) //profile already updated
                {
                    Toast.makeText(getApplicationContext(),"Signed in with google!",Toast.LENGTH_SHORT).show();
                    SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                    sp.edit().putInt("lc",1).apply();

                    Intent i = new Intent(SignupClass.this, MainActivity.class);
                    startActivity(i);
                }
                else if(!snapshot.child("phoneNumber").exists() && snapshot.child("userName").exists()) //google signup or email signup
                {
                    Intent i = new Intent(SignupClass.this, PhoneVerification.class);
                    i.putExtra("code","44");  //determines if this is an account linking case or new independent signin/up using phone
                    startActivity(i);
                }
                else if (snapshot.child("phoneNumber").exists() && !snapshot.child("userName").exists())  //signup using phone
                {
                    Intent i = new Intent(SignupClass.this, SettingsActivity.class);
                    startActivity(i);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
