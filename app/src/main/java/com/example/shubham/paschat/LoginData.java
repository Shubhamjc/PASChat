package com.example.shubham.paschat;

public class LoginData {
    private String name;
    private String number;
    private Boolean admin;

    public LoginData() {}

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public LoginData(String name, String number){
        this.name = name;
        this.number = number;
    }

    public LoginData(String name, String number,Boolean admin){
        this.name = name;
        this.number = number;
        this.admin = admin;
    }
}
