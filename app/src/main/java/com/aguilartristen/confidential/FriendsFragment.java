package com.aguilartristen.confidential;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    //List of friends
    private RecyclerView mFriendList;
    //TextView that shows no confidants if dataset is empty
    private TextView mEmptyView;
    //Database Reference refers to the Users Database
    private DatabaseReference mUsersDatabase;
    //Database Reference refers to the Users Database
    private DatabaseReference mFriendsDatabase;
    //An Auth Object for the user
    private FirebaseAuth mAuth;
    //This gets the id for the current id
    private String mCurrent_user_id;

    private View mMainView;

    //Adapter
    FirebaseRecyclerAdapter friendsRecyclerAdapter;

    //Query
    Query query;

    public FriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        mEmptyView = (TextView) mMainView.findViewById(R.id.confidants_empty_view);

        mFriendList = (RecyclerView) mMainView.findViewById(R.id.friends_list);

        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        //Users Database
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        //Firebase Offline feature, keeps all data in Users database saved
        mUsersDatabase.keepSynced(true);

        //Query (Reference to Friends Database) for RecyclerView
        query = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        //Firebase Offline feature, keeps all data in Friends database saved
        query.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        //Adds a diving line between friends
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mFriendList.addItemDecoration(itemDecor);


        //Setting up the Recycler Adapter
        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(query, Friends.class)
                        .build();

        friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull Friends friends) {

                //holder.setDate(friends.getDate());

                //Gets the specific user
                final String list_user_id = getRef(position).getKey();

                //gets the key of the chosen user on friends list from the Users data by using the list_user_id
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //You need to use the FriendsViewHolder class to use these values in the Friends Fragment
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        final String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();
                        //String usersOnline = dataSnapshot.child("online").getValue().toString();

                        //If the 'online' feature is present within the child
                        if(dataSnapshot.hasChild("online")){

                            /*Around the date of 4-3-18, the app would constantly crash
                            when moving to the chats fragment of friends fragment. The reason
                            for that is because when using the online and offline feature, some of
                            the online values where set to numbers, and when converted to
                            boolean caused a logical error.
                            Make sure Online Values are either 'True' or 'False'
                             */
                           String userOnline = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(userOnline);

                        }

                        holder.setFriendName(userName);
                        holder.setFriendStatus(userStatus);
                        holder.setFriendImage(userThumbImage,getContext());

                        //If someone clicks on a single friend user box
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                CharSequence[] options = new CharSequence[]{"Open Profile", "Send Message", "Confidential Chat"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //Click Event For Each Item
                                        //In future, it's smart to add a switch statement when dealing with multiple items
                                        if(which == 0){ //View Profile

                                            Intent profile_Intent = new Intent(getContext(), ProfileActivity.class);
                                            profile_Intent.putExtra("user_id", list_user_id);
                                            startActivity(profile_Intent);

                                        }else if(which == 1){ //Send Message

                                            Intent chat_Intent = new Intent(getContext(), ChatActivity.class);
                                            chat_Intent.putExtra("user_id", list_user_id);
                                            chat_Intent.putExtra("user_name", userName);
                                            chat_Intent.putExtra("top_image", userThumbImage);
                                            startActivity(chat_Intent);

                                        }else if(which == 2){ //Confidential Mode


                                            Intent confidential_Intent = new Intent(getContext(), ConfidentialChatActivity.class);
                                            confidential_Intent.putExtra("user_id", list_user_id);
                                            confidential_Intent.putExtra("user_name_confidential", userName);
                                            confidential_Intent.putExtra("top_image", userThumbImage);
                                            confidential_Intent.putExtra("activity", "FriendsFrag");
                                            startActivity(confidential_Intent);

                                        }

                                    }
                                });

                                builder.show();

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.d("Confidential", "Problem setting up the recycler view on the Friend Fragment");

                    }
                });

            }

            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout_friends, parent, false);

                /*
                If the Recycler View was created, that means there was data to retrieve,
                and will set the TextView to be gone. If there is no data, this will never be called,
                and the text will be visible
                 */
                mEmptyView.setVisibility(View.GONE);

                return new FriendsViewHolder(view);
            }

        };

        mFriendList.setAdapter(friendsRecyclerAdapter);

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        friendsRecyclerAdapter.startListening();

    }



    @Override
    public void onStop() {
        super.onStop();

        friendsRecyclerAdapter.stopListening();

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        /*public void setDate(String date) {

            TextView userDateView = (TextView) mView.findViewById(R.id.user_single_status);
            userDateView.setText(date);

        }*/

        public void setFriendName(String name) {

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setFriendStatus(String status){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);

        }

        public void setFriendImage(String thumb_image, Context ctx){

            //This takes the username and sets it for the individual users username
            CircleImageView imageView = (CircleImageView) mView.findViewById(R.id.user_icon_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.user_icon).into(imageView);

        }

        public void setUserOnline(String online_status){

            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);

            if(online_status.equals("true")){

                userOnlineView.setImageResource(R.drawable.green_circle);

            }else{

                userOnlineView.setImageResource(R.drawable.red_circle);

            }
        }

    }


}
