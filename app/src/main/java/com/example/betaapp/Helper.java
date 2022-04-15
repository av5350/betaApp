package com.example.betaapp;

import android.app.Dialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helper {
    public static String studentFormDestPath = "";
    public static String studentFinishYear = "";

    public static String currentStudentId = "";

    public static boolean isEmpty(EditText et, TextInputLayout layout)
    {
        boolean isEmpty = false;
        String text = et.getText().toString();

        if(TextUtils.isEmpty(text)) {
            if (layout == null) {
                et.setError("לא יכול להיות ריק");
                et.requestFocus();
            }
            else
            {
                layout.setErrorEnabled(true);
                layout.requestFocus();
            }
            isEmpty = true;
        }

        return isEmpty;
    }

    public static boolean isHebrew(EditText et)
    {
        boolean matchFound = true; // if text is "" its still good
        String text = et.getText().toString();

        if (!text.isEmpty()) {
            // א-ת and '
            Pattern pattern = Pattern.compile("^[\u0590-\u05FF \" ’ ']+$");
            Matcher matcher = pattern.matcher(et.getText().toString());

            matchFound = matcher.find();

            if (!matchFound) {
                et.setError("חובה להיות בעברית");
            }
        }

        return matchFound;
    }

    // FragmentManager because of the function getSupportFragmentManager:
    // to interact with the fragments associated with the material design date picker
    // and to put any error in logcat (because of the date picker)
    public static void initDatePicker(TextView textView, FragmentManager manager) {
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();

        // set the validator point forward from june
        // this mean the all the dates before the June month
        // are blocked
        calendarConstraintBuilder.setValidator(DateValidatorPointBackward.now());

        MaterialDatePicker.Builder dateBuilder = MaterialDatePicker.Builder.datePicker();

        dateBuilder.setTitleText("SELECT A DATE");
        dateBuilder.setCalendarConstraints(calendarConstraintBuilder.build());

        // create the instance of the material date
        final MaterialDatePicker materialDatePicker = dateBuilder.build();

        textView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // check if the dialog was opened already - so don't open it again
                        Dialog dialogFrg = materialDatePicker.getDialog();
                        if(dialogFrg == null || !dialogFrg.isShowing())
                            materialDatePicker.show(manager, "MATERIAL_DATE_PICKER");
                    }
                });

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        // format the selected date
                        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        utc.setTimeInMillis((long) selection);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        String formatted = format.format(utc.getTime());

                        textView.setText(formatted);
                    }
                });

    }

    public static boolean checkID(String id)
    {
        return false;
    }
}
