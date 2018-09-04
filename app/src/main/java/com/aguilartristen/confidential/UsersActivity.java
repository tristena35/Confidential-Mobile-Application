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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar mToolbar;

    //RecyclerView
    private RecyclerView mUsersList;

    //Firebase Database
    private DatabaseReference mUsersDatabase;

    //Adapter
    FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    Query query = FirebaseDatabase.getInstance().getReference().child("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //----Setting up Custom ActionBar

        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        //getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.all_users_custom_bar, null);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(action_bar_view);

        //Log.d("FIREBASE_SEARCH1", query.getRef().toString());

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration itemDecor = new DividerItemDecoration(UsersActivity.this, DividerItemDecoration.VERTICAL);
        mUsersList.addItemDecoration(itemDecor);

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(query, Users.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options) {
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout_users, parent, false);

                return new UsersViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final UsersViewHolder usersViewHolder, int position, @NonNull final Users users) {

                    FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    String mCurrentUserID = mAuth.getCurrentUser().getUid();

                    //Get Key of other user
                    final String user_id = getRef(position).getKey();
                    //Test to try and retrieve the username of the person sending friend request

                    mUsersDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //Log.d("USERS_VALUES", dataSnapshot.child(user_id).child("confidential_mode").getValue().toString());

                            if(dataSnapshot.hasChild("confidential_mode") &&
                                    dataSnapshot.child("confidential_mode")
                                    .getValue().toString().equals("false")) {

                                usersViewHolder.setDisplayName(users.getName());
                                usersViewHolder.setUserStatus(users.getStatus());
                                usersViewHolder.setUserImage(users.getThumb_image(), getApplicationContext());

                                //Clicking on one of the users in the 'All Users' page and going to their profile
                                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                    Intent profile_Intent = new Intent(UsersActivity.this, ProfileActivity.class);
                                    profile_Intent.putExtra("user_id", user_id);
                                    startActivity(profile_Intent);

                                    }
                                });

                            }else if(dataSnapshot.hasChild("confidential_mode") &&
                                    dataSnapshot.child("confidential_mode")
                                            .getValue().toString().equals("true")){

                                usersViewHolder.setDisplayName("Anonymous");
                                usersViewHolder.setUserStatus("Confidential");
                                usersViewHolder.setUserImage(R.drawable.confidential_logo,getApplicationContext());

                            }else{

                                Log.d("USERS_ERROR", "Previous if statements failed");

                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                            Log.d("CONFIDENTIAL_MODE", databaseError.getMessage());

                        }
                    });

            }

        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseRecyclerAdapter.startListening();

        //Checks that when the user logged in, it will put online status to true
        //mUsersRef.child("online").setValue(true);
        Log.d("Confidential", "onStart in All Users Worked");

    }



    @Override
    protected void onStop() {
        super.onStop();

        firebaseRecyclerAdapter.stopListening();

        //If app is closed then online should be set to false
        //mUsersRef.child("online").setValue(false);

    }



    //This class will act an an adapter for out Firebase Recycler View
    //Since it's an inner class, it needs to be static
    public static class UsersViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setDisplayName(String name){

            //This takes the name and sets it for the individual users username
            TextView usernameView = (TextView) mView.findViewById(R.id.user_single_name);
            usernameView.setText(name);

        }

        public void setUserStatus(String status){

            //This takes the name and sets it for the individual users username
            TextView statusView = (TextView) mView.findViewById(R.id.user_single_status);
            statusView.setText(status);

        }

        public void setUserImage(String thumb_image, Context ctx){

            //This takes the name and sets it for the individual users username
            CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.user_icon_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.user_icon).into(imageView);

        }

        public void setUserImage(int image, Context ctx){

            //This takes the name and sets it for the individual users username
            CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.user_icon_image);
            Picasso.with(ctx).load(image).placeholder(R.drawable.user_icon).into(imageView);

        }

    }

}
