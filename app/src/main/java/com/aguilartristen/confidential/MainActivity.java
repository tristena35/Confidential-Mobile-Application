package com.aguilartristen.confidential;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    //Layout manager that allows the user to flip left and right through pages of data
    private ViewPager mViewPager;

    /*
    Implementation of PagerAdapter that represents each page as a
    Fragment that is persistently kept in the fragment manager as long as the user can return to the page.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private DatabaseReference mUsersRef;

    //TabLayout provides a horizontal layout to display tabs
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.main_custom_bar, null);

        //Settings up Custom Bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);


        /*

        In the Near future, learn how to implement the activity I made:
        app_bar_layout_main, in order to put the name "CONFIDENTIAL" in the center
        on top of the MainActivity Page.

         */

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {

            //Getting Ref
            mUsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        }

        //Tabs
        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //Setting up adapter for ViewPager
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //Setting up tabLayout
        mTabLayout = (TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            sendToStart();

        } else {

            //If the arrive to the mainActivity page, they are online
            mUsersRef.child("online").setValue("true");

            /*
            If value for 'online' is true, that means user is ONLINE
            If value for 'online' is a TIMESTAMP value, that means the user is OFFLINE
             */

            /*The last time they were online will show, and a new child will be created
            mUsersRef.child("lastSeen").setValue(ServerValue.TIMESTAMP);
            */

        }

    }


    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null) {

            //Might have to change this TIMESTAMP because its puts numbers instead of true and false
            mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);

        }

    }

    private void sendToStart() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish(); //So they cannot press back button
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // super.onCreateOptionsMenu(menu);   I commented this out for it to work, but why
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn){
            mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId() == R.id.main_my_account_btn){
            Intent myAccountIntent = new Intent(MainActivity.this, MyAccountActivity.class);
            startActivity(myAccountIntent);
        }

        if(item.getItemId() == R.id.feed_btn){
            Intent feedIntent = new Intent(MainActivity.this, FeedActivity.class);
            startActivity(feedIntent);
        }

        if(item.getItemId() == R.id.make_a_post_btn){
            Intent makePostIntent = new Intent(MainActivity.this, MakePostActivity.class);
            startActivity(makePostIntent);
        }

        if(item.getItemId() == R.id.search_confidant_btn){
            Intent searchIntent = new Intent(MainActivity.this, SearchConfidantActivity.class);
            startActivity(searchIntent);
        }

        if(item.getItemId() == R.id.main_settings_btn){
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        if(item.getItemId() == R.id.main_all_btn){
            Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(usersIntent);
        }

        return true;
    }
}
