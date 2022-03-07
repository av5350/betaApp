package com.example.betaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class FormStudConfirmActivity extends AppCompatActivity {
    EditText numberBrothers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_stud_confirm);

        numberBrothers = (EditText) findViewById(R.id.numberBrothers);

        Toast.makeText(FormStudConfirmActivity.this, Helper.studentFinishYear, Toast.LENGTH_SHORT).show();

        // all the numeric fields accepts just numbers
        numberBrothers.setTransformationMethod(null);
    }
}