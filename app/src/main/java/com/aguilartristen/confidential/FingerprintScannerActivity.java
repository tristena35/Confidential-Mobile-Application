package com.aguilartristen.confidential;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import de.hdodenhof.circleimageview.CircleImageView;

public class FingerprintScannerActivity extends AppCompatActivity{

    private FirebaseAuth mAuth;

    //Layout Item Objects
    private TextView mFingerprintMessage;
    private CircleImageView mProfileImage;

    //Toolbar
    private Toolbar mToolbar;

    //A FinerprintManager Object represents a class that coordinates access to the fingerprint hardware
    private FingerprintManager mFingerprintManager;

    //A KeyguardManager Obkect represents a class that can be used to lock and unlock the keyboard
    private KeyguardManager mKeyguardManager;

    //A KeyStore Object represents a storage facility for cryptographic keys and certificates
    private KeyStore mKeyStore;

    //A Cipher Object provides the functionality of a cryptographic cipher for encryption and decryption
    private Cipher mCipher;

    private String KEY_NAME = "AndroidKey";

    //Passed values from previous activity
    private String userName,profileImage;


    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finger_print);

        //Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.fingerprint_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.fingerprint_custom_bar, null);

        //Setting up custom ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);

        //Layout Elements
        mFingerprintMessage = (TextView)findViewById(R.id.fingerprint_message);
        mProfileImage = (CircleImageView)findViewById(R.id.fingerprint_image);

        //userName = getIntent().getStringExtra("user_name");
        //profileImage = getIntent().getStringExtra("profile_image");

        mAuth = FirebaseAuth.getInstance();

        /*
        Steps for using Fingerprint Authentication:
         */
        // Check 1: Android version should be greater or equal to Marshmallow
        // Check 2: Device has Fingerprint Scanner
        // Check 3: Have permission to use fingerprint scanner in the app
        // Check 4: Lock screen is secured with atleast 1 type of lock
        // Check 5: Atleast 1 Fingerprint is registered

        //Checks to make sure the phones SDK supports Fingerprint Scanning
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //Initializing the FingerprintManager Object
            mFingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            //Initializing the KeyguardManager Object
            mKeyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

            if(!mFingerprintManager.isHardwareDetected()){

                //This statement will occur if the phone does not have a finger print scanner
                mFingerprintMessage.setText("Fingerprint scanner not detected in device");

            }else if (ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){

                //They haven't given permission to use finger print scanner
                mFingerprintMessage.setText("Permission not granted to use fingerprint scanner");

            }else if (!mKeyguardManager.isKeyguardSecure()){

                //They have not enabled the finger print scanner in their phone
                mFingerprintMessage.setText("Add lock to your phone in settings");

            }else if (!mFingerprintManager.hasEnrolledFingerprints()){

                //They have not yet created a fingerprint for their phone
                mFingerprintMessage.setText("You should add at least 1 Fingerprint to use this feature");

            }else {

                //If they are ready to scan in
                mFingerprintMessage.setText("Place your finger on scanner to access the app.");

                /*
                Calls the generateKey() method:
                    - Generates a secret key.
                 */
                generateKey();

                /*
                This method creates a temporary encrypting object for the users finger print.
                If it successfully creates, the if statement is executed, if not, then it is
                not.
                 */
                if(cipherInit()){

                    //Creates an Object representing the wrapper class for the crypto objects supported by FingerprintManager.
                    FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(mCipher);

                    //Here, the other class is used to finalize the process of authentication (Username, Image)
                    //FingerPrintHandlerDialog fingerprintHandler = new FingerPrintHandlerDialog(this, userName);

                    FingerPrintHandlerDialog fingerprintHandler = new FingerPrintHandlerDialog(this);

                    fingerprintHandler.startAuth(mFingerprintManager, cryptoObject);

                }

            }

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private void generateKey() {

        try{

            //Initializes the KeyStore Object
            mKeyStore = KeyStore.getInstance("AndroidKeyStore");

            //This KeyGenerator Object creates a random temporary key
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            //Loads this keystore using the given LoadStoreParameter.
            mKeyStore.load(null);

            //Initializes this key generator with the specified parameter set and a user-provided source of randomness.
            keyGenerator.init(new
                            KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());

            //Generates a secret key.
            keyGenerator.generateKey();

        }catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e){

            //Catches errors and prints them
            e.printStackTrace();

        }catch (Exception e) {

            //Catches errors and prints them
            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {

        try{

            //Creates the temporary encrypting object for the users finger print
            mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);

        }catch (NoSuchAlgorithmException | NoSuchPaddingException e) {

            //Thrown if the above object fails to create correctly
            throw new RuntimeException("Failed to get Cipher", e);

        }

        try{

            //Loads this keystore using the given LoadStoreParameter.
            mKeyStore.load(null);

            //Here the SecretKey Object is created using the KeyStore Object
            SecretKey key = (SecretKey) mKeyStore.getKey(KEY_NAME,null);

            //Initializes this cipher with the above created key
            mCipher.init(Cipher.ENCRYPT_MODE, key);

            //Return true in order to now attempt the fingerprint authentication
            return true;

        }catch (KeyPermanentlyInvalidatedException e) {

            //This catches an invalid key exception, and will not authenticate the user
            return false;

        }catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {

            //Several errors are caught here
            throw new RuntimeException("Failed to init Cipher", e);

        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Checks to see if there is no one logged in
        if(currentUser == null){

            sendToStart();

        }else {

            Log.d("UID",currentUser.getUid());

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case android.R.id.home:
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onStop() {

        super.onStop();

    }

    private void sendToMain() {

        Intent mainIntent = new Intent(FingerprintScannerActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish(); //So they cannot press back button

    }

    private void sendToStart() {

        Intent startIntent = new Intent(FingerprintScannerActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish(); //So they cannot press back button

    }


}
