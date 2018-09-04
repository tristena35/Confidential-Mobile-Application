package com.aguilartristen.confidential;

/**
 * Created by trist on 3/26/2018.
 */

public class FriendReqs {

    public String date;
    private String request_type;

    public FriendReqs(){

        //Mandatory Constructor

    }

    public FriendReqs(String date){
        this.date = date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public String getRequest_type() {
        return request_type;
    }

    public void setRequest_type(String request_type) {
        this.request_type = request_type;
    }
}
