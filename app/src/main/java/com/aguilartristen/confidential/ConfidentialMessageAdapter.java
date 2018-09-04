package com.aguilartristen.confidential;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/*
This class is responsible for retrieving messages
 */

public class ConfidentialMessageAdapter extends RecyclerView.Adapter<ConfidentialMessageAdapter.MessageViewHolder>{

    //Holds all messages
    private List<Messages> mMessagesList;
    //Database Reference
    private DatabaseReference mUserDatabase;
    //Firebase Auth Object
    private FirebaseAuth mAuth;

    public ConfidentialMessageAdapter(List<Messages> MessagesList){

        this.mMessagesList = MessagesList;

    } // Default Constructor

    @Override
    public MessageViewHolder onCreateViewHolder (ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.confidential_message_single_layout, parent, false);

        return new MessageViewHolder(v);

    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        private TextView messageText;
        private CircleImageView profileImage;
        private ImageView messageImage;
        private TextView displayName;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.confidential_message_message_text);
            messageImage = (ImageView) itemView.findViewById(R.id.confidential_message_image);
            displayName = (TextView) itemView.findViewById(R.id.confidential_name_text_layout);

        }

    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        String mCurrentUserID = mAuth.getCurrentUser().getUid();

        //Gets a specific message to load in
        final Messages c = mMessagesList.get(position);

        //Gets who sent the message
        String from_user = c.getFrom();
        //Gets the type of message, 'text' or 'image'
        String message_type = c.getType();

        //We go into the persons data who we received the message from
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Gets their name
                String name = dataSnapshot.child("name").getValue().toString();
                //Gets their thumb image
                String image = dataSnapshot.child("thumb_image").getValue().toString();

                //This gets their name and puts in the displayName textview
                holder.displayName.setText(name);

                holder.messageText.setText(c.getMessage());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        if(message_type.equals("text")) { //Regular text

            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.INVISIBLE);


        } else { //It's an image

            holder.messageText.setVisibility(View.INVISIBLE);
            holder.messageImage.setVisibility(View.VISIBLE);
            Picasso.with(holder.messageImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_message_image).into(holder.messageImage);

        }

    }


    @Override
    public int getItemCount() {
        //Gets number of messages
        return mMessagesList.size();
    }
}
