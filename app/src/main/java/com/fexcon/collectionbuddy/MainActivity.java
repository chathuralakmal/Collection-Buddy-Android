package com.fexcon.collectionbuddy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private Button btnCoin;
    private TextView myContribution;
    private TextView totalContribution;

    private boolean valueUpdated;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getSharedPreferences("SHARED_PREFS", 0);
        boolean loggedInUser = settings.getBoolean("LOGGED_IN_USER", false);

        if(!loggedInUser){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else{
            mDatabase = FirebaseDatabase.getInstance().getReference();

            mAuth = FirebaseAuth.getInstance();

            btnCoin = (Button)findViewById(R.id.btnCoin);
            myContribution = (TextView)findViewById(R.id.myContribution);
            totalContribution = (TextView)findViewById(R.id.totalContribution);

            Button btnLogout = (Button)findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth.getInstance().signOut();
                    SharedPreferences settings = getSharedPreferences("SHARED_PREFS", 0); // 0 - for private mode
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("LOGGED_IN_USER", false);
                    editor.apply();

                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });


            user = FirebaseAuth.getInstance().getCurrentUser();

            setTitle("User : "+user.getEmail());
            final MediaPlayer mp = MediaPlayer.create(this, R.raw.cha_ching_sound);
            btnCoin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    valueUpdated = false;
                    mDatabase.child("collection").child(user.getUid()).child("value").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.e("Checking Data","-->"+dataSnapshot);
                            if(!valueUpdated){
                                mp.start();
                                String currentValue = "0";
                                if(dataSnapshot.exists()){
                                    currentValue = dataSnapshot.getValue().toString();
                                }

                                Integer myValue = Integer.parseInt(currentValue) + 10;
                                mDatabase.child("collection").child(user.getUid()).child("name").setValue(user.getEmail());
                                mDatabase.child("collection").child(user.getUid()).child("value").setValue(String.valueOf(myValue));
                                valueUpdated = true;
                                loadData();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });


            loadData();
        }



    }


    public void loadData(){
        /** Calculating My Contribution **/
        mDatabase.child("collection").child(user.getUid()).child("value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    myContribution.setText("My Contribution : " + dataSnapshot.getValue().toString()+"/=");
                }else{
                    myContribution.setText("My Contribution : 0/=");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /** Calculating Total Contribution **/

        mDatabase.child("collection").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Integer totalValue = 0;
                for(DataSnapshot snap : dataSnapshot.getChildren()){
                    for(DataSnapshot childSnap : snap.getChildren()){
                        if(childSnap.getKey().equals("value")) {
                            Log.e("Total", "Snapshot --> " + childSnap.getValue().toString());
                            totalValue = totalValue + Integer.parseInt(childSnap.getValue().toString());
                        }
                    }
                }
                totalContribution.setText("Total : "+totalValue+"/=");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



}
