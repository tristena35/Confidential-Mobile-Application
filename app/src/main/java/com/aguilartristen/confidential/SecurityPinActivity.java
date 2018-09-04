package com.aguilartristen.confidential;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SecurityPinActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private DatabaseReference mUsersPinRef;
    private DatabaseReference mUsersRef;

    private FirebaseUser mCurrentUser;

    private String current_uid;

    private TextView mTitle;
    private TextView mLogOut;
    private TextView mFingerPrint;
    private EditText mPinBox;
    private Button mOkBtn;

    private String currentUserName;
    private String currentUserImage;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_pin);

        //Layout Elements
        mTitle = (TextView)findViewById(R.id.security_title);
        mLogOut = (TextView)findViewById(R.id.security_log_out);
        mFingerPrint = (TextView)findViewById(R.id.security_finger_print_scanner);
        mPinBox = (EditText)findViewById(R.id.security_pin_box);
        mOkBtn = (Button)findViewById(R.id.security_ok_btn);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            //Getting their reference
            mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
            //Getting Pin Ref
            mUsersPinRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid()).child("pin");

        }


        /*Getting Username and Profile image ---NEEDS WORK---:


        //Getting User
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //String to get current users UID
        current_uid = mCurrentUser.getUid();

        Log.d("CURRENT USER", "The current UID is: " + current_uid);

        mUsersRef.child(current_uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Log.d("CURRENT USER", (String) dataSnapshot.child("name").getValue());

                try {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    //String profileImage = dataSnapshot.child("image").getValue().toString();
                    currentUserName = userName;
                }catch(NullPointerException e){

                    mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);
                    FirebaseAuth.getInstance().signOut();
                    sendToStart();

                }

                //currentUserImage = profileImage;

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d("RETRIEVAL_FAILED", databaseError.getMessage());

            }

        });

        */

        mFingerPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(SecurityPinActivity.this,"Clicked",Toast.LENGTH_SHORT);

                Intent fingerprintScanner = new Intent(SecurityPinActivity.this,FingerprintScannerActivity.class);

                fingerprintScanner.putExtra("user_name", currentUserName);
                //fingerprintScanner.putExtra("thumb_image", currentUserImage);

                startActivity(fingerprintScanner);

            }
        });

        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                sendToStart();
                mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);

            }
        });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String pinEntered = (mPinBox.getText()).toString();

                //Getting total number of friends
                mUsersPinRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String actualPin = dataSnapshot.getValue().toString();

                        //If the pin is correct, go to MainActivity
                        if(pinEntered.equals(actualPin)) {
                            sendToMain();
                            //Toast.makeText(SecurityPinActivity.this,"Welcome Confidant",Toast.LENGTH_SHORT).show();
                        }else{
                            mPinBox.setText("");
                            Toast.makeText(SecurityPinActivity.this,"Invalid Pin", Toast.LENGTH_SHORT).show();
                            Log.d("PINS", "Pin from Firebase " + actualPin);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Toast.makeText(SecurityPinActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                    }
                });

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendToStart();

        } else {

            Log.d("UID",currentUser.getUid());

        }

    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(SecurityPinActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish(); //So they cannot press back button
    }

    private void sendToStart() {
        Intent startIntent = new Intent(SecurityPinActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish(); //So they cannot press back button
    }

}
