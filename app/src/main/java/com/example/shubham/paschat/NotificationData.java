package com.example.shubham.paschat;

public class NotificationData {
    private String date;
    private String time;
    private String sender;
    private String title;
    private String message;
    private String type;

    public NotificationData(){}

    NotificationData(String date,String time,String sender,String title,String message,String type){
        this.date = date;
        this.time = time;
        this.sender = sender;
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getSender() {
        return sender;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {return type;}

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTitle(String title) {this.title = title;}

    public void setType(String type) {this.type = type;}
}
