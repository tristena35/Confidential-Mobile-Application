package com.aguilartristen.confidential;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
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
import com.google.firebase.database.ChildEventListener;
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
public class ChatFragment extends Fragment {

    private RecyclerView mConvList;

    private TextView mEmptyView;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    //Adapter
    FirebaseRecyclerAdapter firebaseConvAdapter;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chat, container, false);

        //RecyclerView to hold convos
        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);

        mEmptyView = (TextView) mMainView.findViewById(R.id.chat_empty_view);

        mAuth = FirebaseAuth.getInstance();

        //Gets current users ID
        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        //A database reference to the Chat Query
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);

        mConvDatabase.keepSynced(true);

        //Database reference to Users
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        //Database reference to all users Messages
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrent_user_id);

        mUsersDatabase.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        //Keep latest convos to seem most recent
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        //Adds a diving line between friends
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        mConvList.addItemDecoration(itemDecor);

        //A query to go through and get Chat and time
        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        //Setting up the Recycler Adapter
        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(conversationQuery, Conv.class)
                        .build();

        firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder convViewHolder, int position, @NonNull final Conv conv) {

                //Gets the person's ID we click on
                final String list_user_id = getRef(position).getKey();

                //Gets the last message to display under them in the Chats Page
                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        //This gets the message
                        String data = dataSnapshot.child("message").getValue().toString();

                        //Gets time of last message sent
                        String last_message_time = dataSnapshot.child("timesent").getValue().toString();

                        //If it has been seen, it will not be bolded
                        convViewHolder.setMessage(data, conv.isSeen());

                        convViewHolder.setDate(last_message_time);

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


                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //This gets the username of the person we clicked on
                        final String userName = dataSnapshot.child("name").getValue().toString();

                        //This gets their image
                        final String userThumb = dataSnapshot.child("thumb_image").getValue().toString();

                        convViewHolder.setName(userName);
                        convViewHolder.setUserImage(userThumb, getContext());

                        convViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                //Putting both IDs into the ChatActivity so that Activity can use them to get the messages

                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", userName);
                                chatIntent.putExtra("top_image", userThumb);
                                startActivity(chatIntent);

                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public ConvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout_chat, parent, false);

                /*
                If the Recycler View was created, that means there was data to retrieve,
                and will set the TextView to be gone. If there is no data, this will never be called,
                and the text will be visible
                 */
                mEmptyView.setVisibility(View.GONE);

                return new ConvViewHolder(view);
            }

        };

        mConvList.setAdapter(firebaseConvAdapter);

        return mMainView;

    }



    @Override
    public void onStart() {
        super.onStart();

        firebaseConvAdapter.startListening();

    }



    @Override
    public void onStop() {
        super.onStop();

        firebaseConvAdapter.stopListening();

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ConvViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

        }

        public void setMessage(String message, boolean isSeen){

            TextView userStatusView = (TextView) mView.findViewById(R.id.user_single_status);
            userStatusView.setText(message);

            ImageView userNoficationImage = (ImageView) mView.findViewById(R.id.user_notification_sign);

            /*
            This if statement will check if the latest message was seen or not:
            If it was, it will show just the text
            If it wasn't, it will show the text a bit bolded
             */

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
                userNoficationImage.setVisibility(View.VISIBLE);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
                userNoficationImage.setVisibility(View.INVISIBLE);
            }

        }

        public void setName(String name){

            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);

        }

        public void setDate(String date){

            TextView dateView = (TextView) mView.findViewById(R.id.last_text_sent_time);
            dateView.setText(date);

        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_icon_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.user_icon).into(userImageView);

        }

    }


}
