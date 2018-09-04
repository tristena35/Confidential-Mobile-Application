package com.aguilartristen.confidential;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ChangeUsernameActivity extends AppCompatActivity {

    //TextInputLayouts Entered
    private TextView mCurrentUsername;
    private TextInputLayout mNewUsername;

    //Button to Register
    private Button mUpdateButton;

    //Refers to the Database of the chosen user
    private DatabaseReference mUserDatabase;

    //Progress Bar
    private ProgressDialog mRegProgress;

    //Toolbar
    private Toolbar mToolbar;

    //Firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference mAllUsersDatabase;

    private String CurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_username);

        //Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.change_username_toolbar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.change_username_custom_bar, null);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(action_bar_view);

        //Gets current user id of user
        CurrentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Firebase Database Objects
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(CurrentUserID);
        mAllUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //Android Fields
        mCurrentUsername = (TextView) findViewById(R.id.change_username_current);
        mNewUsername = (TextInputLayout) findViewById(R.id.change_username_new);
        mUpdateButton = (Button) findViewById(R.id.change_username_btn);

        //----GETTING CURRENT USERNAME----
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String currentUsername = dataSnapshot.child("name").getValue().toString();

                mCurrentUsername.setText(currentUsername);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Progress bar
        mRegProgress = new ProgressDialog(this);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //----DO A CHECK TO MAKE SURE USERNAME ISN'T TAKEN

                String usernameEntered = mNewUsername.getEditText().getText().toString();

                if(!usernameEntered.isEmpty()){

                    mUserDatabase.child("name").setValue(usernameEntered);
                    mNewUsername.setError("");
                    Toast.makeText(ChangeUsernameActivity.this,"Username successfully changed",Toast.LENGTH_SHORT).show();

                }else{

                    mNewUsername.setError("Please enter a valid Username");

                }

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


}
