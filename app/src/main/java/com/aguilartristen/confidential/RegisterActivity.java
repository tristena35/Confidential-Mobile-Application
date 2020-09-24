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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends AppCompatActivity {

    //TextInputLayouts Entered
    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private TextInputLayout mConfirmPass;
    private TextInputLayout mPin;

    //Button to Register
    private Button mRegisterButton;


    //Progress Bar
    private ProgressDialog mRegProgress;

    //Toolbar
    private Toolbar mToolbar;

    //Firebase Auth
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    //When it is initially 0, if it is valid, it will change to 1.
    private int PASSWORD_STATE = 0;
    private int CONFIRM_PASSWORD_STATE = 0;
    private int EMAIL_STATE = 0;
    private int USERNAME_STATE = 0;
    private int PIN_STATE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Android Fields
        mDisplayName = (TextInputLayout) findViewById(R.id.reg_display_name);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mConfirmPass = (TextInputLayout) findViewById(R.id.reg_confirm_password);
        mPin = (TextInputLayout) findViewById(R.id.reg_pin);
        mRegisterButton = (Button) findViewById(R.id.reg_create_btn);

        EditText mPasswordWatcher = (EditText)findViewById(R.id.reg_password_editText);
        EditText mConfirmPasswordWatcher = (EditText)findViewById(R.id.reg_confirm_password_editText);
        EditText mUsernameWatcher = (EditText)findViewById(R.id.reg_display_name_editText);
        EditText mEmailWatcher = (EditText)findViewById(R.id.reg_email_editText);
        EditText mPinWatcher = (EditText)findViewById(R.id.reg_pin_editText);



        /*
                HELPING USER VALIDATE USERNAME
         */
        mUsernameWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {

                    String username = mDisplayName.getEditText().getText().toString();

                    int counter = 0;

                    for (int i = 0; i < username.length(); i++) {

                        if(isSpecialCharacter(username.charAt(i)) || username.length() < 4) {//Counts number of '@' symbols
                            //mDisplayName.setError("Special characters not allowed");
                            counter++;
                        }

                    }

                    if(counter != 0){//INVALID
                        USERNAME_STATE = 0;
                        mDisplayName
                                .setError("At least 4 characters long | No special characters");
                    }else{ //VALID
                        USERNAME_STATE = 1;
                        mDisplayName.setError("");
                    }

                    Log.d("USERNAME_STATE", "State: " + USERNAME_STATE);

                }else{
                    mDisplayName.setError("");
                    USERNAME_STATE = 0;
                }

            }
        });




        /*
                HELPING USER VALIDATE PASSWORD
         */
        mPasswordWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                /*
                If the edit text is not empty
                 */
                if (!s.toString().isEmpty()) {

                    String password = mPassword.getEditText().getText().toString();

                    int countNumbers = 0;
                    int countLetter = 0;
                    int countSpecialChar = 0;

                    for (int i = 0; i < password.length(); i++) {

                        if (Character.isDigit(password.charAt(i))) {

                            countNumbers++;

                        }
                        else if(Character.isLetter(password.charAt(i))){

                            countLetter++;

                        }
                        else if(isSpecialCharacter(password.charAt(i))){

                            countSpecialChar++;

                        }
                        else if(Character.isWhitespace(password.charAt(i))){

                            mPassword.setError("Make sure there are no spaces.");
                            PASSWORD_STATE = 0;

                        }

                    }

                    if(countLetter == 0 && countNumbers == 0 && countSpecialChar == 0){
                        mPassword.setError("Enter Password");
                        PASSWORD_STATE = 0;
                    }
                    else if(countLetter < 4 && countNumbers < 2 && countSpecialChar < 1) {
                        mPassword.setError("Weak (At least 4 letters | 2 numbers | 1 special character)");
                        PASSWORD_STATE = 0;
                    }
                    else if(countLetter >= 4 && countNumbers >= 2 && countSpecialChar == 0) {
                        mPassword.setError("Better (Include a special character)");
                        PASSWORD_STATE = 0;
                    }
                    else if(countLetter >= 4 && countNumbers >= 2 && countSpecialChar >= 1) {
                        mPassword.setError("Strong");
                        PASSWORD_STATE = 1;
                    }

                    Log.d("PASSWORD_STATE", "State: " + PASSWORD_STATE);

                }else{
                    mPassword.setError("");
                    PASSWORD_STATE = 0;
                }

            }
        });




        /*
                HELPING USER VALIDATE CONFIRM PASSWORD
         */
        mConfirmPasswordWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                /*
                If the edit text is not empty
                 */
                if (!s.toString().isEmpty()) {

                    String password = mPassword.getEditText().getText().toString();

                    String confirm_password = mConfirmPass.getEditText().getText().toString();

                    if(!confirm_password.equals(password)){//INVALID

                        mConfirmPass.setError("Passwords do not match");
                        CONFIRM_PASSWORD_STATE = 0;

                    }else{//VALID

                        mConfirmPass.setError("");
                        CONFIRM_PASSWORD_STATE = 1;

                    }

                    Log.d("CONFIRM_PASSWORD_STATE", "State: " + CONFIRM_PASSWORD_STATE);

                }else{//Empty Text
                    mPassword.setError("");
                    CONFIRM_PASSWORD_STATE = 0;
                }

            }
        });




        /*
                HELPING USER VALIDATE EMAIL
         */
        mEmailWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {

                    String email = mEmail.getEditText().getText().toString();

                    int countAtSymbol = 0;

                    for (int i = 0; i < email.length(); i++) {

                        if(email.charAt(i) == '@')//Counts number of '@' symbols
                            countAtSymbol++;

                    }

                    if(countAtSymbol != 1){
                        mEmail.setError("Please enter a valid Email");
                        EMAIL_STATE = 0;
                    }else{ //VALID
                        EMAIL_STATE = 1;
                        mEmail.setError("");
                    }

                    Log.d("EMAIL_STATE", "State: " + EMAIL_STATE);

                }else{

                    mEmail.setError("");
                    EMAIL_STATE = 0;

                }

            }
        });



        /*
                HELPING USER VALIDATE PIN
         */
        mPinWatcher.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {

                    String pin = mPin.getEditText().getText().toString();

                    if(pin.length() < 4){

                        mPin.setError("Pin must be 4 characters long");
                        PIN_STATE = 0;

                    }else{ //VALID

                        PIN_STATE = 1;
                        mPin.setError("");

                    }

                    Log.d("PIN_STATE", "State: " + PIN_STATE);

                }else{
                    mPin.setError("");
                    PIN_STATE = 0;
                }

            }

        });




        //Progress bar
        mRegProgress = new ProgressDialog(this);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();



        /*
                WHEN THEY CLICK REGISTER
         */
        mRegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(USERNAME_STATE == 1 && EMAIL_STATE == 1 && PASSWORD_STATE == 1
                        && CONFIRM_PASSWORD_STATE == 1 && PIN_STATE == 1){ //All fields success

                    //Check to make sure the username is not already taken
                    String display_name = mDisplayName.getEditText().getText().toString();

                    String email = mEmail.getEditText().getText().toString();
                    String password = mPassword.getEditText().getText().toString();
                    String pin = mPin.getEditText().getText().toString();

                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();

                    register_user(display_name, email, password, pin);

                }
                else{ //If there was an error

                    Toast.makeText(RegisterActivity.this,
                            "Make sure all fields are filled in correctly", Toast.LENGTH_SHORT).show();

                }


            }
        });

    }

    /*Look into specific error calls in firebase for this registration process and call them based on the error.
    -For Example, if email is tristengmail.com missing '@', should throw and InvalidEmailException.
     */
    private void register_user(final String display_name, String email, String password, final String pin) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("Confidential", "Inside register_user");
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Confidential", "createUserWithEmail:success");

                            //Getting the user's unique UID
                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();

                            String uid = current_user.getUid();
                            //Getting the token for the Notifications
                            String deviceToken = FirebaseInstanceId.getInstance().getToken();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            //This HashMap is creating fields in the database
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("device_token",deviceToken);
                            userMap.put("name",display_name);
                            userMap.put("status","A new Confidant.");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("pin", pin);
                            userMap.put("confidential_mode","false");

                            //We are then setting those values to the database
                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        mRegProgress.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish(); // We do not want the back button to bring us back to register
                                    }

                                }
                            });

                            FirebaseUser user = mAuth.getCurrentUser();

                        }
                        else {
                            // If sign in fails, display a message to the user.
                            Log.w("Confidential", "createUserWithEmail:failure", task.getException());
                            mRegProgress.hide();
                            Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    /*
    Checks for a special character
     */

    private static boolean isSpecialCharacter(Character c)
    {
        return c != 32 &&	//not a space
                (c < 48 || c > 57) && 	//not a digit
                (c < 65 || c > 90) && 	//not an uppercase alphabet
                (c < 97 || c > 122);	//not a lowercase alphabet
    }

}
