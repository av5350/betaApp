package com.example.betaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class FormStudConfirmActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_stud_confirm);

        Toast.makeText(FormStudConfirmActivity.this, Helper.studentFinishYear, Toast.LENGTH_SHORT).show();
    }
}