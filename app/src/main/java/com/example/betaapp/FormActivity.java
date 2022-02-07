package com.example.betaapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class FormActivity extends AppCompatActivity {
    int progress = 0;
    TextView studentId;
    SeekBar seekbarState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        studentId = (TextView) findViewById(R.id.studentId);

        Intent gi = getIntent();

        studentId.setText("Hi " + gi.getStringExtra("id"));

        seekbarState = (SeekBar) findViewById(R.id.seekbarState);

        seekbarState.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });
    }

    public void prevFragment(View view) {
        if (progress > 0) {
            progress--;
        }
        seekbarState.setProgress(progress);
    }

    public void nextFragment(View view) {
        if (progress < 6) {
            progress++;
        }
        seekbarState.setProgress(progress);
    }
}