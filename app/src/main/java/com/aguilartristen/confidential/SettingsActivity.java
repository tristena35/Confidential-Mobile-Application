package com.aguilartristen.confidential;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar mToolbar;

    //Database Reference
    private DatabaseReference mUsersRef;

    //TextViews
    private TextView mChangePassword;
    private TextView mChangeUsername;
    private TextView mLogOut;

    //Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        //Getting Ref
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        //Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.settings_custom_bar, null);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(action_bar_view);

        //TextViews
        mChangePassword = (TextView)findViewById(R.id.settings_change_password_text);
        mChangeUsername = (TextView)findViewById(R.id.settings_change_username_text);
        mLogOut = (TextView)findViewById(R.id.settings_log_out_text);

        //When you click on ChangePassword TextView
        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent changePassIntent = new Intent(SettingsActivity.this,ChangePasswordActivity.class);
                startActivity(changePassIntent);

            }
        });

        mChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent changePassIntent = new Intent(SettingsActivity.this,ChangeUsernameActivity.class);
                startActivity(changePassIntent);

            }
        });

        //When you click on ChangePassword TextView
        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);
                FirebaseAuth.getInstance().signOut();
                sendToStart();

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void sendToStart(){

        Intent startIntent = new Intent(SettingsActivity.this,StartActivity.class);
        startActivity(startIntent);

    }

}