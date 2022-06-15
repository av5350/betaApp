package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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

import org.apache.commons.validator.routines.EmailValidator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The type Select child activity.
 */
public class SelectChildActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    AlertDialog.Builder newChildDialog;
    ListView childrenLV;
    TextView welcomeMsg;
    ArrayAdapter<String> adp;
    ArrayList<String> childrenID = new ArrayList<String>();

    ProgressDialog progressDialog;

    String childID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_child);

        welcomeMsg = (TextView) findViewById(R.id.welcomeMsg);
        childrenLV = (ListView) findViewById(R.id.childrenLV);

        childrenLV.setOnItemClickListener(this);

        adp = new ArrayAdapter<String>(SelectChildActivity.this, R.layout.support_simple_spinner_dropdown_item, childrenID);
        childrenLV.setAdapter(adp);

        initUI();
    }

    /**
     * Init the welcome message (with the parents first name), and fill the listview data with the children id
     */
    private void initUI()
    {
        FBref.refUsers.child(FBref.auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                boolean isDad = dS.child("isDad").getValue(Boolean.class);

                if (isDad)
                    welcomeMsg.setText("ברוך הבא " + dS.child("firstName").getValue(String.class));
                else // its a mom
                    welcomeMsg.setText("ברוכה הבאה " + dS.child("firstName").getValue(String.class));

                for (DataSnapshot data : dS.child("childrenID").getChildren()) {
                    childrenID.add(data.getValue(String.class));
                }
                adp.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * Add child dialog - get from the user the child id and the second parent mail.
     *
     * @param view the view
     */
    public void addChild(View view) {
        newChildDialog = new AlertDialog.Builder(this);
        newChildDialog.setTitle("הכנס ילד חדש");

        final EditText studentID = new EditText(this);
        final EditText secondParentEmail = new EditText(this);

        studentID.setHint("תעודת זהות ילד באורך 9 ספרות");
        secondParentEmail.setHint("מייל הורה שני. אם אין הורה שני - נא להשאיר ריק");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(studentID);
        layout.addView(secondParentEmail);

        newChildDialog.setView(layout);

        newChildDialog.setPositiveButton("הוסף", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // if the id is good - 9 digits and in good format
                if ((studentID.getText().toString().length() == 9) && (Helper.checkID(studentID.getText().toString()))) {
                    // if there is no value in the second parent mail field - we know this student has 1 parent
                    if ((secondParentEmail.getText().toString().equals("")) || (EmailValidator.getInstance().isValid(secondParentEmail.getText().toString())))
                    {
                        checkIdInDB(studentID.getText().toString(), secondParentEmail.getText().toString());
                    }
                    else // the mail field value is bad
                    {
                        Toast.makeText(SelectChildActivity.this, "המייל שהוזן לא בפורמט הנכון, אנא נסה שנית!", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(SelectChildActivity.this, "תעודת הזהות שגויה או אינה בפורמט הנכון, אנא נסה שנית!", Toast.LENGTH_SHORT).show();
                }
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

    /**
     * Check that the id is not already in the db (there are no 2 people with the same id)
     *
     * @param id the id to check
     * @param secondParentEmail the additional info to push to db if the id is not already in the db
     */
    private void checkIdInDB(String id, String secondParentEmail)
    {
        FBref.refStudents.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                // if the id is already in the db
                if (dS.exists())
                    Toast.makeText(SelectChildActivity.this, "תעודת הזהות כבר נמצאת במערכת!", Toast.LENGTH_SHORT).show();
                else {
                    Student newStudent = new Student(id, FBref.auth.getCurrentUser().getUid(), secondParentEmail, "", 0, 0);
                    FBref.refStudents.child(id).setValue(newStudent);

                    // update the lv and the list of children of a person in db
                    childrenID.add(id);
                    FBref.refUsers.child(FBref.auth.getCurrentUser().getUid() + "/childrenID").setValue(childrenID);
                    adp.notifyDataSetChanged();
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        childID = childrenID.get(i);

        // get the children object from firebase
        FBref.refStudents.child(childID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                int status = (int) dS.child("status").getValue(Integer.class);
                String formPath = dS.child("registrationFormID").getValue(String.class);

                // if the student didnt finished the form - let him continue it
                if (status == 0)
                {
                    // download the student's xml form file (or template)
                    downloadXML(formPath);
                }
                else
                {
                    Toast.makeText(SelectChildActivity.this, "לא ניתן להשלים את הפעולה, השאלון כבר הוגש", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Download the xml form for the selected child
     *
     * @param formPath the path to the wanted form (or "" if want to download a template xml)
     */
    private void downloadXML(String formPath)
    {
        StorageReference pathReference;
        File localFile;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("מוריד קבצי עזר");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.show();

        // if wants to download a template xml (user doesnt have an xml form already)
        if (formPath.equals("")) {
            // Create a reference with an initial file path and name
            pathReference = FBref.storageRef.child("forms/template.xml");
            localFile = new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/student_tempXML.xml");
        }
        else // the file already exists in the db
        {
            pathReference = FBref.storageRef.child("forms").child(formPath);
            localFile = new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/" + childID + ".xml");
        }

        try {
            localFile.createNewFile();
            localFile.setReadable(true);
            localFile.setWritable(true);

            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    progressDialog.dismiss();

                    // move to form activity
                    Intent si = new Intent(SelectChildActivity.this, FormActivity.class);
                    si.putExtra("id", childID);
                    startActivity(si);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    localFile.delete();
                    Toast.makeText(SelectChildActivity.this, "התרחשה בעיה בעת הורדת הקובץ. אנא נסה שנית!", Toast.LENGTH_SHORT).show();
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
        else if(id == R.id.credits)
        {
            Intent si = new Intent(SelectChildActivity.this, CreditsActivity.class);
            startActivity(si);
        }

        return true;
    }
}