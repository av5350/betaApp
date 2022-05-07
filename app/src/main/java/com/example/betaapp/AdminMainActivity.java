package com.example.betaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);
    }

    public void goSortData(View view) {
        Intent si = new Intent(AdminMainActivity.this, AdminSortDataActivity.class);
        startActivity(si);
    }
}