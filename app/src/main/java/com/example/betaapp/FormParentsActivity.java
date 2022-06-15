package com.example.betaapp;

import static com.example.betaapp.Helper.isEmpty;
import static com.example.betaapp.Helper.isHebrew;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

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
    boolean showActivityOnce; // if the child have 1 parent - show activity once

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

        parentType = (TextView) findViewById(R.id.parentType); // just for ui (if screen is form mom/dad now)
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

        ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, maritalStatusList);
        ((MaterialAutoCompleteTextView) maritalStatus.getEditText()).setAdapter(adp);

        editTexts = new EditText[]{parentFirstName, parentLastName, parentID, parentBirthCountry, parentProfession, parentEducationYears,
                parentWorkPlace, parentPhone, parentEmail};

        Intent gi = getIntent();

        // which activity we are now (dad/mom)
        isDadActivity = gi.getBooleanExtra("dad", true) ? "dad" : "mom";
        showActivityOnce = gi.getBooleanExtra("showActivityOnce", false);

        // all the numeric fields accepts just numbers
        parentID.setTransformationMethod(null);
        parentEducationYears.setTransformationMethod(null);
        parentPhone.setTransformationMethod(null);

        ids.put(R.id.parentFirstName, isDadActivity + "FirstName");
        ids.put(R.id.parentLastName, isDadActivity + "LastName");
        ids.put(R.id.parentID, isDadActivity + "ID");
        ids.put(R.id.parentBirthCountry, isDadActivity + "BirthCountry");
        ids.put(R.id.parentProfession, isDadActivity + "Profession");
        ids.put(R.id.parentEducationYears, isDadActivity + "EducationYears");
        ids.put(R.id.parentWorkPlace, isDadActivity + "WorkPlace");
        ids.put(R.id.parentPhone, isDadActivity + "Phone");
        ids.put(R.id.parentEmail, isDadActivity + "Email");

        getXmlData();
        initUI();
        Helper.initDatePicker(parentBirthDate, getSupportFragmentManager());
    }

    /**
     * This function gets the updated screen elements fields data
     * from the student's xml form
     */
    private void getXmlData()
    {
        // get the current data from the xml file
        ArrayList<String> wantedXmlFields = new ArrayList<>(ids.values());
        wantedXmlFields.add(isDadActivity + "MaritalStatus");
        wantedXmlFields.add(isDadActivity + "BirthDate");
        data = XmlHelper.getData(wantedXmlFields);
    }

    /**
     * Init the screen fields with the student's last saved info
     * Or from the logged user info (name, phone, mail)
     * And change the title of te activity for the mom or dad
     */
    private void initUI()
    {
        // get all EditTexts data from the xml form
        for (EditText editText : editTexts) {
            editText.setText(data.get(ids.get(editText.getId())));
        }

        ((MaterialAutoCompleteTextView) maritalStatus.getEditText()).setText(data.get(isDadActivity + "MaritalStatus"), false);
        parentBirthDate.setText(data.get(isDadActivity + "BirthDate"));

        // change the name of the activity (for mom/dad) and change the seekbar state
        if (isDadActivity.equals("mom"))
        {
            parentType.setText("פרטי אם");
            seekbarState.setProgress(3);
        }

        // get logged in user data from firebase
        FBref.refUsers.child(FBref.auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // is the user who fill the form is the dad or mom of the student
                boolean isUserDad = snapshot.child("isDad").getValue(Boolean.class);

                // if its the relevant activity for the user (mom/dad)
                if (isDadActivity.equals("dad") == isUserDad)
                {
                    // if its the first time entering the activity
                    if (parentFirstName.getText().toString() == null) {
                        parentFirstName.setText(snapshot.child("firstName").getValue(String.class));
                        parentLastName.setText(snapshot.child("lastName").getValue(String.class));

                        // get the logged in user's data (phone + mail)
                        FirebaseUser user = FBref.auth.getCurrentUser();
                        parentPhone.setText(user.getPhoneNumber());
                        parentEmail.setText(user.getEmail());
                    }

                    // for the logged in parent - avoid from changing this data (name, phone, mail)
                    parentFirstName.setKeyListener(null);
                    parentLastName.setKeyListener(null);
                    parentPhone.setKeyListener(null);
                    parentEmail.setKeyListener(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /**
     * Save elements fields to the student's xml form.
     * Or also move activity if the next button was clicked
     *
     * @param view the view
     */
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
                if (showActivityOnce || isDadActivity.equals("mom"))
                {
                    Intent si = new Intent(FormParentsActivity.this, FormFilesActivity.class);
                    startActivity(si);
                }
                else // move to mom activity
                {
                    Intent si = new Intent(FormParentsActivity.this, FormParentsActivity.class);
                    si.putExtra("dad", false); // false = mom
                    startActivity(si);
                }
            }
        }
    }

    /**
     * Check if all the fields data are good
     * (for example,some of them must be in hebrew, and some must not be empty)
     *
     * @return true if all the fields in this activity are ok (false if they are not)
     */
    private boolean checkFields()
    {
        boolean isGood = false;

        // check non empty fields and that fields are in hebrew
        // and check that the user entered birthdate
        if ((!isEmpty(parentFirstName, null) && !isEmpty(parentLastName, null) && (parentID.getText().toString().length() == 9) && (Helper.checkID(parentID.getText().toString()))
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

    /**
     * Go back to the last screen (confirm activity or to the dad activity)
     *
     * @param view the view
     */
    public void back(View view) {
        finish();
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
            Intent si = new Intent(FormParentsActivity.this, CreditsActivity.class);
            startActivity(si);
        }

        return true;
    }
}