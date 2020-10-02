package com.aguilartristen.confidential;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class ChangePasswordActivity extends AppCompatActivity {

    // TextInputLayouts Entered
    private TextInputLayout mCurrentPassword;
    private TextInputLayout mNewPassword;
    private TextInputLayout mConfirmPassword;

    // Button to Register
    private Button mUpdateButton;

    // Progress Bar
    private ProgressDialog mRegProgress;

    // Toolbar
    private Toolbar mToolbar;

    // Firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.change_password_toolbar);
        setSupportActionBar(mToolbar);

        // This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.change_password_custom_bar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);

        //Android Fields
        mCurrentPassword = (TextInputLayout) findViewById(R.id.change_password_current);
        mNewPassword = (TextInputLayout) findViewById(R.id.change_password_new);
        mConfirmPassword = (TextInputLayout) findViewById(R.id.change_password_verify);
        mUpdateButton = (Button) findViewById(R.id.change_password_btn);

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        // Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Progress bar
        mRegProgress = new ProgressDialog(this);
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
}
