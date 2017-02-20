package com.fexcon.collectionbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private EditText email;
    private EditText password;
    private Boolean registeredUser;
    private Button btnLoginOrRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("CollectionBuddy", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("CollectionBuddy", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };

        SharedPreferences settings = getSharedPreferences("SHARED_PREFS", 0);

       registeredUser = settings.getBoolean("REGISTERED_USER", false);

        btnLoginOrRegister = (Button)findViewById(R.id.btnLogin);
        if(registeredUser){
            btnLoginOrRegister.setText("Login");
        }else{
            btnLoginOrRegister.setText("Register");
        }
        btnLoginOrRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(registeredUser){
                    if(!email.getText().toString().isEmpty() && !email.getText().toString().isEmpty()) {
                        userLogin();
                    }
                }else{
                    userRegister();
                }
            }
        });



    }

    public void userRegister(){
        mAuth.createUserWithEmailAndPassword(email.getText().toString(), email.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("CollectionBuddy", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {
                            /** Shared Preferences **/
                            SharedPreferences settings = getSharedPreferences("SHARED_PREFS", 0); // 0 - for private mode
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("REGISTERED_USER", true);
                            editor.apply();

                            btnLoginOrRegister.setText("Login");
                            registeredUser = true;


                            Toast.makeText(getApplicationContext(),"Registration Success!",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(),"Registration Failed!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void userLogin(){
        mAuth.signInWithEmailAndPassword(email.getText().toString(), email.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("CollectionBuddy", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (task.isSuccessful()) {

                            SharedPreferences settings = getSharedPreferences("SHARED_PREFS", 0); // 0 - for private mode
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("LOGGED_IN_USER", true);
                            editor.apply();

                            Toast.makeText(getApplicationContext(),"Login Success!",Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                        }else{
                            Toast.makeText(getApplicationContext(),"Login Failed!",Toast.LENGTH_SHORT).show();
                            Log.w("CollectionBuddy", "signInWithEmail:failed", task.getException());
                        }

                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

}
