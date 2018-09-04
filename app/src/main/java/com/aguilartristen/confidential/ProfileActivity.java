package com.aguilartristen.confidential;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    //TextViews
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount,mProfileFriends;

    //ImageViews
    private ImageView mProfileImageView;

    //CircleImageView
    private CircleImageView mProfileImage;

    //Button
    private Button mSendFriendRequestBtn;

    //Button
    private Button mProfileDeclineReqBtn;

    //Refers to the Database of the chosen user
    private DatabaseReference mUserDatabase;
    //Database object for recording friend requests
    private DatabaseReference mFriendReqDatabase;
    //Database object to keep track of users that are friends
    private DatabaseReference mFriendDatabase;
    //Database for Notifications
    private DatabaseReference mNotificationDatabase;

    private DatabaseReference mRootRef;

    //This Variable is to keep track of the current user logged in
    private FirebaseUser mCurrent_user;

    //Progress Bar
    private ProgressDialog mProgressDialog;

    //Variable to check for friendship
    private String mCurrent_state;

    //FirebaseAuth Object to check for Online status
    private FirebaseAuth mAuth;
    //DatabaseRef to point to users to get current user for online status
    private DatabaseReference mUsersRef;

    //Toolbar
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.profile_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.profile_custom_bar, null);

        //Setting up custom bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);

        //Passes the users_id of the profile we are looking at from the previous intent (UsersActivity)
        final String user_Id = getIntent().getStringExtra("user_id");
        //final String user_clicked_name = getIntent().getStringExtra("user_clicked_name");

        //Firebase Database Objects
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_Id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        //Getting Ref of users logged in
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        //Gets an instance of the user who is logged in
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        //Matching layout objects with variables
        mProfileImage = (CircleImageView) findViewById(R.id.search_profile_image);
        //mProfileImageView = (ImageView) findViewById(R.id.profile_image);
        //mProfileName = (TextView) findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_user_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.confidants_counts);
        mProfileFriends = (TextView) findViewById(R.id.profile_numberOfFriends);
        mSendFriendRequestBtn = (Button) findViewById(R.id.profile_send_req_btn);
        mProfileDeclineReqBtn = (Button) findViewById(R.id.profile_decline_req_btn);

        //Putting there name on the Top
        mProfileName = (TextView) findViewById(R.id.custom_bar_profile_page_username);

        //This Button shouldn't appear unless the user has been sent a request
        mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
        mProfileDeclineReqBtn.setEnabled(false);

        //Represents not friends
        mCurrent_state = "not_friends";

        //Progress Dialog
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User");
        mProgressDialog.setMessage("Please wait while we load the user's information");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        //Getting total number of friends
        mFriendDatabase.child(user_Id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //The line below returns a long
                long total_confidants = dataSnapshot.getChildrenCount();

                if(total_confidants > 0) {
                    mProfileFriendsCount.setText((int) total_confidants + "");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //These lines retrieve the data specified from the users database using
                //the dataSnapshot
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String display_image = dataSnapshot.child("image").getValue().toString();

                //Puts their username on top bar
                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                //Puts the individual users image into the image view
                Picasso.with(ProfileActivity.this).load(display_image).placeholder(R.drawable.user_icon).into(mProfileImage);







                //--------- FRIENDS LIST/REQUEST RECEIVED FEATURE ------------//

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //If our user Object has the ID of the users page we are on
                        if(dataSnapshot.hasChild(user_Id)){

                            //Here you are checking if they have sent you a friend request
                            String req_type = dataSnapshot.child(user_Id).child("request_type").getValue().toString();

                            //If we have 'received' a request from this specific user.
                            if(req_type.equals("received")){

                                mCurrent_state = "req_received";
                                mSendFriendRequestBtn.setText("Accept Request");

                                mProfileDeclineReqBtn.setVisibility(View.VISIBLE);
                                mProfileDeclineReqBtn.setEnabled(true);

                            }
                            else if(req_type.equals("sent")){

                                //if we sent a friend request already, the button will give the option to cancel it
                                mCurrent_state = "req_sent";
                                mSendFriendRequestBtn.setText("Cancel Request");


                            }

                            mProgressDialog.dismiss();

                        }else{ //Already Friends

                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_Id)){

                                        mCurrent_state = "friends";
                                        mSendFriendRequestBtn.setText("Disconnect");

                                    }

                                    mProgressDialog.dismiss();

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    mProgressDialog.dismiss();

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.d("Confidential","You have an error getting users profile 2");

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d("Confidential","You have an error getting users profile");

            }


        });





        //This is the Send Friend Request Button and it creates a received and sent field in firebase
        //according to the user who sent the friend request
        mSendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Once the user taps on the button once, they cannot again.
                mSendFriendRequestBtn.setEnabled(false);

                // - ----------- NOT FRIENDS STATE ----------- - //

                if(mCurrent_state.equals("not_friends")){


                    DatabaseReference newNotificatioRef = mRootRef.child("notifications").child(user_Id).push();
                    String newNotificationID = newNotificatioRef.getKey();

                    Date c = Calendar.getInstance().getTime();
                    //SimpleDateFormat date = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");
                    SimpleDateFormat date = new SimpleDateFormat("MMMM dd, yyyy");
                    String getTimeNow = date.format(c);


                    /*
                     *We want to store information in this Notification Database
                     *So we use a HashMap to store information all in one object
                    */
                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("From", mCurrent_user.getUid());
                    notificationData.put("Type", "Request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_Id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_Id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_Id + "/date", getTimeNow);
                    requestMap.put("Friend_req/" + user_Id + "/" + mCurrent_user.getUid() + "/date", getTimeNow);
                    requestMap.put("Notifications/" + user_Id + "/" + newNotificationID, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(ProfileActivity.this, "Something went wrong adding this person", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(ProfileActivity.this,"Request Sent", Toast.LENGTH_SHORT).show();
                            }


                            mSendFriendRequestBtn.setEnabled(true);

                            mCurrent_state = "req_sent";

                            mSendFriendRequestBtn.setText("Cancel Request");

                        }
                    });

                }

                // - ----------- CANCEL REQUEST STATE ----------- - //
                /*If this if statement is true, that means they have already sent the friend request
                so we will not remove the friend request and reset the button if it is click on.
                */
                if(mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_Id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){

                                mFriendReqDatabase.child(user_Id).child(mCurrent_user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()) {

                                            mSendFriendRequestBtn.setEnabled(true);
                                            mCurrent_state = "not_friends";
                                            mSendFriendRequestBtn.setText("Send Request");

                                            Toast.makeText(ProfileActivity.this,"Request Cancelled", Toast.LENGTH_SHORT).show();

                                        }
                                        else{

                                            Toast.makeText(ProfileActivity.this, "Problem Canceling Request", Toast.LENGTH_SHORT).show();

                                        }

                                    }
                                });

                            }
                            else{
                                Toast.makeText(ProfileActivity.this, "Error removing request", Toast.LENGTH_LONG).show();
                            }

                        }
                    });

                }


                //------- REQ RECEIVED STATE ----------//
                //------- ADDING FRIEND ------ //

                if(mCurrent_state.equals("req_received")){

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_Id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_Id + "/" + mCurrent_user.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_Id, null);
                    friendsMap.put("Friend_req/" + user_Id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mSendFriendRequestBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mSendFriendRequestBtn.setText("Disconnect");

                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);

                                //Go back to 'user_clicked_name'
                                //Toast.makeText(ProfileActivity.this," You and " + user_clicked_name + " are now friends!",Toast.LENGTH_LONG).show();
                                Toast.makeText(ProfileActivity.this,"Connection Made",Toast.LENGTH_LONG).show();


                            }else{

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

                //---------- UNFRIEND -----------//

                if(mCurrent_state.equals("friends")){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_Id, null);
                    unfriendMap.put("Friends/" + user_Id + "/" + mCurrent_user.getUid(), null);


                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrent_state = "not_friends";
                                mSendFriendRequestBtn.setText("Send Request");

                                Toast.makeText(ProfileActivity.this,"Disconnected", Toast.LENGTH_SHORT).show();

                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);

                            }else{

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            mSendFriendRequestBtn.setEnabled(true);

                        }
                    });

                }



            }
        });





        //----- DECLINES FRIEND REQUEST ---------//

        mProfileDeclineReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCurrent_state.equals("req_received")) {

                    Map declinefriendMap = new HashMap();
                    declinefriendMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_Id, null);
                    declinefriendMap.put("Friend_req/" + user_Id + "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(declinefriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){

                                mCurrent_state = "not_friends";
                                mSendFriendRequestBtn.setText("Send Request");

                                mProfileDeclineReqBtn.setVisibility(View.INVISIBLE);
                                mProfileDeclineReqBtn.setEnabled(false);

                            }else{

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            mSendFriendRequestBtn.setEnabled(true);

                        }

                    });

                }

            }

        });






    } //onCreate

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
