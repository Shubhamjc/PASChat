package com.example.shubham.paschat;

import android.app.Application;

public class GlobalClass extends Application{
    private String phoneNumber;
    private String userName;
    private String activeGroup;

    public String getUserName() {
        return userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getActiveGroup() {
        return activeGroup;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setActiveGroup(String activeGroup) {
        this.activeGroup = activeGroup;
    }
}

