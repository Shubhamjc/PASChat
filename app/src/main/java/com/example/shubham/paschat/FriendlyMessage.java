package com.example.shubham.paschat;

public class FriendlyMessage {
    private String text;
    private String name;
    private String number;
    private String photoUrl;
    private String date;
    private String time;

    //Constructor
    public FriendlyMessage(){}

    public FriendlyMessage(String text, String name,String number, String photoUrl,String date,String time) {
        this.text = text;
        this.name = name;
        this.number = number;
        this.photoUrl = photoUrl;
        this.date = date;
        this.time = time;
    }

    public String getText() {return text;}

    public void setText(String text) {this.text = text;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public String getPhotoUrl() {return photoUrl;}

    public void setPhotoUrl(String photoUrl) {this.photoUrl = photoUrl;}

    public String getTime() {return time;}

    public void setTime(String time) {this.time = time;}

    public String getDate() {return date;}

    public void setDate(String date) {this.date = date;}

    public void setNumber(String number) {
        this.number = number;
    }

    public String getNumber() {return number;}
}
