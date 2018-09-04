package com.aguilartristen.confidential;

/**
 * Created by trist on 3/26/2018.
 */

public class Friends {

    public String date;

    public Friends(){
        //Mandatory Constructor
    }

    public Friends(String date){
        this.date = date;
    }

    public void setDate(String date){
        this.date = date;
    }

    public String getDate() {
        return date;
    }
}
