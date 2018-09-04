package com.aguilartristen.confidential;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MyAccountActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabase,mFriendDatabase;
    private FirebaseUser mCurrentUser;

    //FirebaseAuth Object to check for Online status
    private FirebaseAuth mAuth;
    //DatabaseRef to point to users to get current user for online status

    //Toolbar
    private Toolbar mToolbar;

    //Android Layout
    private CircleImageView mDisplayImage,mAnonymousImage;
    private TextView mDisplayName;
    private TextView mStatus;
    private TextView mConfidantCount;
    private TextView mTitleView;
    private Switch mConfidentialSwitch;

    private Button mStatusBtn;
    private Button mImageBtn;
    private Button mConfidentialModeBtn;

    private static final int GALLERY_PICK = 1;

    //Storage Firebase
    private StorageReference mImagesStorage;

    //Progress Dialog
    private ProgressDialog mProgressDialog;

    //Array that is necessary for uploading the thumb images
    byte[] thumb_byte;

    //Variable to check for confidentialMode
    private String mConfidential_mode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        mToolbar = (Toolbar) findViewById(R.id.my_account_page_toolbar);
        setSupportActionBar(mToolbar);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.my_account_custom_bar, null);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);

        mAuth = FirebaseAuth.getInstance();

        // Name, Status, Profile Image

        mDisplayImage = (CircleImageView) findViewById(R.id.my_account_image);
        mAnonymousImage = (CircleImageView)findViewById(R.id.my_account_anonymous_image);
        mDisplayName = (TextView) findViewById(R.id.my_account_display_name);
        mStatus = (TextView) findViewById(R.id.my_account_status);
        mConfidantCount = (TextView) findViewById(R.id.my_account_confidants_counts);

        mConfidentialSwitch = (Switch) findViewById(R.id.my_account_switch);
        mStatusBtn = (Button) findViewById(R.id.my_account_change_status);
        mImageBtn = (Button) findViewById(R.id.my_account_change_image);
        //mConfidentialModeBtn = (Button)findViewById(R.id.settings_confidential_btn);

        //Custom Bar
        mTitleView = (TextView) findViewById(R.id.my_account_bar_title);

        //Profile Storage
        mImagesStorage = FirebaseStorage.getInstance().getReference();

        //Getting User
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        String current_uid = mCurrentUser.getUid();

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        //Getting Ref of users logged in
        //mConfidentialUsersRef = FirebaseDatabase.getInstance().getReference().child("Users_Confidential").child(mAuth.getCurrentUser().getUid());
        //Use this reference to get all information from user
        //mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        //Represents not in confidential mode
        mConfidential_mode = "no";

        //Enables offline capabilities for the Settings Activity
        mUserDatabase.keepSynced(true);

        countFriends();


        //----Loading Currents Users Stuff

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);
                //mTitleView.setText(name);

                if(!image.equals("default")){

                    Picasso.with(MyAccountActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.user_icon).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(MyAccountActivity.this).load(image).placeholder(R.drawable.user_icon).into(mDisplayImage);
                            Log.d("PROFILE_IMAGE", "Error");

                        }
                    });
                }


                //------Checking Confidential Mode-------


                if(dataSnapshot.hasChild("confidential_mode")){

                    String confidential_type = dataSnapshot.child("confidential_mode").getValue().toString();

                    if(confidential_type.equals("false")){

                        //If not in confidential mode
                        mConfidential_mode = "no";
                        mConfidentialSwitch.setText("STANDARD MODE");
                        mConfidentialSwitch.setTextColor(Color.BLACK);

                    }
                    else if(confidential_type.equals("true")){

                        //If already in confidential mode, should give us the option to go back
                        mConfidential_mode = "yes";
                        mConfidentialSwitch.setText("CONFIDENTIAL MODE");
                        mConfidentialSwitch.setTextColor(Color.RED);
                        mConfidentialSwitch.setChecked(true);
                        mAnonymousImage.setVisibility(View.VISIBLE);

                    }

                }else{

                   Log.d("CONFIDENTIAL_MODE", "No Confidential_Mode value");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d("CONFIDENTIAL_MODE",databaseError.getMessage());

            }

        });

        mConfidentialSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                Log.d("CHECK", "state: " + isChecked);

                if(isChecked){

                    //-----Turn on Confidential Mode
                    mUserDatabase.child("confidential_mode").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(MyAccountActivity.this,"Confidential Mode Enabled",Toast.LENGTH_SHORT).show();
                            mConfidential_mode = "yes";
                            mConfidentialSwitch.setText("CONFIDENTIAL MODE");
                            mConfidentialSwitch.setTextColor(Color.RED);
                            mAnonymousImage.setVisibility(View.VISIBLE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MyAccountActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

                        }
                    });

                }else{

                    //-----Put Info into databaseRef
                    mUserDatabase.child("confidential_mode").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(MyAccountActivity.this, "Confidential Mode Disabled", Toast.LENGTH_SHORT).show();
                            mConfidential_mode = "no";
                            mConfidentialSwitch.setText("STANDARD MODE");
                            mConfidentialSwitch.setTextColor(Color.BLACK);
                            mAnonymousImage.setVisibility(View.INVISIBLE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(MyAccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        }
                    });
                }
            }
        });

        /*-----CONFIDENTIAL MODE-----
        mConfidentialModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Once the user taps on the button once, they cannot again, until success.
                mConfidentialModeBtn.setEnabled(false);

                if(mConfidential_mode.equals("no")){

                    //-----Put Info into databaseRef
                    mUserDatabase.child("confidential_mode").setValue("true").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(MyAccountActivity.this,"Confidential Mode Enabled",Toast.LENGTH_SHORT).show();

                            mConfidentialModeBtn.setEnabled(true);

                            mConfidential_mode = "yes";

                            mConfidentialModeBtn.setText("STANDARD MODE");

                            mAnonymousImage.setVisibility(View.VISIBLE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mConfidentialModeBtn.setEnabled(true);

                            Toast.makeText(MyAccountActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

                        }
                    });

                }

                if(mConfidential_mode.equals("yes")){

                    //-----Put Info into databaseRef
                    mUserDatabase.child("confidential_mode").setValue("false").addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Toast.makeText(MyAccountActivity.this,"Confidential Mode Disabled",Toast.LENGTH_SHORT).show();

                            mConfidentialModeBtn.setEnabled(true);

                            mConfidential_mode = "no";

                            mConfidentialModeBtn.setText("CONFIDENTIAL MODE");

                            mAnonymousImage.setVisibility(View.INVISIBLE);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            mConfidentialModeBtn.setEnabled(true);

                            Toast.makeText(MyAccountActivity.this,"Something went wrong",Toast.LENGTH_SHORT).show();

                        }
                    });

                }

            }
        });
        */

        mStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status_value = mStatus.getText().toString();


                Intent statusIntent = new Intent(MyAccountActivity.this,StatusActivity.class);

                //This adds the current status to the text box in StatusActivity
                statusIntent.putExtra("status_value",status_value);
                startActivity(statusIntent);

            }
        });

        mImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //------USE THIS WHEN GOING STRAIGHT GALLERY FOR Profile image

                /*Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
                */

                //****USE EITHER ONE OR THE OTHER*****

                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MyAccountActivity.this);

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // super.onCreateOptionsMenu(menu);   I commented this out for it to work, but why
        getMenuInflater().inflate(R.menu.my_account_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.my_account_settings_option){
            Intent settingsIntent = new Intent(MyAccountActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //This function is responsible for retrieving uploaded image

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            //This line gets the image
            Uri imageUri = data.getData();

            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

            //Toast.makeText(MyAccountActivity.this, imageUri, Toast.LENGTH_LONG).show();

        }

        //This checks if the image provided is from the crop Activity
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                //Once the image is recieved, then make the progress dialog
                mProgressDialog = new ProgressDialog(MyAccountActivity.this);
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                String current_user_id = mCurrentUser.getUid();

                try {

                    //This creates a bitmap using the library we imported,
                    //and compressors the image for the thumb_image
                    Bitmap thumb_image = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();

                    Log.d("UPDATE_PRO_IMAGE", "Success4");

                } catch (IOException e) {

                    e.printStackTrace();

                }

                StorageReference filepath = mImagesStorage.child("profile_images").child(current_user_id + "jpg");
                //Creates a new child in storage for thumb_images
                final StorageReference thumb_filepath = mImagesStorage.child("profile_images").child("thumb_images").child(current_user_id + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if(task.isSuccessful()) {

                            //Toast.makeText(MyAccountActivity.this, "Working", Toast.LENGTH_LONG).show();
                            //getDownloadURL returns a url, so you need to convert to string
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadURL = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful()){

                                        Map updateHashMap = new HashMap<>();
                                        updateHashMap.put("image", download_url);
                                        updateHashMap.put("thumb_image", thumb_downloadURL);

                                        //Here we get the database reference, specify under which child
                                        //we want to change the value, and then set that value
                                        //Then we add the add the listener to check if the data
                                        //was uploaded successfully
                                        mUserDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    mProgressDialog.dismiss();

                                                    //Side Note: LENGTH_LONG shows for 3.5 seconds
                                                    //           LENGTH_SHORT shows for 2.0 seconds
                                                    Toast.makeText(MyAccountActivity.this,"Success Uploading",Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        });

                                    } else{

                                        Toast.makeText(MyAccountActivity.this,"Error Uploading Thumbnail",Toast.LENGTH_LONG).show();
                                        mProgressDialog.dismiss();

                                    }

                                }
                            });

                        }
                        else{

                            Toast.makeText(MyAccountActivity.this,"Error in uploading",Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
                Log.d("UPDATE_PRO_IMAGE", error.toString());
                Toast.makeText(MyAccountActivity.this,"Error in uploading this Picture",Toast.LENGTH_LONG).show();

            }
        }

    }

    //This was to be used to make a name for the image, but then we
    //just used the current user's id
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void countFriends() {

        String current_uid = mCurrentUser.getUid();

        //Getting total number of friends
        mFriendDatabase.child(current_uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //The line below returns a long
                long total_confidants = dataSnapshot.getChildrenCount();

                if(total_confidants > 0) {
                    mConfidantCount.setText((int) total_confidants + "");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
