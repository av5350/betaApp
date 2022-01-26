package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SelectChildActivity extends AppCompatActivity {
    AlertDialog.Builder newChildDialog;
    ListView childrenLV;
    ArrayAdapter<String> adp;
    ArrayList<String> childrenID = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_child);

        childrenLV = (ListView) findViewById(R.id.childrenLV);

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
                    adp.notifyDataSetChanged();
                }
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
}