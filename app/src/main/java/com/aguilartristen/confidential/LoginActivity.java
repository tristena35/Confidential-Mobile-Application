package com.aguilartristen.confidential;

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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private TextView mSignUp;

    private TextInputLayout mLoginEmail;
    private TextInputLayout mLoginPassword;
    private Button mLoginButton;

    //Progress Bar
    private ProgressDialog mLoginProgress;

    //Toolbar
    private Toolbar mToolbar;

    //Firebase Auth
    private FirebaseAuth mAuth;

    //Database Reference
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginProgress = new ProgressDialog(this);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mSignUp = (TextView)findViewById(R.id.login_signUp2);

        //Android Fields
        mLoginEmail = (TextInputLayout) findViewById(R.id.login_email);
        mLoginPassword = (TextInputLayout) findViewById(R.id.login_password);
        mLoginButton = (Button) findViewById(R.id.login_btn);

        //Firebase Auth

        mAuth = FirebaseAuth.getInstance();

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mLoginEmail.getEditText().getText().toString();
                String password = mLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials");
                    mLoginProgress.setCanceledOnTouchOutside(false);
                    mLoginProgress.show();

                    loginUser(email,password);
                }

            }
        });

        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignUp.setTextColor(Color.parseColor("#8B0000"));
                sendToSignUp();
            }
        });

    }

    //---------- LOGIN FEATURE ----------//

    private void loginUser(String loginEmail, String loginPassword) {
        mAuth.signInWithEmailAndPassword(loginEmail,loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Takes away Progress Dialog since your signed in
                    mLoginProgress.dismiss();

                    //Getting the token for the Notifications
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    //Gets Key for current user logged in
                    String current_user_id = mAuth.getCurrentUser().getUid();

                    //Gets into the 'Users' database, and going to the key of whichever user is logged in
                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                            //This line will stop the issue with the user pressing the back button on the main page,
                            //and coming back to the start page
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainIntent);
                            finish();

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(LoginActivity.this,"Failed to send a token when logging in",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    String error = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        error = "Invalid Email!";
                    } catch (FirebaseAuthInvalidCredentialsException e) { //Create rules for password in future
                        error = "Invalid Password!"; //Also add a confirm password
                    } catch (Exception e) {
                        error = "Default error!";
                        e.printStackTrace();
                    }
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
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

    private void sendToSignUp() {
        Intent startIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(startIntent);
        mSignUp.setTextColor(Color.parseColor("red"));
    }

}
