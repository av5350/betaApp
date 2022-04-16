package com.example.betaapp;

public class Student {
    private String studentID;
    private String parentUID;
    private String secondParentEmail;
    private String registrationFormID;
    private String interviewDate;
    private int finishYear;
    private int status;
    private int formState;
    //todo: אין סיכוי שיהיה כמה ריאיונות עם תלמיד? לשאול את איציק

    public Student(){

    }

    public Student(String studentID, String parentUID, String secondParentEmail, String registrationFormID, String interviewDate,
                   int finishYear, int status, int formState) {
        this.studentID = studentID;
        this.parentUID = parentUID;
        this.secondParentEmail = secondParentEmail;
        this.registrationFormID = registrationFormID;
        this.interviewDate = interviewDate;
        this.finishYear = finishYear;
        this.status = status;
        this.formState = formState;
    }

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getParentUID() {
        return parentUID;
    }

    public void setParentUID(String parentUID) {
        this.parentUID = parentUID;
    }

    public String getSecondParentEmail() {
        return secondParentEmail;
    }

    public void setSecondParentEmail(String secondParentEmail) {
        this.secondParentEmail = secondParentEmail;
    }

    public String getRegistrationFormID() {
        return registrationFormID;
    }

    public void setRegistrationFormID(String registrationFormID) {
        this.registrationFormID = registrationFormID;
    }

    public String getInterviewDate() {
        return interviewDate;
    }

    public void setInterviewDate(String interviewDate) {
        this.interviewDate = interviewDate;
    }

    public int getFinishYear() {
        return finishYear;
    }

    public void setFinishYear(int finishYear) {
        this.finishYear = finishYear;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFormState() {
        return formState;
    }

    public void setFormState(int formState) {
        this.formState = formState;
    }
}
