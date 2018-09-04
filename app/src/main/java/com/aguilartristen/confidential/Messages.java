package com.aguilartristen.confidential;

public class Messages {

    //Type for if its an image or text
    private String message, type;
    private boolean seen;
    private long time;
    //Who sent message
    private String from;
    private String timesent;

    public Messages(String message, boolean seen, long time, String type, String timesent){

        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.timesent = timesent;

    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(){

    } // Default Constructor

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTimesent() {
        return timesent;
    }

    public void setTimesent(String timesent) {
        this.timesent = timesent;
    }

}
