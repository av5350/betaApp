package com.example.betaapp;

import java.util.ArrayList;

/**
 * The type User.
 */
public class User {
    private String uid;
    private int role;
    private String firstName;
    private String lastName;
    private boolean isDad;

    /**
     * Instantiates a new User.
     */
    public User(){}

    /**
     * Instantiates a new User.
     *
     * @param uid       the uid
     * @param role      the role
     * @param firstName the first name
     * @param lastName  the last name
     * @param isDad     the is dad
     */
    public User(String uid, int role, String firstName, String lastName, boolean isDad){
        this.uid = uid;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isDad = isDad;
    }

    /**
     * Gets uid.
     *
     * @return the uid
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets uid.
     *
     * @param uid the uid
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets role.
     *
     * @return the role
     */
    public int getRole() {
        return role;
    }

    /**
     * Sets role.
     *
     * @param role the role
     */
    public void setRole(int role) {
        this.role = role;
    }

    /**
     * Gets first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Gets last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets last name.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Gets is dad.
     *
     * @return the is dad
     */
    public boolean getIsDad() {
        return isDad;
    }

    /**
     * Sets is dad.
     *
     * @param isDad the is dad
     */
    public void setIsDad(boolean isDad) {
        this.isDad = isDad;
    }
}