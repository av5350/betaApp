package com.example.betaapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

/**
 * The type Helper class for the app.
 * @author Itey Weintraub <av5350@bs.amalnet.k12.il>
 * @version	1
 * short description:
 *
 *      This class is for some used functions to appear once in the application code (here)
 */
public class Helper {
    // the local path of the student's form
    public static String studentFormDestPath = "";

    public static int studentFinishYear = 0;

    public static String currentStudentId = "";

    /**
     * Check if EditText or designed spinner is empty
     * and if its empty, the function put error hint on the element
     *
     * @param et     the edittext
     * @param layout the layout (for designed spinner elements)
     *               (null if the check is on edittext)
     * @return if the edittext or the spinner is empty or not
     */
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

    /**
     * Is edittext's value is in hebrew or not
     * empty value is still in hebrew
     *
     * @param et the EditText to check
     * @return if the value is in hebrew or not
     */
    public static boolean isHebrew(EditText et)
    {
        boolean matchFound = true; // if text is "" its still good
        String text = et.getText().toString();

        if (!TextUtils.isEmpty(text)) {
            //" א-ת and '
            // are ok
            Pattern pattern = Pattern.compile("^[\u0590-\u05FF \" ’ ']+$");
            Matcher matcher = pattern.matcher(et.getText().toString());

            matchFound = matcher.find();

            if (!matchFound) {
                et.setError("חובה להיות בעברית");
            }
        }

        return matchFound;
    }

    /**
     * Init date picker.
     *
     * @param textView the text view that would start the date picker dialog
     * @param manager  the manager so we can interact with the material designed date pickers
     */
    public static void initDatePicker(TextView textView, FragmentManager manager) {
        CalendarConstraints.Builder calendarConstraintBuilder = new CalendarConstraints.Builder();

        // set the validator point before now
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

    /**
     * This function removes the Credential if user doesn't want to stay connected
     *
     * @param context the screen context
     */
    public static void removeUserCredential(Context context)
    {
        SharedPreferences signInState = context.getSharedPreferences("States", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = signInState.edit();
        editor.putString("credential", "");
        editor.commit();
    }

    /**
     * Logout the user from the app.
     *
     * @param context the context
     */
    public static void logout(Context context) {
        FBref.auth.signOut();
        removeUserCredential(context); // remove the Credential because we logged out

        Intent si = new Intent(context, MainActivity.class);
        si.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // make that the user could not return activities back (clear the stack)
        context.startActivity(si);
    }

    /**
     * Check if inputted id is a read israeli id or not
     *
     * @param id the id to check
     * @return true - valid id, false - its not a valid id
     */
    public static boolean checkID(String id)
    {
        int sumIdNumbers = 0;
        int currDigit = 0;

        // break a number to its digits
        char[] idNumbers = id.toCharArray();

        for (int i = 0; i < 9; i++)
            {
                currDigit = Character.getNumericValue(idNumbers[i]);

                // digit in odd index (multiply by 1) and in even index (multiply by 2)
                currDigit *= (i % 2) + 1;

                // every number the bigger than 9 will be the sum of the 2 digits that creates it
                // so we minus the number by 9 (18 will be 1+8=9)
                if (currDigit > 9)
                    currDigit -= 9;

                sumIdNumbers += currDigit;
            }

            // if the sum divided by 10 with no remainder - its good id
            return (sumIdNumbers % 10 == 0);
        }
    }
