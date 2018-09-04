package com.aguilartristen.confidential;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedActivity extends AppCompatActivity {

    //This is called the root ref because we are going to use this one reference for the whole database.
    private DatabaseReference mFeedDatabase;

    //Reference to Users
    private DatabaseReference mUsersRef;

    //Toolbar
    private Toolbar mFeedToolbar;

    //This Objects are all related to the chat_custom_bar layout
    private TextView mUsernameView;
    private TextView mTimePostedView;
    private CircleImageView mUserImage;

    //Firebase Auth Object
    private FirebaseAuth mAuth;

    //String Variable to hold current user'S ID
    private String mCurrentUserID;

    //Objects on post
    private ImageButton mLikeButton;
    private ImageButton mDislikeButton;

    //List of Messages
    private RecyclerView mFeedList;

    // Storage Firebase
    private StorageReference mImageStorage;

    //Username
    private String userName;

    //Username of person signed in
    private String currentUserName;

    //Image for Top of Chat
    private String topImage;

    //Gets number of Likes and Dislikes
    long newNumberOfCurrentLikes;
    long newNumberOfCurrentDislikes;

    //Query for Feed Database
    Query query = FirebaseDatabase.getInstance().getReference().child("Feed_page").orderByKey().limitToLast(15);

    //Adapter
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        mFeedToolbar = (Toolbar) findViewById(R.id.feed_page_toolbar);
        setSupportActionBar(mFeedToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.feed_custom_bar, null);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();

        //Getting Refs
        mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);
        mFeedDatabase = FirebaseDatabase.getInstance().getReference().child("Feed_page");

        //Like and Dislike button on individual Posts
        mLikeButton = (ImageButton)findViewById(R.id.feed_individual_likes_image);
        mDislikeButton = (ImageButton)findViewById(R.id.feed_individual_dislikes_image);

        /*----- FEED LIST ------*/

        mFeedList = (RecyclerView) findViewById(R.id.feed_messages_list);
        mFeedList.setHasFixedSize(true);
        mFeedList.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecor = new DividerItemDecoration(FeedActivity.this, DividerItemDecoration.VERTICAL);
        mFeedList.addItemDecoration(itemDecor);


        FirebaseRecyclerOptions<Feed> options =
                new FirebaseRecyclerOptions.Builder<Feed>()
                        .setQuery(query, Feed.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Feed, FeedViewHolder>(options) {
            @Override
            public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.feed_single_layout, parent, false);

                return new FeedViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FeedViewHolder feedUserViewHolder, int position, @NonNull final Feed post) {

                FirebaseAuth mAuth = FirebaseAuth.getInstance();

                //Get Key of other user
                final String user_id = getRef(position).getKey();

                feedUserViewHolder.setFeedUserDisplayName(post.getName());
                feedUserViewHolder.setFeedUserMessage(post.getMessage());
                feedUserViewHolder.setFeedUserTimePosted(post.getTimePosted());
                feedUserViewHolder.setFeedUserImage(post.getThumbImage(), getApplicationContext());
                feedUserViewHolder.setFeedUserLikes(post.getLikesCount());
                feedUserViewHolder.setFeedUserDislikes(post.getDislikesCount());



                //Clicking on one of the posts should enlarge it

                /*feedUserViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent profile_Intent = new Intent(FeedActivity.this, ProfileActivity.class);
                        profile_Intent.putExtra("user_id", user_id);
                        startActivity(profile_Intent);
                    }
                });*/


                //-----When Clicking the like button------
                feedUserViewHolder.mView.findViewById(R.id.feed_individual_likes_image).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(FeedActivity.this,"LIKES for " + user_id,Toast.LENGTH_SHORT).show();

                        mFeedDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                long numberOfCurrentLikes = (long) dataSnapshot.child("likes").getValue();

                                newNumberOfCurrentLikes = numberOfCurrentLikes + 1;

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mFeedDatabase.child(user_id).child("likes").setValue(newNumberOfCurrentLikes);

                    }
                });


                //-----When Clicking the dislike button-----
                feedUserViewHolder.mView.findViewById(R.id.feed_individual_dislikes_image).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(FeedActivity.this,"DISLIKES for " + user_id,Toast.LENGTH_SHORT).show();

                        mFeedDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                long numberOfCurrentDislikes = (long) dataSnapshot.child("dislikes").getValue();

                                newNumberOfCurrentDislikes = numberOfCurrentDislikes + 1;

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mFeedDatabase.child(user_id).child("dislikes").setValue(newNumberOfCurrentDislikes);

                    }
                });

            }

        };

        mFeedList.setAdapter(firebaseRecyclerAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseRecyclerAdapter.startListening();

    }

    @Override
    protected void onStop() {
        super.onStop();

        firebaseRecyclerAdapter.stopListening();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.home) {
            Intent mainIntent = new Intent(FeedActivity.this,MainActivity.class);
            startActivity(mainIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

//This class will act an an adapter for out Firebase Recycler View
//Since it's an inner class, it needs to be static
class FeedViewHolder extends RecyclerView.ViewHolder{

    View mView;

    public FeedViewHolder(View itemView) {
        super(itemView);

        mView = itemView;

    }

    public void setFeedUserDisplayName(String name){

        //Sets the USERS name
        TextView usernameView = (TextView) mView.findViewById(R.id.feed_username);
        usernameView.setText(name);

    }

    public void setFeedUserMessage(String message){

        //Sets the Feed Message
        TextView messageView = (TextView) mView.findViewById(R.id.feed_user_message);
        messageView.setText(message);

    }

    public void setFeedUserTimePosted(String timePosted){

        //Sets Time Posted
        TextView timePostedView = (TextView) mView.findViewById(R.id.feed_time_posted);
        timePostedView.setText(timePosted);

    }

    public void setFeedUserLikes(int likes){

        //Sets Likes
        TextView likesView = (TextView) mView.findViewById(R.id.feed_likes_number);
        likesView.setText(likes + "");

    }

    public void setFeedUserDislikes(int dislikes){

        //Sets Likes
        TextView likesView = (TextView) mView.findViewById(R.id.feed_dislikes_number);
        likesView.setText(dislikes + "");

    }

    public void setFeedUserImage(String thumb_image, Context ctx){

        //Sets Users Image
        CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.feed_user_image);
        Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.user_icon).into(imageView);

    }


}

