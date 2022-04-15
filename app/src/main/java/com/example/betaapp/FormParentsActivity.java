package com.example.betaapp;

import static com.example.betaapp.Helper.initDatePicker;
import static com.example.betaapp.Helper.isEmpty;
import static com.example.betaapp.Helper.isHebrew;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.HashMap;

public class FormParentsActivity extends AppCompatActivity {
    EditText parentFirstName, parentLastName, parentID, parentBirthCountry, parentProfession, parentEducationYears,
            parentWorkPlace, parentPhone, parentEmail;
    TextInputLayout maritalStatus;
    TextView parentBirthDate, parentType;
    SeekBar seekbarState;

    String isDadActivity; // if the activity is Dad/mom

    HashMap<String, String> data;
    EditText[] editTexts;
    HashMap<Integer, String> ids = new HashMap<>();
    String[] maritalStatusList = new String[]{"רווק", "נשוי", "אלמן", "גרוש"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_parents);

        parentFirstName = (EditText) findViewById(R.id.parentFirstName);
        parentLastName = (EditText) findViewById(R.id.parentLastName);
        parentID = (EditText) findViewById(R.id.parentID);
        parentBirthCountry = (EditText) findViewById(R.id.parentBirthCountry);
        parentProfession = (EditText) findViewById(R.id.parentProfession);
        parentEducationYears = (EditText) findViewById(R.id.parentEducationYears);
        parentWorkPlace = (EditText) findViewById(R.id.parentWorkPlace);
        parentPhone = (EditText) findViewById(R.id.parentPhone);
        parentEmail = (EditText) findViewById(R.id.parentEmail);

        maritalStatus = (TextInputLayout) findViewById(R.id.maritalStatus);
        parentBirthDate = (TextView) findViewById(R.id.parentBirthDate);

        parentType = (TextView) findViewById(R.id.parentType); // just for ui
        seekbarState = (SeekBar) findViewById(R.id.seekbarState);

        seekbarState.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, maritalStatusList);
        ((MaterialAutoCompleteTextView) maritalStatus.getEditText()).setAdapter(adp);

        editTexts = new EditText[]{parentFirstName, parentLastName, parentID, parentBirthCountry, parentProfession, parentEducationYears,
                parentWorkPlace, parentPhone, parentEmail};

        Intent gi = getIntent();

        // which activity we are now (dad/mom)
        isDadActivity = gi.getBooleanExtra("dad", true) ? "dad" : "mom";

        ids.put(R.id.parentFirstName, isDadActivity + "FirstName");
        ids.put(R.id.parentLastName, isDadActivity + "LastName");
        ids.put(R.id.parentID, isDadActivity + "ID");
        ids.put(R.id.parentBirthCountry, isDadActivity + "BirthCountry");
        ids.put(R.id.parentProfession, isDadActivity + "Profession");
        ids.put(R.id.parentEducationYears, isDadActivity + "EducationYears");
        ids.put(R.id.parentWorkPlace, isDadActivity + "WorkPlace");
        ids.put(R.id.parentPhone, isDadActivity + "Phone");
        ids.put(R.id.parentEmail, isDadActivity + "Email");

        // all the numeric fields accepts just numbers
        parentID.setTransformationMethod(null);
        parentEducationYears.setTransformationMethod(null);
        parentPhone.setTransformationMethod(null);

        // get the current data from the xml file
        ArrayList<String> wantedXmlFields = new ArrayList<>(ids.values());
        wantedXmlFields.add(isDadActivity + "MaritalStatus");
        wantedXmlFields.add(isDadActivity + "BirthDate");
        data = XmlHelper.getData(wantedXmlFields);
        initUI();
        initDatePicker(parentBirthDate, getSupportFragmentManager());
    }

    private void initUI()
    {
        // put all EditTexts data
        for (EditText editText : editTexts) {
            editText.setText(data.get(ids.get(editText.getId())));
        }

        maritalStatus.getEditText().setText(data.get(isDadActivity + "MaritalStatus"));
        parentBirthDate.setText(data.get(isDadActivity + "BirthDate"));

        if (isDadActivity.equals("mom"))
        {
            parentType.setText("פרטי אם");
            seekbarState.setProgress(3);
        }
    }

    public void saveData(View view) {
        // if all fields are ok
        if (checkFields()) {
            String typedText = "";

            // save all the editTexts
            for (EditText editText : editTexts) {
                if (editText.getText() == null)
                    typedText = "";
                else
                    typedText = editText.getText().toString();

                data.put(ids.get(editText.getId()), typedText);
            }

            // save maritalStatus and BirthDate
            data.put(isDadActivity + "MaritalStatus", maritalStatus.getEditText().getText().toString());
            data.put(isDadActivity + "BirthDate", parentBirthDate.getText().toString());

            XmlHelper.pushData(data);

            // if want to move page
            // this view has parameter of tag - so we know to move page
            if (view.getTag().equals("move"))
            {
                // move to mom activity
                if (isDadActivity.equals("dad")) {
                    Intent si = new Intent(FormParentsActivity.this, FormParentsActivity.class);
                    si.putExtra("dad", false); // false = mom
                    startActivity(si);
                }
                else
                {
                    Intent si = new Intent(FormParentsActivity.this, FormFilesActivity.class);
                    startActivity(si);
                }
            }
        }
    }

    private boolean checkFields()
    {
        boolean isGood = false;

        // check non empty fields and that fields are in hebrew
        // and check that the user entered birthdate
        if ((!isEmpty(parentFirstName, null) && !isEmpty(parentLastName, null) && !isEmpty(parentID, null)
                && !isEmpty(parentBirthCountry, null) && !isEmpty(parentProfession, null)
                && !isEmpty(parentEducationYears, null) && !isEmpty(parentPhone, null) && !isEmpty(parentEmail, null)
                && !isEmpty(maritalStatus.getEditText(), maritalStatus))
                && (isHebrew(parentFirstName) && isHebrew(parentLastName) && isHebrew(parentBirthCountry)
                && isHebrew(parentProfession)) && (!parentBirthDate.getText().toString().equals("")))
        {
            // check mail format
            if (!(EmailValidator.getInstance().isValid(parentEmail.getText().toString())))
            {
                parentEmail.setError("כתובת מייל לא תקינה!");
            }
            else
            {
                isGood = true;
            }

        }

        return isGood;
    }

    public void back(View view) {
        finish();
    }
}