package com.example.betaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class FormStudConfirmActivity extends AppCompatActivity {
    EditText numberBrothers;
    CheckBox checkboxAgreeContact, checkboxShareVideo;
    SeekBar seekbarState;

    ArrayList<String> elementsIds = new ArrayList<>();
    HashMap<String, String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_stud_confirm);

        numberBrothers = (EditText) findViewById(R.id.numberBrothers);
        checkboxAgreeContact = (CheckBox) findViewById(R.id.checkboxAgreeContact);
        checkboxShareVideo = (CheckBox) findViewById(R.id.checkboxShareVideo);

        seekbarState = (SeekBar) findViewById(R.id.seekbarState);

        // the user could not change the seekbar state by clicking
        seekbarState.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        Toast.makeText(FormStudConfirmActivity.this, Helper.studentFinishYear, Toast.LENGTH_SHORT).show();

        // all the numeric fields accepts just numbers
        numberBrothers.setTransformationMethod(null);

        elementsIds.add("agreeContact");
        elementsIds.add("shareVideo");
        elementsIds.add("numberBrothers");

        // get the current data from the xml file
        data = XmlHelper.getData(elementsIds);

        // update elements with their current values (from the xml)
        numberBrothers.setText(data.get("numberBrothers"));
        checkboxAgreeContact.setChecked(Boolean.parseBoolean(data.get("agreeContact")));
        checkboxShareVideo.setChecked(Boolean.parseBoolean(data.get("shareVideo")));
    }

    public void saveData(View view) {
        // if one of the fields is empty
        if (!checkboxShareVideo.isChecked() || !checkboxAgreeContact.isChecked() || (numberBrothers.getText().toString().equals("")))
        {
            Toast.makeText(FormStudConfirmActivity.this, "חובה להזין נתונים לכל השדות המסומנים", Toast.LENGTH_SHORT).show();
        }
        else {
            data.put("agreeContact", String.valueOf(checkboxAgreeContact.isChecked()));
            data.put("shareVideo", String.valueOf(checkboxShareVideo.isChecked()));
            data.put("numberBrothers", numberBrothers.getText().toString());

            XmlHelper.pushData(data);

            // if want to move page
            // this view has parameter of tag - so we know to move page
            if (view.getTag().equals("move"))
            {
                Intent si = new Intent(FormStudConfirmActivity.this, FormParentsActivity.class);
                si.putExtra("dad", true); // false = mom
                startActivity(si);
            }
        }
    }

    public void back(View view) {
        finish();
    }
}