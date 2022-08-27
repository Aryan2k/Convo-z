package com.example.convo_z.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.convo_z.MainActivity;
import com.example.convo_z.Model.MessagesModel;
import com.example.convo_z.Model.Users;
import com.example.convo_z.R;
import com.example.convo_z.SettingsActivity;
import com.example.convo_z.Signup.signupClass;
import com.example.convo_z.Verification.PhoneVerification;
import com.example.convo_z.databinding.ActivityLoginBinding;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LoginClass extends AppCompatActivity {

   // private static final String ALGORITHM = "AES";
   // private static final String KEY = "1Hbfh667adfDEJ78";

    GoogleSignInClient mGoogleSignInClient;

    ActivityLoginBinding loginBinding;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    FirebaseDatabase database;
   // String url_logincheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());

        getSupportActionBar().hide();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        progressDialog = new ProgressDialog(LoginClass.this);
        progressDialog.setTitle("Logging in User");
        progressDialog.setMessage("We're signing you in!");

        loginBinding.signinPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginClass.this,PhoneVerification.class);
                i.putExtra("code","22"); //dummy value to avoid crash
                startActivity(i);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("938774708033-8pbshs2bhd4vokqqa2ob48mqsos077e4.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        loginBinding.signinL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog.show();

                if(!loginBinding.emailL.getText().toString().isEmpty() && !loginBinding.passwordL.getText().toString().isEmpty()) {

                    auth.signInWithEmailAndPassword(loginBinding.emailL.getText().toString().trim(), loginBinding.passwordL.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                //    progressDialog.dismiss();

                                    if(task.isSuccessful()) {
                                        checkProfileStatus(FirebaseAuth.getInstance().getUid());
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
                    if(loginBinding.emailL.getText().toString().trim().isEmpty() && !loginBinding.passwordL.getText().toString().trim().isEmpty())
                    {
                        loginBinding.emailL.setError("Enter your email");
                        loginBinding.emailL.requestFocus();
                        progressDialog.dismiss();
                        return;
                    }
                    else if(!loginBinding.emailL.getText().toString().trim().isEmpty() && loginBinding.passwordL.getText().toString().trim().isEmpty())
                    {
                        loginBinding.passwordL.setError("Enter your password");
                        loginBinding.passwordL.requestFocus();
                        progressDialog.dismiss();
                        return;
                    }
                    else
                    {
                        loginBinding.emailL.setError("Enter your email");
                        loginBinding.passwordL.setError("Enter your password");
                        loginBinding.emailL.requestFocus();
                        loginBinding.passwordL.requestFocus();
                        progressDialog.dismiss();
                        return;
                    }
                }
            }
        });

        loginBinding.googleL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

    }

/*
   public void login(View v) throws Exception {

        //   EditText phone = (EditText)(findViewById(R.id.editTextTextPersonName));
        //   EditText password = (EditText)(findViewById(R.id.editTextTextPassword));

        //   final String Phone = phone.getText().toString();
        //  String Password = password.getText().toString();

        //  if(!Phone.isEmpty() && !Password.isEmpty())
        {
            Map<String, String> params = new HashMap<String, String>();

            //    String pw = encrypt(Password);

            //  params.put("phone", Phone);
            //   params.put("password", pw);

            final RequestQueue requestQueue = Volley.newRequestQueue(this);

            JSONObject object = new JSONObject(params);

           / JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url_logincheck, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {

                    try {
                        Toast.makeText(getApplicationContext(), response.getString("result"), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        if(response.getString("result").equals("Successfully logged in!"))
                        {
                            SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
               //             sp.edit().putInt("lc",1).apply();
               //             sp.edit().putString("phone",Phone).apply();

                            Intent i = new Intent(LoginClass.this, MainActivity.class);
                            startActivity(i);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            requestQueue.add(objectRequest);
        }
            //else
            {
                Toast.makeText(getApplicationContext(), "Please enter all the fields!", Toast.LENGTH_SHORT).show();
            }
        }
    }
*/

    public void signupintent(View v)
    {
        Intent i = new Intent(LoginClass.this, signupClass.class);
        startActivity(i);
    }

   /* public static String encrypt(String value) throws Exception
    {
        Key key = generateKey();
        Cipher cipher = Cipher.getInstance(LoginClass.ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encryptedByteValue = cipher.doFinal(value.getBytes("utf-8"));
        String encryptedValue64 = Base64.encodeToString(encryptedByteValue, Base64.DEFAULT);
        return encryptedValue64;

    }
    private static Key generateKey() throws Exception
    {
        Key key = new SecretKeySpec(LoginClass.KEY.getBytes(),LoginClass.ALGORITHM);
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
                // Google Sign In was successful.
                GoogleSignInAccount account = task.getResult(ApiException.class);
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
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                         //   progressDialog.dismiss();
                            final FirebaseUser user = auth.getCurrentUser();

                            database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override                 //to check if the user has already signed up with that google account
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.hasChild(user.getUid())) //if not,add it to db
                                    {
                                         Users users = new Users();
                                         users.setUserId(user.getUid());
                                         users.setUserName(user.getDisplayName());
                                         users.setProfilepic(user.getPhotoUrl().toString());

                                        database.getReference().child("Users").child(user.getUid()).setValue(users);
                                        Toast.makeText(getApplicationContext(),"Signed in with google!",Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

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
                    Toast.makeText(getApplicationContext(),"Sign in successful!",Toast.LENGTH_SHORT).show();
                    SharedPreferences sp = getSharedPreferences("login",MODE_PRIVATE);
                    sp.edit().putInt("lc",1).apply();

                    Intent i = new Intent(LoginClass.this, MainActivity.class);
                    startActivity(i);
                }
                else if(!snapshot.child("phoneNumber").exists() && snapshot.child("userName").exists()) //google signup or email signup
                {
                    Intent i = new Intent(LoginClass.this, PhoneVerification.class);
                    i.putExtra("code","44"); //determines if this is an account linking case or new independent signin/up using phone
                    startActivity(i);
                }
                else if (snapshot.child("phoneNumber").exists() && !snapshot.child("userName").exists())  //signup using phone
                        {
                            Intent i = new Intent(LoginClass.this, SettingsActivity.class);
                            startActivity(i);
                        }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}
