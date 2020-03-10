package com.aguilartristen.confidential;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.security.spec.ECField;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    //RecyclerView
    private RecyclerView mReqList;

    //TextView that shows no requests if dataset is empty
    private TextView mEmptyView;

    //Reference to the Friends_Req database
    private DatabaseReference mFriendReqDatabase;

    private DatabaseReference mUsersDatabase;


    private FirebaseAuth mAuth;

    //Gets our current ID
    private String mCurrent_user_id;

    private View mMainView;

    //Adapter
    FirebaseRecyclerAdapter firebaseReqAdapter;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_request, container, false);

        //RecyclerView to hold convos
        mReqList = (RecyclerView) mMainView.findViewById(R.id.req_list);

        mEmptyView = (TextView) mMainView.findViewById(R.id.request_empty_view);

        mAuth = FirebaseAuth.getInstance();

        //Gets current users ID
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        //A database reference to whole Friend Req database
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        mFriendReqDatabase.keepSynced(true);

        //Database reference to Users
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //Keep latest convos to seem most recent
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mReqList.setHasFixedSize(true);
        mReqList.setLayoutManager(linearLayoutManager);

        //Adds a diving line between friends
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mReqList.addItemDecoration(itemDecor);

        //Gets our requests
        Query requestsQuery = FirebaseDatabase.getInstance().getReference()
                .child("Friend_req").child(mCurrent_user_id);

        //Setting up the Recycler Adapter
        FirebaseRecyclerOptions<FriendReqs> options =
                new FirebaseRecyclerOptions.Builder<FriendReqs>()
                        .setQuery(requestsQuery, FriendReqs.class)
                        .build();

        firebaseReqAdapter = new FirebaseRecyclerAdapter<FriendReqs, RequestsViewHolder>(options) {

            @Override
            public RequestsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout_requests, parent, false);

                /*
                If the Recycler View was created, that means there was data to retrieve,
                and will set the TextView to be gone. If there is no data, this will never be called,
                and the text will be visible
                 */
                mEmptyView.setVisibility(View.GONE);

                return new RequestsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull final FriendReqs friendsReqs) {

                //Gets the specific user
                final String list_user_id = getRef(position).getKey();

                //gets the key of the chosen user on friends list from the Users data by using the list_user_id
                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //You need to use the FriendsViewHolder class to use these values in the Friends Fragment
                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String userThumbImage = dataSnapshot.child("thumb_image").getValue().toString();
                        String userStatus = dataSnapshot.child("status").getValue().toString();

                        holder.setRequestUserName(userName);
                        holder.setRequestUserStatus(userStatus);
                        holder.setRequestUserImage(userThumbImage,getContext());

                        //Clicking on one of the users in the 'All Users' page and going to their profile
                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                Intent profile_Intent = new Intent(getContext(), ProfileActivity.class);
                                profile_Intent.putExtra("user_id", list_user_id);

                                startActivity(profile_Intent);

                            }
                        });

                        mFriendReqDatabase.child(list_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                try{
                                    String date = dataSnapshot.child(mCurrent_user_id).child("date").getValue().toString();
                                    holder.setRequestDate(date);
                                }catch(Exception e){
                                    Log.e("Friend Request", e.toString());
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                                Log.e("Friend Request", "ERROR WITH ADDING FRIEND");

                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                        Log.d("Confidential", "Problem setting up the recycler view on the Friend Fragment");

                    }
                });

            }

        };

        mReqList.setAdapter(firebaseReqAdapter);

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        firebaseReqAdapter.startListening();

    }



    @Override
    public void onStop() {
        super.onStop();

        firebaseReqAdapter.stopListening();

    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RequestsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setRequestUserName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setRequestUserStatus(String status){

            //This takes the name and sets it for the individual users username
            TextView statusView = (TextView) mView.findViewById(R.id.user_single_status);
            statusView.setText(status);

        }

        public void setRequestUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_icon_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.user_icon).into(userImageView);

        }

        public void setRequestDate(String date){

            TextView dateView = (TextView) mView.findViewById(R.id.request_date);
            dateView.setText(date);

        }

    }


}


