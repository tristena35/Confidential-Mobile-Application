package com.aguilartristen.confidential;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by trist on 2/28/2018.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter {

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        //Return 3 because we have 3 tabs in the main page
        return 3;
    }

    public CharSequence getPageTitle(int position){
        switch(position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHAT";
            case 2:
                return "CONFIDANTS";
            default:
                return null;
        }
    }

}
