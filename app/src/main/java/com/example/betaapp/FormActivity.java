package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.validator.routines.EmailValidator;

public class FormActivity extends AppCompatActivity {
    SeekBar seekbarState;

    String studentFormPath;

    AlertDialog.Builder adb;

    EditText firstName, lastName, city,
    street, addressNumber, homeNumber,
    neighborhood, zipCode, studentPhone, homePhone, birthDateHebrew, studentMail, birthCountry, tnuatNoar, comments;

    TextInputLayout wantedClass, currentSchool, kupatHolim, maslul;

    HashMap<String, String> data;
    EditText[] editTexts;

    HashMap<Integer, String> ids = new HashMap<>();
    TextInputLayout[] spinners;

    TextView birthDate, aliyaDate, id;
    TextView[] textViews;


    String[] kupatHolimList = new String[]{"מכבי", "מאוחדת", "כללית", "לאומית"};
    String[] currentSchoolList = new String[]{"בית ספר 1", "בית ספר 2", "בית ספר 3", "בית ספר 4"};
    String[] wantedClassList = new String[]{"ז", "ח", "ט", "י"};
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

        studentFormPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + gi.getStringExtra("id") + ".xml";
        Helper.studentFormDestPath = studentFormPath;

        seekbarState = (SeekBar) findViewById(R.id.seekbarState);

        seekbarState.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        // all the numeric fields accepts just numbers
        addressNumber.setTransformationMethod(null);
        homeNumber.setTransformationMethod(null);
        zipCode.setTransformationMethod(null);
        homePhone.setTransformationMethod(null);
        studentPhone.setTransformationMethod(null);


        if (ContextCompat.checkSelfPermission(FormActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(FormActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }

        ActivityCompat.requestPermissions( this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 1
        );

        initDatePicker(birthDate);
        initDatePicker(aliyaDate);
        get_xml(gi.getStringExtra("id"));
    }

    public void get_xml(String studentID)
    {
        if (new File(studentFormPath).exists()) {
            XmlHelper.init(studentFormPath, true);
            data = XmlHelper.getData(new ArrayList<String>(ids.values()));
            initUI();

            // get the finishYear of the student
            FBref.refStudents.child(studentID).child("finishYear").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dS) {
                    Helper.studentFinishYear = dS.getValue(String.class);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
        else
        {
            // init the XmlHelper with the temp xml path we downloaded
            String tempPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/student_tempXML.xml";
            XmlHelper.init(tempPath, true);
            data = XmlHelper.getData(new ArrayList<String>(ids.values()));
        }
    }

    private void initUI()
    {
        for (int index = 0; index < editTexts.length; index++)
        {
            editTexts[index].setText(data.get(ids.get(editTexts[index].getId())));
        }

        // put all the spinners data
        for (int index = 0; index < spinners.length; index++)
        {
            ((MaterialAutoCompleteTextView) spinners[index].getEditText()).setText(data.get(ids.get(spinners[index].getId())), false);
        }

        // put all the text views data
        for (int index = 0; index < textViews.length; index++)
        {
            textViews[index].setText(data.get(ids.get(textViews[index].getId())));
        }

        // the user cannot change its birth date after saving first part of form
        birthDate.setClickable(false);
    }

    public void saveData(View view) {

        // if all fields are ok
        if (checkFields()) {
            // create confirm alert dialog
            adb = new AlertDialog.Builder(this);
            adb.setTitle("אחרי האישור לא תוכל לערוך את השדות הבאים:");

            adb.setMessage("*. תעודת זהות\n*. תאריך לידה");

            adb.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String typedText = "";

                    // save all the editTexts
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

                    // if the variable wasnt initialized yet (first time)
                    if (Helper.studentFinishYear.equals(""))
                        Helper.studentFinishYear = getEndYear();

                    // cannot edit the birthdate anymore
                    birthDate.setClickable(false);

                    XmlHelper.pushData(data);

                    // if want to move page
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

    private String getEndYear()
    {
        int year = Integer.parseInt(birthDate.getText().toString().split("-")[0]);

        return String.valueOf(year + 18);
    }

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

    private boolean isEmpty(EditText et, TextInputLayout layout)
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

    private boolean isHebrew(EditText et)
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

    public void initDatePicker(TextView textView) {
        MaterialDatePicker.Builder dateBuilder = MaterialDatePicker.Builder.datePicker();

        dateBuilder.setTitleText("SELECT A DATE");

        // create the instance of the material date
        final MaterialDatePicker materialDatePicker = dateBuilder.build();

        textView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
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
                        Toast.makeText(FormActivity.this, getEndYear(), Toast.LENGTH_SHORT).show();
                    }
                });

    }
}