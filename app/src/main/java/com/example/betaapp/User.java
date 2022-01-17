package com.example.betaapp;

import java.util.ArrayList;

public class User {
    private String uid;
    private int role;
    private ArrayList<String> childrenID;

    public User(){}

    public User(String uid, int role, ArrayList<String> childrenID){
        this.uid = uid;
        this.role = role;
        this.childrenID = childrenID;
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
}

// איך לערוך את הדאטאבייס לפי רולים שמי שלא התחבר עדיין יוכל לגשת רק לעמודה של מיילים וטלפונים
