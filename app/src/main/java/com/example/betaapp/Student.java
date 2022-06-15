package com.example.betaapp;

/**
 * The type Student.
 */
public class Student {
    private String studentID;
    private String parentUID;
    private String secondParentEmail;
    private String registrationFormID;
    private int finishYear;
    private int status;

    /**
     * Instantiates a new Student.
     */
    public Student(){

    }

    /**
     * Instantiates a new Student.
     *
     * @param studentID          the student id
     * @param parentUID          the parent uid
     * @param secondParentEmail  the second parent email
     * @param registrationFormID the registration form id (in firebase)
     * @param finishYear         the finish year
     * @param status             the status
     */
    public Student(String studentID, String parentUID, String secondParentEmail, String registrationFormID,
                   int finishYear, int status) {
        this.studentID = studentID;
        this.parentUID = parentUID;
        this.secondParentEmail = secondParentEmail;
        this.registrationFormID = registrationFormID;
        this.finishYear = finishYear;
        this.status = status;
    }

    /**
     * Gets student id.
     *
     * @return the student id
     */
    public String getStudentID() {
        return studentID;
    }

    /**
     * Sets student id.
     *
     * @param studentID the student id
     */
    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    /**
     * Gets parent uid.
     *
     * @return the parent uid
     */
    public String getParentUID() {
        return parentUID;
    }

    /**
     * Sets parent uid.
     *
     * @param parentUID the parent uid
     */
    public void setParentUID(String parentUID) {
        this.parentUID = parentUID;
    }

    /**
     * Gets second parent email.
     *
     * @return the second parent email
     */
    public String getSecondParentEmail() {
        return secondParentEmail;
    }

    /**
     * Sets second parent email.
     *
     * @param secondParentEmail the second parent email
     */
    public void setSecondParentEmail(String secondParentEmail) {
        this.secondParentEmail = secondParentEmail;
    }

    /**
     * Gets registration form id.
     *
     * @return the registration form id
     */
    public String getRegistrationFormID() {
        return registrationFormID;
    }

    /**
     * Sets registration form id.
     *
     * @param registrationFormID the registration form id
     */
    public void setRegistrationFormID(String registrationFormID) {
        this.registrationFormID = registrationFormID;
    }

    /**
     * Gets finish year.
     *
     * @return the finish year
     */
    public int getFinishYear() {
        return finishYear;
    }

    /**
     * Sets finish year.
     *
     * @param finishYear the finish year
     */
    public void setFinishYear(int finishYear) {
        this.finishYear = finishYear;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(int status) {
        this.status = status;
    }
}
