package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.Calendar;

/**
 * The type Admin sort data activity.
 */
public class AdminSortDataActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {
    ListView studentsLV;
    Spinner sortClassSpinner;

    String[] classesList = new String[]{"צפייה בכל הכיתות", "ז", "ח", "ט", "י", "יא", "יב"};

    ArrayList<String> registerStatus = new ArrayList<String>();
    ArrayList<String> firstNames = new ArrayList<String>();
    ArrayList<String> lastNames = new ArrayList<String>();
    ArrayList<String> studentsIDS = new ArrayList<String>();

    ArrayList<String> studentsFormPaths = new ArrayList<String>();

    CustomStudentsLvAdapter studentsAdp;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_sort_data);

        sortClassSpinner = (Spinner) findViewById(R.id.sortClassSpinner);
        studentsLV = (ListView) findViewById(R.id.studentsLV);

        studentsLV.setOnItemClickListener(this);

        studentsAdp = new CustomStudentsLvAdapter(getApplicationContext(),
                registerStatus, firstNames, lastNames, studentsIDS);
        studentsLV.setAdapter(studentsAdp);

        ArrayAdapter<String> classesAdp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, classesList);
        sortClassSpinner.setAdapter(classesAdp);
        sortClassSpinner.setOnItemSelectedListener(this);

        // show the first selection of the spinner (show all grades data)
        sortClassSpinner.setSelection(0);
    }

    /**
     * This function handles a click on student listview row
     *
     * @param adapterView the adapter of the clicked listview
     * @param view the listview that was clicked
     * @param position the position of the clicked row in the adapter
     * @param id the position of the clicked row
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        // if its not that header (that was clicked)
        if (position != 0)
        {
            downloadStudentForm(studentsFormPaths.get(position - 1), studentsIDS.get(position));
        }
    }

    /**
     * This function download a student form from firebase
     *
     * @param formPath the path of the form in firebase
     * @param studentID the id of the student we download his form
     */
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

    /**
     * This function handles a click on the filter data (by grade) spinner
     *
     * @param adapterView the adapter of the clicked spinner
     * @param view the spinner that was clicked
     * @param pos the position of the clicked row in the adapter
     * @param rowId the position of the clicked row
     */
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long rowId) {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int finishYear = 0; // the default (o index - show all students)

        if (pos != 0) {
            finishYear = (7 - pos) + currentYear;
        }

        getStudentsInfo(finishYear);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /**
     * This function takes the data about the students we will put in the listView (by their finish year)
     *
     * @param finishYear a year to filter the data (by grade)
     *                   if its 0 - than show all the students (don't filter the data)
     */
    private void getStudentsInfo(int finishYear)
    {
        Query query = FBref.refStudents.orderByChild("finishYear");

        // the user choose some finish year so get just it's students
        if (finishYear != 0)
            query = query.equalTo(finishYear);

        // clear the previous lv selection and init new values for now
        firstNames.clear();
        lastNames.clear();
        studentsIDS.clear();
        registerStatus.clear();

        firstNames.add("שם פרטי");
        lastNames.add("שם משפחה");
        studentsIDS.add("תעודת זהות");
        registerStatus.add("מצב הרשמה");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                for(DataSnapshot data : dS.getChildren()) {
                    // don't add the students who didnt start the registration form already
                    if (!(((String) data.child("registrationFormID").getValue()).equals(""))) {
                        firstNames.add((String) data.child("firstName").getValue());
                        lastNames.add((String) data.child("lastName").getValue());
                        studentsIDS.add(data.getKey());
                        registerStatus.add(((Long) data.child("status").getValue() == 1) ? "הוגש" : "בתהליך");
                        studentsFormPaths.add((String) data.child("registrationFormID").getValue());
                    }
                }
                studentsAdp.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Create the options menu
     *
     * @param menu the menu
     * @return ture if success
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Where to go when the menu item was selected
     *
     * @param item The menu item that was selected.
     * @return true - if it success
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        // log out the user
        if (id == R.id.logout)
        {
            Helper.logout(getApplicationContext());
        }

        return true;
    }
}