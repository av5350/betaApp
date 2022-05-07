package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

public class AdminSortDataActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    ListView studentsLV;

    ArrayList<String> registerStatus = new ArrayList<String>();
    ArrayList<String> firstNames = new ArrayList<String>();
    ArrayList<String> lastNames = new ArrayList<String>();
    ArrayList<String> studentsIDS = new ArrayList<String>();

    ArrayList<String> studentsFormPaths = new ArrayList<String>();

    CustomStudentsLvAdapter adp;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sort_data);

        studentsLV = (ListView) findViewById(R.id.studentsLV);

        studentsLV.setOnItemClickListener(this);

        adp = new CustomStudentsLvAdapter(getApplicationContext(),
                registerStatus, firstNames, lastNames, studentsIDS);
        studentsLV.setAdapter(adp);

        getStudentsInfo();
    }

    // לוקח המפיירבייס את הנתונים על התלמידים שנציג בlistview
    private void getStudentsInfo()
    {
        firstNames.add("שם פרטי");
        lastNames.add("שם משפחה");
        studentsIDS.add("תעודת זהות");
        registerStatus.add("מצב הרשמה");

        FBref.refStudents.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                for(DataSnapshot data : dS.getChildren()) {
                    // don't add the students who didnt start the registration firm already
                    if (!(((String) data.child("registrationFormID").getValue()).equals(""))) {
                        firstNames.add((String) data.child("firstName").getValue());
                        lastNames.add((String) data.child("lastName").getValue());
                        studentsIDS.add(data.getKey());
                        registerStatus.add(((Long) data.child("status").getValue() == 1) ? "הוגש" : "בתהליך");
                        studentsFormPaths.add((String) data.child("registrationFormID").getValue());
                    }
                }
                adp.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        // if its not that header (that was clicked)
        if (i != 0)
        {
            downloadStudentForm(studentsFormPaths.get(i - 1), studentsIDS.get(i));
        }
    }

    private void downloadStudentForm(String formPath, String studentID)
    {
        StorageReference pathReference;
        File localFile;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("מוריד קובץ עזר");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.show();

        pathReference = FBref.storageRef.child("forms").child(formPath);
        localFile = new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/" + studentID + ".xml");

        try {
            localFile.createNewFile();
            localFile.setReadable(true);
            localFile.setWritable(true);

            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local file has been created
                    progressDialog.dismiss();

                    Intent si = new Intent(AdminSortDataActivity.this, StudentInfoActivity.class);
                    si.putExtra("studentID", studentID);
                    startActivity(si);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    localFile.delete();
                    Toast.makeText(AdminSortDataActivity.this, "An error occurred. Try again!", Toast.LENGTH_SHORT).show();
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