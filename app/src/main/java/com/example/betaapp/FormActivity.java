package com.example.betaapp;

import static com.example.betaapp.Helper.initDatePicker;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.validator.routines.EmailValidator;

/**
 * The type first Form activity.
 * @author Itey Weintraub <av5350@bs.amalnet.k12.il>
 * @version	1
 * short description:
 *
 *      This activity is to get student's information (name, id, mail...)
 */
public class FormActivity extends AppCompatActivity {
    SeekBar seekbarState;

    String studentFormPath;

    AlertDialog.Builder adb;

    EditText firstName, lastName, city, street, addressNumber, homeNumber, neighborhood, zipCode, studentPhone,
            homePhone, birthDateHebrew, studentMail, birthCountry, tnuatNoar, comments;

    TextInputLayout wantedClass, currentSchool, kupatHolim, maslul;

    HashMap<String, String> data;

    EditText[] editTexts;

    HashMap<Integer, String> ids = new HashMap<>();

    TextInputLayout[] spinners;

    TextView birthDate, aliyaDate, id;

    TextView[] textViews;

    String[] kupatHolimList = new String[]{"מכבי", "מאוחדת", "כללית", "לאומית"};

    String[] currentSchoolList = new String[]{"בית ספר 1", "בית ספר 2", "בית ספר 3", "בית ספר 4"};

    String[] wantedClassList = new String[]{"ז", "ח", "ט", "י", "יא", "יב"};

    String[] maslulList = new String[]{"מסלול 1", "מסלול 2", "מסלול 3", "מסלול 4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        id = (TextView) findViewById(R.id.id);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        city = (EditText) findViewById(R.id.city);
        street = (EditText) findViewById(R.id.street);
        addressNumber = (EditText) findViewById(R.id.addressNumber);
        homeNumber = (EditText) findViewById(R.id.homeNumber);
        neighborhood = (EditText) findViewById(R.id.neighborhood);
        zipCode = (EditText) findViewById(R.id.zipCode);
        studentPhone = (EditText) findViewById(R.id.studentPhone);
        homePhone = (EditText) findViewById(R.id.homePhone);
        birthDate = (TextView) findViewById(R.id.birthDate);
        birthDateHebrew = (EditText) findViewById(R.id.birthDateHebrew);
        currentSchool = (TextInputLayout) findViewById(R.id.currentSchool);
        studentMail = (EditText) findViewById(R.id.studentMail);
        kupatHolim = (TextInputLayout) findViewById(R.id.kupatHolim);
        birthCountry = (EditText) findViewById(R.id.birthCountry);
        aliyaDate = (TextView) findViewById(R.id.aliyaDate);
        tnuatNoar = (EditText) findViewById(R.id.tnuatNoar);
        maslul = (TextInputLayout) findViewById(R.id.maslul);
        comments = (EditText) findViewById(R.id.comments);

        wantedClass = (TextInputLayout) findViewById(R.id.wantedClass);
        seekbarState = (SeekBar) findViewById(R.id.seekbarState);

        ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, wantedClassList);
        ((MaterialAutoCompleteTextView) wantedClass.getEditText()).setAdapter(adp);

        adp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, currentSchoolList);
        ((MaterialAutoCompleteTextView) currentSchool.getEditText()).setAdapter(adp);

        adp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, kupatHolimList);
        ((MaterialAutoCompleteTextView) kupatHolim.getEditText()).setAdapter(adp);

        adp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, maslulList);
        ((MaterialAutoCompleteTextView) maslul.getEditText()).setAdapter(adp);

        editTexts = new EditText[]{firstName, lastName, city,
                street, addressNumber, homeNumber,
                neighborhood, zipCode, studentPhone, homePhone,
                birthDateHebrew, studentMail, birthCountry, tnuatNoar, comments};

        spinners = new TextInputLayout[]{wantedClass, currentSchool, kupatHolim, maslul};

        textViews = new TextView[]{birthDate, aliyaDate};

        // make a connection between the element in the screen and its destination in the xml file.
        ids.put(R.id.firstName, "firstName");
        ids.put(R.id.lastName, "lastName");
        ids.put(R.id.wantedClass, "wantedClass");
        ids.put(R.id.city, "city");
        ids.put(R.id.street, "street");
        ids.put(R.id.addressNumber, "addressNumber");
        ids.put(R.id.homeNumber, "homeNumber");
        ids.put(R.id.neighborhood, "neighborhood");
        ids.put(R.id.zipCode, "zipCode");
        ids.put(R.id.studentPhone, "studentPhone");
        ids.put(R.id.homePhone, "homePhone");
        ids.put(R.id.birthDate, "birthDate");
        ids.put(R.id.birthDateHebrew, "birthDateHebrew");
        ids.put(R.id.currentSchool, "currentSchool");
        ids.put(R.id.studentMail, "studentMail");
        ids.put(R.id.kupatHolim, "kupatHolim");
        ids.put(R.id.birthCountry, "birthCountry");
        ids.put(R.id.aliyaDate, "aliyaDate");
        ids.put(R.id.tnuatNoar, "tnuatNoar");
        ids.put(R.id.maslul, "maslul");
        ids.put(R.id.comments, "comments");

        Intent gi = getIntent();

        id.setText(gi.getStringExtra("id"));
        Helper.currentStudentId = gi.getStringExtra("id");

        studentFormPath = getApplicationContext().getCacheDir().getAbsolutePath() + "/" + gi.getStringExtra("id") + ".xml";
        Helper.studentFormDestPath = studentFormPath;

        // make the seekbar untouchable (cant change the current position with it)
        seekbarState.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        // make all the numeric fields accepts just numbers
        addressNumber.setTransformationMethod(null);
        homeNumber.setTransformationMethod(null);
        zipCode.setTransformationMethod(null);
        homePhone.setTransformationMethod(null);
        studentPhone.setTransformationMethod(null);

        initDatePicker(birthDate, getSupportFragmentManager());
        initDatePicker(aliyaDate, getSupportFragmentManager());
        getLocalXml(gi.getStringExtra("id"));
    }

    /**
     * Gets local student's xml form file we downloaded before (or the temp xml if its not exists)
     *
     * @param studentID the student id (in the server we store the student's finish year under his id)
     */
    private void getLocalXml(String studentID)
    {
        if (new File(studentFormPath).exists()) {
            XmlHelper.init(studentFormPath, true);
            data = XmlHelper.getData(new ArrayList<String>(ids.values()));
            initUI();

            // get the finish year of the student (we save the images of the student under the finish year)
            FBref.refStudents.child(studentID).child("finishYear").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dS) {
                    Helper.studentFinishYear = dS.getValue(Integer.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        else
        {
            // init the XmlHelper with the temp xml path we downloaded
            String tempPath = getApplicationContext().getCacheDir().getAbsolutePath() + "/student_tempXML.xml";
            XmlHelper.init(tempPath, true);
            data = XmlHelper.getData(new ArrayList<String>(ids.values()));
        }
    }

    /**
     * Init the screen fields with the student's last saved info
     */
    private void initUI()
    {
        // put all the edittexts data
        for (EditText editText : editTexts) {
            editText.setText(data.get(ids.get(editText.getId())));
        }

        // put all the spinners data
        for (TextInputLayout spinner : spinners) {
            ((MaterialAutoCompleteTextView) spinner.getEditText()).setText(data.get(ids.get(spinner.getId())), false);
        }

        // put all the text views data
        for (TextView textView : textViews) {
            textView.setText(data.get(ids.get(textView.getId())));
        }

        // the user cannot change its birth date after saving first part of form
        birthDate.setClickable(false);
    }

    /**
     * Save data to the xml file and move to the next activity if the next button was clicked.
     *
     * @param view the view
     */
    public void saveData(View view)
    {
        // if all fields are ok
        if (checkFields()) {
            // create confirm alert dialog
            adb = new AlertDialog.Builder(this);
            adb.setTitle("אחרי האישור לא תוכל לערוך את השדה הבא:");

            adb.setMessage("*. תאריך לידה");

            adb.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String typedText = "";

                    // save all the editTexts (not all the fields are must to complete)
                    for (EditText editText : editTexts) {
                        if (editText.getText() == null)
                            typedText = "";
                        else
                            typedText = editText.getText().toString();

                        data.put(ids.get(editText.getId()), typedText);
                    }

                    // save all the spinners (TextInputLayout)
                    for (TextInputLayout spinner : spinners) {
                        typedText = spinner.getEditText().getText().toString();
                        data.put(ids.get(spinner.getId()), typedText);
                    }

                    for (TextView textView : textViews) {
                        typedText = textView.getText().toString();
                        data.put(ids.get(textView.getId()), typedText);
                    }

                    // if the studentFinishYear variable wasnt initialized yet (its the first time)
                    if (Helper.studentFinishYear == 0)
                        Helper.studentFinishYear = getEndYear();

                    // cannot edit the birthdate anymore
                    birthDate.setClickable(false);

                    XmlHelper.pushData(data);

                    // save student's first and last name
                    FBref.refStudents.child(Helper.currentStudentId).child("firstName").setValue(firstName.getText().toString());
                    FBref.refStudents.child(Helper.currentStudentId).child("lastName").setValue(lastName.getText().toString());

                    // if want to move page (if click on the next button)
                    // this view has parameter of tag - so we know to move page
                    if (view.getTag().equals("move"))
                    {
                        Intent si = new Intent(FormActivity.this, FormStudConfirmActivity.class);
                        startActivity(si);
                    }
                }
            });

            adb.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            adb.show();
        }
    }

    /**
     * Get the student finish year (by the class he want to go next year)
     *
     * @return the finish year
     */
    private int getEndYear()
    {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String wantClass = wantedClass.getEditText().getText().toString();

        // if the student wants to go the the 7th grade - he would be 6 years at this school
        return ((6 - Arrays.asList(wantedClassList).indexOf(wantClass)) + currentYear);
    }

    /**
     * Check if all the fields data are good (for example, some of them must be in hebrew, and some must not be empty)
     *
     * @return true if all the fields in this activity are ok (false if they are not)
     */
    private boolean checkFields()
    {
        boolean isGood = false;

        // check non empty fields and that fields are in hebrew
        // and check that the user entered birthdate
        if ((!isEmpty(firstName, null) && !isEmpty(lastName, null) && !isEmpty(city, null)
                && !isEmpty(street, null) && !isEmpty(addressNumber, null)
                && !isEmpty(zipCode, null) && !isEmpty(birthCountry, null) && !isEmpty(wantedClass.getEditText(), wantedClass)
                && !isEmpty(currentSchool.getEditText(), currentSchool) && !isEmpty(kupatHolim.getEditText(), kupatHolim)
                && !isEmpty(maslul.getEditText(), maslul))
            && (isHebrew(firstName) && isHebrew(lastName) && isHebrew(city) && isHebrew(street)
                && isHebrew(neighborhood) && isHebrew(birthDateHebrew) && isHebrew(birthCountry)
                && isHebrew(tnuatNoar) && isHebrew(comments)) && (!birthDate.getText().toString().equals("")))
        {
            // check mail format ("" is acceptable)
            if (!(studentMail.getText().toString().isEmpty() || EmailValidator.getInstance().isValid(studentMail.getText().toString())))
            {
                studentMail.setError("כתובת מייל לא תקינה!");
            }
            else
            {
                isGood = true;
            }

        }

        return isGood;
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
            Intent si = new Intent(FormActivity.this, CreditsActivity.class);
            startActivity(si);
        }

        return true;
    }

    public void back(View view) {
        finish();
    }
}