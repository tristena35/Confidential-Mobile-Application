package com.aguilartristen.confidential;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MakePostActivity extends AppCompatActivity {

    //Database reference to Feed Database
    private DatabaseReference mFeedDatabaseRef, mUserDatabase;

    //This Objects are all related to the chat_custom_bar layout
    private EditText mPostBox;
    private Button mSubmitPostButton;

    //Firebase Auth Object
    private FirebaseAuth mAuth;

    //String Variable to hold current user'S ID
    private String mCurrentUserID;

    private String currentUserName;
    private String currentUsersThumbImage;

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
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);

        //Where the user enters the post message
        mPostBox = (EditText)findViewById(R.id.post_text_box);

        //Once button is clicked, post should sent to database, as well as on top of feed
        mSubmitPostButton = (Button)findViewById(R.id.post_submit_button);

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

                    Map feedPostMap = new HashMap();
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/name", currentUserName);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/thumb_image", currentUsersThumbImage);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/message", postText);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/likes", 0);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/dislikes", 0);
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/time_posted", currentTime.toString());
                    feedPostMap.put("Feed_page/" + mCurrentUserID + "/timestamp", ServerValue.TIMESTAMP);

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
