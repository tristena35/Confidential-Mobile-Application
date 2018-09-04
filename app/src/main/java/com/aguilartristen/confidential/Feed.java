package com.aguilartristen.confidential;

/**
 * Created by trist on 8/12/2018.
 */

public class Feed {

    //These string names should match the key for their names in Firebase
    public int likes;
    public int dislikes;
    public String name;
    public String message;
    public String thumb_image;
    public String time_posted;
    public long timestamp;

    //Without this default constructor, app may crash
    public Feed(){
    }

    public Feed(String name, String message, int likes, int dislikes, String thumb_image, String time_posted, long timestamp) {
        this.name = name;
        this.message = message;
        this.likes = likes;
        this.dislikes = dislikes;
        this.thumb_image = thumb_image;
        this.time_posted = time_posted;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public String getThumbImage() {
        return thumb_image;
    }

    public void setThumbImage(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public int getLikesCount(){
        return likes;
    }

    public void setLikesCount(int likes){
        this.likes = likes;
    }

    public int getDislikesCount(){
        return dislikes;
    }

    public void setDislikesCount(int dislikes){
        this.dislikes = dislikes;
    }

    public String getTimePosted() {
        return time_posted;
    }

    public void setTimePosted(String time_posted) {
        this.time_posted = time_posted;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

}
