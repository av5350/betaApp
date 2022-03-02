package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SelectChildActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener {
    AlertDialog.Builder newChildDialog;
    ListView childrenLV;
    ArrayAdapter<String> adp;
    ArrayList<String> childrenID = new ArrayList<String>();

    ProgressDialog progressDialog;

    String childID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_child);

        childrenLV = (ListView) findViewById(R.id.childrenLV);

        childrenLV.setOnItemLongClickListener(this);

        adp = new ArrayAdapter<String>(SelectChildActivity.this, R.layout.support_simple_spinner_dropdown_item, childrenID);
        childrenLV.setAdapter(adp);

        getChildren();
    }

    public void getChildren()
    {
        FBref.refUsers.child(FBref.auth.getCurrentUser().getUid() + "/childrenID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                for (DataSnapshot data : dS.getChildren()) {
                    childrenID.add(data.getValue(String.class));
                }
                adp.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void addChild(View view) {
        newChildDialog = new AlertDialog.Builder(this);
        newChildDialog.setTitle("הכנס ילד חדש");

        final EditText studentID = new EditText(this);
        final EditText secondParentEmail = new EditText(this);

        studentID.setHint("תעודת זהות ילד");
        secondParentEmail.setHint("מייל הורה שני");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(studentID);
        layout.addView(secondParentEmail);

        newChildDialog.setView(layout);

        newChildDialog.setPositiveButton("הוסף", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Student newStudent = new Student(studentID.getText().toString(), FBref.auth.getCurrentUser().getUid(), secondParentEmail.getText().toString(), "", "");
                FBref.refStudents.child(studentID.getText().toString()).setValue(newStudent);

                childrenID.add(studentID.getText().toString());
                FBref.refUsers.child(FBref.auth.getCurrentUser().getUid() + "/childrenID").setValue(childrenID);
                adp.notifyDataSetChanged();
            }
        });

        newChildDialog.setNegativeButton("בטל", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        newChildDialog.show();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        childID = childrenID.get(i);

        // get the registerFormId from the child object in firebase
        FBref.refStudents.child(childID).child("registrationFormID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                String formPath = (String) dS.getValue();

                // if the user doesnt have a xml form
                if (formPath.equals(""))
                {
                    downloadTemplateXML();
                }
                else
                {
                    downloadChildXml(formPath);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        return false;
    }

    private void goFormActivity()
    {
        Intent si = new Intent(SelectChildActivity.this, FormActivity.class);
        si.putExtra("id", childID);
        startActivity(si);
    }

    private void downloadTemplateXML()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("מוריד קבצי עזר");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.show();

        // Create a reference with an initial file path and name
        StorageReference pathReference = FBref.storageRef.child("forms/template.xml");

        File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/student_tempXML.xml");
        try {
            localFile.createNewFile();
            localFile.setReadable(true);
            localFile.setWritable(true);

            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    progressDialog.dismiss();
                    goFormActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    localFile.delete();
                    Toast.makeText(SelectChildActivity.this, "An error occurred. Try again!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>(){
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                    //calculating progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                    progressDialog.setProgress((int) progress);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadChildXml(String formPath)
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("מוריד קבצי עזר");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.show();

        // Create a reference with an initial file path and name
        StorageReference pathReference = FBref.storageRef.child("forms").child(formPath);

        File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + childID + ".xml");
        try {
            localFile.createNewFile();
            localFile.setReadable(true);
            localFile.setWritable(true);

            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    progressDialog.dismiss();
                    goFormActivity();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    localFile.delete();
                    Toast.makeText(SelectChildActivity.this, "An error occurred. Try again!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>(){
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                    //calculating progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                    progressDialog.setProgress((int) progress);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}