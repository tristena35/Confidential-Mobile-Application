package com.aguilartristen.confidential;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MakePostActivity extends AppCompatActivity {

    //Database reference to Feed Database
    private DatabaseReference mFeedDatabaseRef, mFeedPostLikesDatabaseRef, mUserDatabase;

    //This Objects are all related to the chat_custom_bar layout
    private EditText mPostBox;
    private Button mSubmitPostButton;
    private Switch mPublicPrivateSwitch;

    //Firebase Auth Object
    private FirebaseAuth mAuth;

    //String Variable to hold current user'S ID
    private String mCurrentUserID;

    private String currentUserName;
    private String currentUsersThumbImage;
    private String privateOrPublic;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_to_feed);

        //Firebase Auth Object
        mAuth = FirebaseAuth.getInstance();

        //Getting Current Users ID
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        //Database Object
        mFeedDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFeedPostLikesDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);

        //Where the user enters the post message
        mPostBox = (EditText)findViewById(R.id.post_text_box);

        //Switch
        mPublicPrivateSwitch = (Switch)findViewById(R.id.switch_public_private);

        //Once button is clicked, post should sent to database, as well as on top of feed
        mSubmitPostButton = (Button)findViewById(R.id.post_submit_button);

        //String value for is the post is going to be public or private, default value is public
        privateOrPublic = "public";

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                currentUserName = dataSnapshot.child("name").getValue().toString();
                currentUsersThumbImage = dataSnapshot.child("thumb_image").getValue().toString();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d("Feed_Username",databaseError.getMessage());

            }

        });


        /*If the Switch is switched*/
        mPublicPrivateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // do something, the isChecked will be
                // true if the switch is in the On position

                Log.d("CHECK", "state: " + isChecked);

                if(isChecked){

                    Toast.makeText(MakePostActivity.this,"Post will be for confidants only",Toast.LENGTH_SHORT).show();
                    privateOrPublic = "private";
                    mPublicPrivateSwitch.setText("CONFIDANTS ONLY");
                    mPublicPrivateSwitch.setTextColor(Color.RED);

                }else{

                    //-----Put Info into databaseRef

                    Toast.makeText(MakePostActivity.this, "Public Post", Toast.LENGTH_SHORT).show();
                    privateOrPublic = "public";
                    mPublicPrivateSwitch.setText("PUBLIC");
                    mPublicPrivateSwitch.setTextColor(Color.BLACK);

                }

            }
        });


        /*
        -- WHERE THE UPLOAD HAPPENS --
         */
        mSubmitPostButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                final String postText = mPostBox.getText().toString();

                //If there is no message
                if(postText.isEmpty())

                    mPostBox.setError("Please enter a message.");

                else{

                    //GETS CURRENT TIME IN good format
                    SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
                    String currentTime = df.format(Calendar.getInstance().getTime());

                    Date c = Calendar.getInstance().getTime();
                    //SimpleDateFormat date = new SimpleDateFormat("MMMM dd, yyyy hh:mm a");
                    SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy");
                    String getTimeNow = date.format(c);

                    //DatabaseReference user_post_push = mFeedDatabaseRef.child("Feed_page").child(mCurrentUserID).push();

                    //final String push_id = user_post_push.getKey();

                    Map feedPostMap = new HashMap();
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/name", currentUserName);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/thumb_image", currentUsersThumbImage);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/message", postText);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/likes", 0); //I put the value as a set so that I can access all users that liked the post
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/dislikes", 0);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/time_posted", currentTime.toString());
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/date_posted", getTimeNow);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/timestamp", ServerValue.TIMESTAMP);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/privacy", privateOrPublic);

                    /*    --- HOW IT SHOULD WORK, Problem, keeping track of the post when someone likes it.

                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/name", currentUserName);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/thumb_image", currentUsersThumbImage);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/message", postText);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/likes", 0); //I put the value as a set so that I can access all users that liked the post
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/dislikes", 0);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/time_posted", currentTime.toString());
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/date_posted", getTimeNow);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/timestamp", ServerValue.TIMESTAMP);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/" + push_id + "/privacy", privateOrPublic);*/


                    mFeedDatabaseRef.updateChildren(feedPostMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){
                                Toast.makeText(MakePostActivity.this, "Something went wrong when posting, " +
                                        "try posting it again." + "", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(MakePostActivity.this,"Posted", Toast.LENGTH_SHORT).show();
                            }

                            //Once posted, should go to the Feed page.
                            Intent feedActivity = new Intent(MakePostActivity.this,FeedActivity.class);
                            startActivity(feedActivity);
                            finish();
                        }
                    });

                }
            }
        });

    }

}
