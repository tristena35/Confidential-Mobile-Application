package com.aguilartristen.confidential;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersRef;

    //Progress Dialog
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        //Getting Ref
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        String status_value = getIntent().getStringExtra("status_value");

        mStatus = (TextInputLayout) findViewById(R.id.status_input);
        mSaveBtn = (Button) findViewById(R.id.status_save_btn);

        mStatus.getEditText().setText(status_value);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Progress Dialog
                mProgressDialog = new ProgressDialog(StatusActivity.this);
                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("Please wait while we save the changes");
                mProgressDialog.show();

                String status = mStatus.getEditText().getText().toString();

                //We do not need to use HashMaps in order to update the status because that is only used
                //to create objects with many properties. In this case, it is only one property
                //status, which we can add individually.

                //Using uid first because uid is a child of mStatusDatabse which is the Users category
                mStatusDatabase.child(current_uid).child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            mProgressDialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Status Updated", Toast.LENGTH_SHORT).show();

                        }
                        else{

                            Toast.makeText(getApplicationContext(),"There was an error in saving changes", Toast.LENGTH_LONG).show();

                        }

                        mProgressDialog.dismiss();

                    }
                });

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        //Checks that when the user logged in, it will put online status to true
        //mUsersRef.child("online").setValue(true);

    }

    @Override
    protected void onStop() {
        super.onStop();

        //If app is closed then online should be set to false
        //mUsersRef.child("online").setValue(false);

    }

}
