package com.aguilartristen.confidential;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

/*

    THIS MESSAGE ADAPTER IS THE BETTER ONE

 */

public class MessageListAdapter extends RecyclerView.Adapter {

    //Two variables to keep track of who sent or received
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context mContext;
    //List to capture passed in list of messages
    private List<Messages> mMessageList;

    //Database Reference
    private DatabaseReference mUserDatabase;
    //Firebase Auth Object
    private FirebaseAuth mAuth;

    public MessageListAdapter(Context context, List<Messages> messageList) {

        mContext = context;
        mMessageList = messageList;

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {

        //Gets a message
        Messages message = (Messages) mMessageList.get(position);

        mAuth = FirebaseAuth.getInstance();

        //Gets current user
        String mCurrentUserID = mAuth.getCurrentUser().getUid();

        if (message.getFrom().equals(mCurrentUserID)){
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;

        }else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }

    }

    // Inflates the appropriate layout according to the ViewType
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //View object
        View view;

        //SENT
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {

            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);

        }
        //RECEIVED
        else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {

            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);

            return new ReceivedMessageHolder(view);

        }

        return null;
    }

    // Passes the message object to a ViewHolder so that the contents can be bound to UI.
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        //Gets the message
        Messages message = (Messages) mMessageList.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;

            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {

        //Two variables for time and text
        TextView messageText, timeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_sent_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_sent_time);
        }

        void bind(Messages message) {

            //Setting message
            messageText.setText(message.getMessage());

            //Shows time sent
            timeText.setText(message.getTimesent());
        }

    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView messageText, timeText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.text_message_body);
            timeText = (TextView) itemView.findViewById(R.id.text_message_time);

        }

        void bind(Messages message) {

            //Setting message
            messageText.setText(message.getMessage());

            //Shows time sent
            timeText.setText(message.getTimesent());

        }
    }

}