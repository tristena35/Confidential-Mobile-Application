package com.aguilartristen.confidential;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


@TargetApi(Build.VERSION_CODES.M)
public class FingerPrintHandlerDialog extends FingerprintManager.AuthenticationCallback {

    //Context Object to get the current Context
    private Context mContext;

    //Variable for username and profile image passed from previous intent
    private String userName, profileImage, lockImage;


    public FingerPrintHandlerDialog(Context context, String userName){

        this.mContext = context;
        this.userName = userName;

    }//Default Constructor

    public FingerPrintHandlerDialog(Context context){

        this.mContext = context;

    }//Default Constructor


    public void startAuth(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject){

        //Creates an object that provides the ability to cancel an operation in progress
        CancellationSignal cancellationSignal = new CancellationSignal();

        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);

    }

    //Called when an unrecoverable error has been encountered and the operation is complete.
    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

        this.update("Authentication Failed: " + errString, false);

    }

    //Called when a fingerprint is valid but not recognized.
    @Override
    public void onAuthenticationFailed() {

        this.update("Access Denied", false);

    }

    //Called when a recoverable error has been encountered during authentication.
    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

        this.update("Authentication Failed: " + helpString, false);

    }

    //Called when a fingerprint is recognized.
    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        //Take there username, and welcome them based on their username
        //this.update("Welcome " + userName, true);

        this.update("Welcome Confidant", true);


        Intent mainIntent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(mainIntent);

    }

    private void update(String message, boolean b) {

        //Gets the TextView Object from the actual Activity
        TextView mFingerprintMessage = (TextView) ((Activity)mContext).findViewById(R.id.fingerprint_message);
        mFingerprintMessage.setText(message);

        CircleImageView mFingerprintImage = (CircleImageView) ((Activity)mContext).findViewById(R.id.fingerprint_image);

        if(b == false){

            mFingerprintMessage.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

        } else { //True ~ Succeeded

            mFingerprintMessage.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));

            //------WHEN UNLOCKED, CHANGE IMAGE TO UNLOCKED LOCK?

            //mFingerprintImage.setImageResource(R.drawable.baseline_lock_open_black_36dp);
            /*Picasso.with(mContext).load(R.drawable.baseline_lock_open_black_36dp).resize(72,72)
                    .into(mFingerprintImage);
            */
        }

    }
}