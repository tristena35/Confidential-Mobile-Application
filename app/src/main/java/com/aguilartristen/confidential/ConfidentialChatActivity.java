package com.aguilartristen.confidential;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import android.os.CountDownTimer;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ConfidentialChatActivity extends AppCompatActivity {

    //User we are chatting with
    private String mConfidentialChatUser;

    //Toolbar
    private Toolbar mChatToolbar;

    //This is called the root ref because we are going to use this one reference for the whole database.
    private DatabaseReference mRootRef;

    //This Objects are all related to the chat_custom_bar layout
    private TextView mTitleView;
    private CircleImageView mProfileImage;

    //Firebase Auth Object
    private FirebaseAuth mAuth;
    //String Variable to hold current user'S ID
    private String mCurrentUserID;

    //Objects on bottom part of ChatActivity
    private ImageButton mChatSendButton;
    private ImageButton mChatAddButton;
    private EditText mChatMessageView;

    //List of Messages
    private RecyclerView mMessagesList;

    //Objects responsible for retrieving and displaying messages
    private final List<Messages> messageList = new ArrayList<>(); //List is parent of ArrayList
    private LinearLayoutManager mLinearLayout;
    private ConfidentialMessageAdapter mConfidentialMessageAdapter;

    //These variables are responsible for messages loaded at once
    //private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private int mCurrentPage = 1;

    //Getting message length for fading functions
    int messageLength = 0;

    //Image for Top of Chat
    private String topImage;

    //Activity came from
    private String prevActivity;

    //Timer TextView
    private TextView timerText;

    //For sending images
    private static final int GALLERY_PICK = 1;

    // Storage Firebase
    private StorageReference mImageStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confidential_chat);

        //---- Setting Up Toolbar -----//
        mChatToolbar = (Toolbar) findViewById(R.id.confidential_chat_app_bar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.red_back_arrow);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        //Extras
        mConfidentialChatUser = getIntent().getStringExtra("user_id");
        String userName = getIntent().getStringExtra("user_name_confidential");
        topImage = getIntent().getStringExtra("top_image");
        prevActivity = getIntent().getStringExtra("activity");

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.confidential_chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action bar Items ----

        mTitleView = (TextView) findViewById(R.id.confidential_custom_chat_bar_title);
        mProfileImage = (CircleImageView) findViewById(R.id.confidential_custom_chat_bar_image);

        //--Top Image For Other Chat User--//
        Picasso.with(ConfidentialChatActivity.this).load(topImage).placeholder(R.drawable.user_icon).into(mProfileImage);

        // ----- Linear Layout on the bottom of the page -----
        mChatAddButton = (ImageButton) findViewById(R.id.confidential_chat_add_btn);
        mChatSendButton = (ImageButton) findViewById(R.id.confidential_chat_send_btn);

        mChatMessageView = (EditText) findViewById(R.id.confidential_chat_message_view);
        mChatMessageView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        mConfidentialMessageAdapter = new ConfidentialMessageAdapter(messageList);

        mMessagesList = (RecyclerView) findViewById(R.id.confidential_chat_messages_list);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mConfidentialMessageAdapter);

        mRootRef.child("Chat_Confidential").child(mCurrentUserID).child(mConfidentialChatUser).child("seen").setValue(true);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        mTitleView.setText(userName);

        //Creates a new database for all open confidential chats
        mRootRef.child("Chat_Confidential").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mConfidentialChatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat_Confidential/" + mCurrentUserID + "/" + mConfidentialChatUser, chatAddMap);
                    chatUserMap.put("Chat_Confidential/" + mConfidentialChatUser + "/" + mCurrentUserID, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null) {

                                Log.d("CONFIDENTIAL_CHAT_LOG", databaseError.getMessage().toString());

                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        //loadMessage();

        // ---- Onclick Listener for Sending Message ------ //

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });

        mChatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        /*
        Should be the first thing you do
         */
        loadMessage();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    //----SENDING IMAGE-----
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "Messages_Confidential/" + mCurrentUserID + "/" + mConfidentialChatUser;
            final String chat_user_ref = "Messages_Confidential/" + mConfidentialChatUser + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootRef.child("Messages_Confidential")
                    .child(mCurrentUserID).child(mConfidentialChatUser).push();

            final String push_id = user_message_push.getKey();


            StorageReference filepath = mImageStorage.child("confidential_message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();

                        //--- Adding the Image to the Confidential Messages Query
                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserID);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CONFIDENTIAL_CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }

    }

    //-----SENDING MESSAGE-----
    private void sendMessage() {

        //Gets message from editText
        String message = mChatMessageView.getText().toString();

        /*
        TODO: When the length is received, use that to calculate how long they have to read message
         */

        //Gets the length of the message for fade functions
        messageLength = message.length();

        //Make sure message is not empty
        if(!TextUtils.isEmpty(message)){

            //They shouldn't be able to send again until after it is read
            mChatSendButton.setEnabled(false);

            String current_user_ref = "Messages_Confidential/" + mCurrentUserID + "/" + mConfidentialChatUser;
            String chat_user_ref = "Messages_Confidential/" + mConfidentialChatUser + "/" + mCurrentUserID;

            //Using the .push functions gives a unique key so multiple messages can be added
            DatabaseReference user_message_push = mRootRef.child("Messages_Confidential")
                    .child(mCurrentUserID).child(mConfidentialChatUser).push();

            //Gets a unique id for each message sent
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserID);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            //Adds message to our messages
            mRootRef.child("Chat_Confidential").child(mCurrentUserID).child(mConfidentialChatUser).child("seen").setValue(true);
            mRootRef.child("Chat_Confidential").child(mCurrentUserID).child(mConfidentialChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            //Adds message to their messages
            mRootRef.child("Chat_Confidential").child(mConfidentialChatUser).child(mCurrentUserID).child("seen").setValue(false);
            mRootRef.child("Chat_Confidential").child(mConfidentialChatUser).child(mCurrentUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError != null){

                        //-----ERROR-----
                        Log.d("CONFIDENTIAL_CHAT_LOG", databaseError.getMessage().toString());
                        Toast.makeText(ConfidentialChatActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();

                    }
                    else{

                        /*
                        After message is sent SUCCESSFULLY, message should clear from
                        Should not clear if message did not go through cause then they
                        would have to continue retyping in message
                         */
                        mChatMessageView.setText("");
                        Toast.makeText(ConfidentialChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                        Intent mainIntent = new Intent(ConfidentialChatActivity.this,MainActivity.class);
                        startActivity(mainIntent);
                        finish();

                    }

                }
            });

        }
    }

    /*
    In this load messages functions, since it is confidential mode, once the message is loaded
    onto the users screen, there should be a timer for until the message is removed.
     */
    private void loadMessage() {

        DatabaseReference messageRef = mRootRef.child("Messages_Confidential").child(mCurrentUserID).child(mConfidentialChatUser);

        //Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        Query messageQuery = messageRef.limitToLast(mCurrentPage);

        Log.d("CONFIDENTIAL_DEBUG", messageQuery.toString());

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if (!dataSnapshot.child("from").equals(mCurrentUserID)) {

                    //Gets the value and kind of casts it to a Message in order to use the classes functions
                    final Messages message = dataSnapshot.getValue(Messages.class);

                    //Check if message is not empty
                    if (message != null) {

                        if (!mCurrentUserID.equals(message.getFrom())) {

                            Log.d("IMAGES", "The message is " + message.getMessage());

                            messageList.add(message);
                            mConfidentialMessageAdapter.notifyDataSetChanged();

                            mMessagesList.scrollToPosition(messageList.size() - 1);

                            //10 Seconds, CountdownInterval is one second
                            CountDownTimer messageTimer = new CountDownTimer(10000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    timerText = (TextView) findViewById(R.id.confidential_message_timer);
                                    //Gets time
                                    String timeForCount = Long.toString(millisUntilFinished / 1000);
                                    timerText.setText(timeForCount);
                                    //TODO: Figure out why this is being counted twice
                                    Log.d("TIMER", "seconds remaining: " + millisUntilFinished / 1000);
                                }

                                public void onFinish() {
                                    //First we delete the message

                                    /*if(prevActivity.equals("FriendsFrag")) {
                                        Intent mainIntent = new Intent(ConfidentialChatActivity.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    }else if(prevActivity.equals("Chat")){
                                        Intent chatIntent = new Intent(ConfidentialChatActivity.this, ChatActivity.class);
                                        startActivity(chatIntent);
                                        finish();
                                    }*/

                                    Intent mainIntent = new Intent(ConfidentialChatActivity.this, MainActivity.class);
                                    startActivity(mainIntent);
                                    finish();

                                    deleteMessage();

                                    //Then we fade it
                                    //fadeMessage();

                                }
                            }.start();

                        }
                    }else {
                        Log.d("CONFIDENTIAL_MESSAGE", "There was no messages to load");
                    }

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public int calculateMessageTime(int numberOfCharacters){

        int count = 0;//# of times we have 40 characters
        int time = 5000; //amount of time to give them to read message

        /*while(numberOfCharacters > 40){
            count ++;
            numberOfCharacters -= 40;
        }*/

        count = numberOfCharacters/5;

        if(count != 0){

            time = 5000*count; //Convert to miliseconds

        }

        return time;
    }

    private void deleteMessage(){

        //References to the confidential_messages queries
        DatabaseReference messageRef = mRootRef.child("Messages_Confidential").child(mCurrentUserID).child(mConfidentialChatUser);
        DatabaseReference messageRef2 = mRootRef.child("Messages_Confidential").child(mConfidentialChatUser).child(mCurrentUserID);

        Log.d("DELETE_MESSAGE", "We are in the delete functions method");

        /*
        TODO: Delete the message in the confidential_messages query
         */
        messageRef.removeValue();
        messageRef2.removeValue();

        //TODO: Remove the message from the recyclerView.

    }

    public void fadeMessage(){

        /*
        Here we fade out all elements in the confidential_message_single_layout layout
        */

        TextView displayName = (TextView)findViewById(R.id.confidential_name_text_layout);
        TextView message = (TextView)findViewById(R.id.confidential_message_message_text);
        //CircleImageView proPicture = (CircleImageView)findViewById(R.id.confidential_message_profile_image);
        TextView timerText = (TextView)findViewById(R.id.confidential_message_timer);
        ImageView imageMessage = (ImageView)findViewById(R.id.confidential_message_image);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f , 1.0f ) ;
        AlphaAnimation fadeOut = new AlphaAnimation( 1.0f , 0.0f ) ;
        message.startAnimation(fadeIn);
        message.startAnimation(fadeOut);
        //proPicture.startAnimation(fadeIn);
        //proPicture.startAnimation(fadeOut);
        displayName.startAnimation(fadeIn);
        displayName.startAnimation(fadeOut);
        imageMessage.startAnimation(fadeOut);
        //fadeIn.setDuration(1200);
        //fadeIn.setFillAfter(true);
        fadeOut.setDuration(1200);
        fadeOut.setFillAfter(true);
        //fadeOut.setStartOffset(4200+fadeIn.getStartOffset());

        //Get rid of the timer text view
        timerText.setVisibility(View.INVISIBLE);

    }

}