package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.validator.routines.EmailValidator;

public class FormActivity extends AppCompatActivity {
    int progress = 0;
    TextView id;
    SeekBar seekbarState;

    String studentFormPath;

    EditText firstName, lastName, city,
    street, addressNumber, homeNumber,
    neighborhood, zipCode, studentPhone, homePhone,
    birthDate, birthDateHebrew, studentMail, birthCountry, aliyaDate, tnuatNoar, comments;

    TextInputLayout wantedClass, currentSchool, kupatHolim, maslul;

    HashMap<String, String> data;

    EditText[] editTexts;

    HashMap<Integer, String> ids = new HashMap<>();

    TextInputLayout[] spinners;

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
        birthDate = (EditText) findViewById(R.id.birthDate);
        birthDateHebrew = (EditText) findViewById(R.id.birthDateHebrew);
        currentSchool = (TextInputLayout) findViewById(R.id.currentSchool);
        studentMail = (EditText) findViewById(R.id.studentMail);
        kupatHolim = (TextInputLayout) findViewById(R.id.kupatHolim);
        birthCountry = (EditText) findViewById(R.id.birthCountry);
        aliyaDate = (EditText) findViewById(R.id.aliyaDate);
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
                birthDate, birthDateHebrew, studentMail, birthCountry, aliyaDate, tnuatNoar, comments};

        spinners = new TextInputLayout[]{wantedClass, currentSchool, kupatHolim, maslul};

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

        studentFormPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/student_" + gi.getStringExtra("id") + ".xml";


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

        get_xml(gi.getStringExtra("id"));
    }

    public void get_xml(String studentId)
    {
        if (new File(studentFormPath).exists()) {
            XmlHelper.init(studentFormPath);
            data = XmlHelper.getData();
            initUI();
        }
        else
        {
            // todo: להוריד את ההערה למטה
            //String tempPath = downloadTemplateXML();
            //XmlHelper.init(tempPath);

            XmlHelper.init(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/student_tempXML.xml");
            data = XmlHelper.getData();
        }

        // צריך למצוא דרך לעשות תרדים
        // כי אם אני מתחיל מחדש את האקטיביטי אז זה קורס כי זה גם עושה איניט וגם מוריד את הקובץ מהענן. זה צריך קודם לסיים להוריד את הקובץ מהענן...

        //System.out.println(data.toString());

        /*
        // Instantiate the Factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            //dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(path));

            // optional, but recommended
            // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();
            Node node = root.getElementsByTagName("firstname").item(0);
            node.setTextContent("hi45");

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            DOMSource source = new DOMSource(doc);

            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/(id2)XML.xml");
            transformer.transform(source, new StreamResult(outputFile));


            // ----------------------(WORKING) GET ALL THE TEXT IN THE XML --------------------
//            NodeList questions = root.getChildNodes();
//            for (int temp = 0; temp < questions.getLength(); temp++)
//            {
//                Node node = questions.item(temp);
//                System.out.println("");    //Just a separator
//                if (node.getNodeType() == Node.ELEMENT_NODE)
//                {
//                    //Print each question's detail
//                    Element eElement = (Element) node;
//
//                    String n = "";
//                    if (eElement.hasChildNodes())
//                    {
//                        n = eElement.getChildNodes().item(0).getTextContent();
//                    }
//                    System.out.println("Employee id : " + n);
//                }
//            }

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private String downloadTemplateXML()
    {
        // Create a reference with an initial file path and name
        StorageReference pathReference = FBref.storageRef.child("forms/template.xml");

        File localFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/student_tempXML.xml");
        try {
            localFile.createNewFile();
            localFile.setReadable(true);
            localFile.setWritable(true);

            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    //Toast.makeText(FormActivity.this, "DONE", Toast.LENGTH_SHORT).show();
                    data = XmlHelper.getData();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(FormActivity.this, "NO", Toast.LENGTH_SHORT).show();
                    // Handle any errors
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return localFile.getPath();
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

    public void saveData(View view) {
        String typedText = "";

        if (checkFields()) {
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
                if (spinner.getEditText().getText() == null)
                    typedText = "";
                else
                    typedText = spinner.getEditText().getText().toString();

                data.put(ids.get(spinner.getId()), typedText);
            }

            XmlHelper.pushData(data, studentFormPath);
        }
    }

    private boolean checkFields()
    {
        boolean isGood = false;

        // check non empty fields and that fields are in hebrew
        if ((!isEmpty(firstName, null) && !isEmpty(lastName, null) && !isEmpty(city, null)
                && !isEmpty(street, null) && !isEmpty(addressNumber, null)
                && !isEmpty(zipCode, null) && !isEmpty(wantedClass.getEditText(), wantedClass)
                && !isEmpty(currentSchool.getEditText(), currentSchool) && !isEmpty(kupatHolim.getEditText(), kupatHolim)
                && !isEmpty(maslul.getEditText(), maslul))
            && (isHebrew(firstName) && isHebrew(lastName) && isHebrew(city) && isHebrew(street)
                && isHebrew(neighborhood) && isHebrew(birthDateHebrew) && isHebrew(birthCountry)
                && isHebrew(tnuatNoar) && isHebrew(comments)))
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
}