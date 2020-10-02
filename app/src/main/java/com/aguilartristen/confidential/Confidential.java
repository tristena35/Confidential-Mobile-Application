package com.aguilartristen.confidential;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by trist on 3/19/2018.
 */

// Offline Capabilities Class. Must Mention In Android Manifest
public class Confidential extends Application {

    // DatabaseRef to the Users Database
    private DatabaseReference mUserDatabase;
    // Firebase Auth Object
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enabling Firebase Offline capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        /*
            PICASSO:
            These lines of code will allow us to retrieve the image and use it even in offline capabilities
         */
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null) {
            mUserDatabase = FirebaseDatabase.getInstance()
                    .getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        // We use the TIMESTAMP directly because the onDisconnect may take some time to set it to false
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                        /*
                            This line should match the one in the MainActivity, and shares same functionality
                            mUserDatabase.child("lastSeen").setValue(ServerValue.TIMESTAMP);
                         */
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
}
