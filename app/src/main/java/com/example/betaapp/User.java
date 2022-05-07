package com.example.betaapp;

import java.util.ArrayList;

public class User {
    private String uid;
    private int role;
    private ArrayList<String> childrenID;
    private String firstName;
    private String lastName;
    private boolean isDad;

    public User(){}

    public User(String uid, int role, ArrayList<String> childrenID, String firstName, String lastName, boolean isDad){
        this.uid = uid;
        this.role = role;
        this.childrenID = childrenID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isDad = isDad;
    }

    public String getUid(){
        return uid;
    }

    public int getRole(){
        return role;
    }

    public ArrayList<String> getChildrenId(){
        return childrenID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isDad() {
        return isDad;
    }

    public void setDad(boolean dad) {
        isDad = dad;
    }
}