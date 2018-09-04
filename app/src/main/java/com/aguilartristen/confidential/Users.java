package com.aguilartristen.confidential;

/**
 * Created by trist on 3/4/2018.
 */

public class Users {

    //These string names should match the key for their names in Firebase
    public String name;
    public String image;
    public String status;
    public String thumb_image;

    //Without this default constructor, app may crash
    public Users(){

    }

    public Users(String name, String image, String status, String thumb_image) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image(){
        return thumb_image;
    }

    public void setThumb_image(String thumb_image){
        this.thumb_image = thumb_image;
    }

}
