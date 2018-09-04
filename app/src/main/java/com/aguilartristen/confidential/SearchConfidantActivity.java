package com.aguilartristen.confidential;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class SearchConfidantActivity extends AppCompatActivity {

    private EditText mSearchName;

    private RecyclerView mSearchResultList;

    //DatabaseRef
    private DatabaseReference mDatabaseReference;

    private FirebaseUser mFirebaseUser;

    //--Lists to hold current users Info as they dynamically populate
    private ArrayList<String> uidList;
    private ArrayList<String> nameList;
    private ArrayList<String> profilePicList;
    private ArrayList<String> statusList;

    //Search Adapter to construct the list
    protected SearchAdapter mSearchAdapter;

    //TextView that shows no confidants if dataset is empty
    private TextView mEmptySearchView;

    //Toolbar
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Toolbar Set
        mToolbar = (Toolbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //This is where we inflate our layout as of now with the new one with the image
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.search_custom_bar, null);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(action_bar_view);

        mSearchName = (EditText) findViewById(R.id.search_field);
        mSearchResultList = (RecyclerView) findViewById(R.id.search_result_list);

        //Default textview for no search results
        //mEmptySearchView = (TextView)findViewById(R.id.search_confidants_empty_view);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //--Setting Up the RecyclerView
        mSearchResultList.setHasFixedSize(true);
        mSearchResultList.setLayoutManager(new LinearLayoutManager(this));
        mSearchResultList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        /*
         * Create a array list for each node you want to use
         * */
        uidList = new ArrayList<>();
        nameList = new ArrayList<>();
        statusList = new ArrayList<>();
        profilePicList = new ArrayList<>();

        mSearchName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    setAdapter(s.toString());
                } else {
                    /*
                     * Clear the list when editText is empty
                     * */
                    uidList.clear();
                    nameList.clear();
                    statusList.clear();
                    profilePicList.clear();
                    mSearchResultList.removeAllViews();
                }
            }
        });
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

    private void setAdapter(final String searchedString) {
        mDatabaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                /*
                 * Clear the list for every new search
                 * */
                uidList.clear();
                nameList.clear();
                statusList.clear();
                profilePicList.clear();
                mSearchResultList.removeAllViews();

                int counter = 0;

                /*
                 * Search all users for matching searched string
                 * */
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String uid = snapshot.getKey();
                    String name = snapshot.child("name").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String profile_pic = snapshot.child("thumb_image").getValue(String.class);

                    if (name.toLowerCase().contains(searchedString.toLowerCase())) {
                        uidList.add(uid);
                        nameList.add(name);
                        statusList.add(status);
                        profilePicList.add(profile_pic);
                        counter++;
                    } /*else if (user_name.toLowerCase().contains(searchedString.toLowerCase())) {
                        fullNameList.add(full_name);
                        //userNameList.add(user_name);
                        profilePicList.add(profile_pic);
                        counter++;
                    }*/

                    /*
                     * Get maximum of 15 searched results only
                     * */
                    if (counter == 15)
                        break;
                }

                mSearchAdapter = new SearchAdapter(SearchConfidantActivity.this, nameList, profilePicList, statusList, uidList);
                mSearchResultList.setAdapter(mSearchAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*public void switchToProfile(String uid){
        Intent profile_Intent = new Intent(SearchConfidantActivity.this, ProfileActivity.class);
        profile_Intent.putExtra("user_id", uid);
        startActivity(profile_Intent);
    }*/

}
