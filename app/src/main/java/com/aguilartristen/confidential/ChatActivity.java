package com.aguilartristen.confidential;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {

    //User we are chatting with
    private String mChatUser;

    //Toolbar
    private Toolbar mChatToolbar;

    //This is called the root ref because we are going to use this one reference for the whole database.
    private DatabaseReference mRootRef;

    //Reference to Users
    private DatabaseReference mUsersRef;

    //This Objects are all related to the chat_custom_bar layout
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;

    //Firebase Auth Object
    private FirebaseAuth mAuth;
    //String Variable to hold current user'S ID
    private String mCurrentUserID;

    //Objects on bottom part of ChatActivity
    private ImageButton mChatAddButton;
    private ImageButton mChatSendButton;
    private EditText mChatMessageView;

    private TextView mTypingView;

    //List of Messages
    private RecyclerView mMessagesList;
    //Swiping layout
    private SwipeRefreshLayout mRefreshLayout;

    //Objects responsible for retrieving and displaying messages
    private final List<Messages> messageList = new ArrayList<>(); //List is parent of ArrayList
    private LinearLayoutManager mLinearLayout;

    //New MessageListAdapter to take into account Sent and Received
    private MessageListAdapter mMessageAdapter;

    //These variables are responsible for messages loaded at once
    private static final int TOTAL_ITEMS_TO_LOAD = 15;
    private int mCurrentPage = 1;

    //For sending images
    private static final int GALLERY_PICK = 1;

    //If state is 0, not typing, if 1 then typing
    private int TYPING_STATE = 0;

    //New Solution for Loading More Messages
    private int itemPos = 0;

    //Hold last key
    private String mLastKey = "";
    private String mPrevKey = "";

    // Storage Firebase
    private StorageReference mImageStorage;

    //Username
    private String userName;

    //Username of person signed in
    private String currentUserName;

    //Image for Top of Chat
    private String topImage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        //Getting Ref
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        //Extras
        mChatUser = getIntent().getStringExtra("user_id");
        userName = getIntent().getStringExtra("user_name");
        topImage = getIntent().getStringExtra("top_image");


        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);

        // ---- Custom Action bar Items ----

        mTitleView = (TextView) findViewById(R.id.custom_chat_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_chat_bar_seen);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_chat_bar_image);
        mTypingView = (TextView) findViewById(R.id.chat_typing_textView);

        //Loads their image into the top of the activity
        Picasso.with(ChatActivity.this).load(topImage).placeholder(R.drawable.user_icon).into(mProfileImage);

        mChatAddButton = (ImageButton) findViewById(R.id.chat_add_btn);
        mChatSendButton = (ImageButton) findViewById(R.id.chat_send_btn);

        //Make first letter Capitalized
        mChatMessageView = (EditText) findViewById(R.id.chat_message_view);
        mChatMessageView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);



        /*
        As they are typing, write they are typing
         */
        mChatMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty()) {

                    //Instead try adding an item under instead then deleting it?

                    //mTypingView.setText("Typing...");
                    TYPING_STATE = 1;
                    mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("typing").setValue(1);
                    //mRootRef.child("Chat").child(mChatUser).child(mCurrentUserID).child("typing").setValue(1);


                }else{

                    //mTypingView.setText("");
                    TYPING_STATE = 0;
                    mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("typing").setValue(0);
                    //mRootRef.child("Chat").child(mChatUser).child(mCurrentUserID).child("typing").setValue(0);

                }

            }
        });



        //Setting up the Adapter
        mMessageAdapter = new MessageListAdapter(this, messageList);

        mMessagesList = (RecyclerView) findViewById(R.id.chat_messages_list);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mMessagesList.setAdapter(mMessageAdapter);

        /*
        Once the User enters the chat, the message should appear to be read
         */
        mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("seen").setValue(true);

        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference();

        loadMessages();

        mTitleView.setText(userName);



        //Query to update a users online attributes
        mRootRef.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child(mChatUser).child("online").getValue().toString();

                //Getting username for typing string, from user we are talking with
                currentUserName = dataSnapshot.child(mChatUser).child("name").getValue().toString();

                if(online.equals("true")) {

                    mLastSeenView.setText("Online");


                } else {

                    //The last seen feature

                    GetTimeSince getTimeAgo = new GetTimeSince();

                    long lastTime = Long.parseLong(online);

                    String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());

                    mLastSeenView.setText(lastSeenTime);

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        //Add the Chat to Firebase
        mRootRef.child("Chat").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    chatAddMap.put("typing", 0);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserID + "/" + mChatUser, chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserID, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null){

                                Log.d("CHAT_LOG", databaseError.getMessage());

                            }

                        }
                    });


                }

                if(dataSnapshot.hasChild(mChatUser)){

                    Log.d("mChatUser", mChatUser);

                    if(dataSnapshot.child(mChatUser).child(mCurrentUserID).child("typing").getValue().toString().equals("1")){
                        mTypingView.setText(currentUserName + " is typing...");
                    }else{
                        mTypingView.setText("");
                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });



        // ---- Onclick Listener for Sending Message ------ //

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();

            }
        });

        // ----- Onclick Listener for Adding Image to Message ---- //

        mChatAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });



        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;

                /*
                Every time the page is refreshed, we want to start at the first message from
                the new messages that came in, so we assign the position 0, which will
                be assigned firstly to 1 then 2 and so on.
                 */
                itemPos = 0;

                //This method will load the messages that have not been loaded yet
                loadMoreMessages();


            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // super.onCreateOptionsMenu(menu);   I commented this out for it to work, but why
        getMenuInflater().inflate(R.menu.chat_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.home){
            onBackPressed();
            return true;
            //TODO: Make this only go back to ChatFragment instead of main page
        }

        if(item.getItemId() == R.id.confidential_chat){
            Intent confidential_Intent = new Intent(ChatActivity.this, ConfidentialChatActivity.class);
            confidential_Intent.putExtra("user_id", mChatUser);
            confidential_Intent.putExtra("user_name_confidential", userName);
            confidential_Intent.putExtra("top_image", topImage);
            confidential_Intent.putExtra("activity", "Chat");
            startActivity(confidential_Intent);
            return true;
            //TODO: Make this only go back to ChatFragment instead of main page
        }

        if(item.getItemId() == R.id.delete_conv){

            //TODO: Figure out why this only works after the app is restarted

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:

                            //-----CLICKED YES------

                            //If they are sure, then delete convo.
                            Map deleteConvoMap = new HashMap();
                            deleteConvoMap.put("Chat/" + mCurrentUserID + "/" + mChatUser, null);
                            deleteConvoMap.put("Chat/" + mChatUser + "/" + mCurrentUserID, null);
                            deleteConvoMap.put("Messages/" + mCurrentUserID + "/" + mChatUser, null);
                            deleteConvoMap.put("Messages/" + mChatUser + "/" + mCurrentUserID, null);

                            mRootRef.updateChildren(deleteConvoMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                    if(databaseError == null){

                                        Toast.makeText(ChatActivity.this,"Conversation Deleted",Toast.LENGTH_SHORT).show();

                                    }else{

                                        String error = databaseError.getMessage();

                                        Toast.makeText(ChatActivity.this, error, Toast.LENGTH_SHORT).show();

                                    }

                                }
                            });

                            //Go back to MainActivity
                            Intent mainIntent = new Intent(ChatActivity.this, MainActivity.class);
                            startActivity(mainIntent);

                        case DialogInterface.BUTTON_NEGATIVE:

                            //----CLICKED NO-----

                            //Toast.makeText(ChatActivity.this, "NO WAS CLICKED", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

            return true;

        }

        return super.onOptionsItemSelected(item);
    }



    //Sending a message
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            final String current_user_ref = "Messages/" + mCurrentUserID + "/" + mChatUser;
            final String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserID;

            //GETS CURRENT TIME IN good format
            SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
            final String currentTime = df.format(Calendar.getInstance().getTime());

            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserID).child(mChatUser).push();

            final String push_id = user_message_push.getKey();

            StorageReference filepath = mImageStorage.child("message_images").child( push_id + ".jpg");

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    if(task.isSuccessful()){

                        String download_url = task.getResult().getDownloadUrl().toString();


                        Map messageMap = new HashMap();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", mCurrentUserID);
                        messageMap.put("timesent", currentTime);

                        Map messageUserMap = new HashMap();
                        messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                        messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                        mChatMessageView.setText("");

                        mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError != null){

                                    Log.d("CHAT_LOG", databaseError.getMessage().toString());

                                }

                            }
                        });


                    }

                }
            });

        }

    }

    //Loads additional messages
    private void loadMoreMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {


                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){

                    messageList.add(itemPos++, message);

                } else {

                    mPrevKey = mLastKey;

                }


                if(itemPos == 1) {

                    mLastKey = messageKey;

                }


                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey);

                mMessageAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(TOTAL_ITEMS_TO_LOAD - 1, 0);

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

    private void loadMessages() {

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserID).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);

                Log.d("IMAGES", "The message is " + message.getMessage());

                itemPos++;

                if(itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messageList.add(message);
                mMessageAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messageList.size() - 1);

                mRefreshLayout.setRefreshing(false);

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

    //Sending the message and information along with it
    private void sendMessage() {

        String message = mChatMessageView.getText().toString();

        //Make sure message is not empty
        if (!TextUtils.isEmpty(message)) {

            String current_user_ref = "Messages/" + mCurrentUserID + "/" + mChatUser;
            String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserID;

            //GETS CURRENT TIME IN good format
            SimpleDateFormat df = new SimpleDateFormat("hh:mm a");
            String currentTime = df.format(Calendar.getInstance().getTime());

            //Using the .push functions gives a unique key so multiple messages can be added
            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserID).child(mChatUser).push();

            //Gets a unique id for each message sent
            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserID);
            messageMap.put("timesent", currentTime.toString());

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            //mChatMessageView.setText("");

            mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("seen").setValue(true);
            mRootRef.child("Chat").child(mCurrentUserID).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserID).child("seen").setValue(false);
            mRootRef.child("Chat").child(mChatUser).child(mCurrentUserID).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if (databaseError != null) {

                        Log.d("CHAT_LOG", databaseError.getMessage().toString());

                    } else {

                        /*
                        After message is sent SUCCESSFULLY, message should clear from
                        Should not clear if message did not go through cause then they
                        would have to continue retyping in message
                         */
                        mChatMessageView.setText("");

                    }

                }
            });

        }

    }


}