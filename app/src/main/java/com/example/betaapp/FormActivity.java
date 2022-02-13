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
import android.os.Bundle;
import android.os.Environment;
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
import java.util.stream.Stream;

public class FormActivity extends AppCompatActivity {
    int progress = 0;
    TextView id;
    SeekBar seekbarState;

    String studentFormPath;

    EditText firstName, lastName, city,
    street, addressNumber, homeNumber,
    neighborhood, zipCode, studentPhone, homePhone,
    birthDate, birthDateHebrew, studentMail, birthCountry, aliyaDate, tnuatNoar, comments;

    TextInputLayout wantedClass;
    Spinner currentSchool, kupatHolim, maslul;

    HashMap<String, String> data;

    EditText[] editTexts;

    HashMap<Integer, String> editTextsID = new HashMap<>();

    Spinner[] spinners;

    String[] kupatHolimList = new String[]{"מכבי", "מאוחדת", "כללית", "לאומית"};
    String[] currentSchoolList = new String[]{"בית ספר 1", "בית ספר 2", "בית ספר 3", "בית ספר 4"};
    String[] wantedClassList = new String[]{"ז", "ח", "ט", "י"};
    String[] maslulClassList = new String[]{"מסלול 1", "מסלול 2", "מסלול 3", "מסלול 4"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);

        id = (TextView) findViewById(R.id.id);

        firstName = (EditText) findViewById(R.id.firstName);
        lastName = (EditText) findViewById(R.id.lastName);
        //wantedClass = (Spinner) findViewById(R.id.wantedClass);
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
        currentSchool = (Spinner) findViewById(R.id.currentSchool);
        studentMail = (EditText) findViewById(R.id.studentMail);
        kupatHolim = (Spinner) findViewById(R.id.kupatHolim);
        birthCountry = (EditText) findViewById(R.id.birthCountry);
        aliyaDate = (EditText) findViewById(R.id.aliyaDate);
        tnuatNoar = (EditText) findViewById(R.id.tnuatNoar);
        maslul = (Spinner) findViewById(R.id.maslul);
        comments = (EditText) findViewById(R.id.comments);


        wantedClass = (TextInputLayout) findViewById(R.id.wantedClass);

        ArrayAdapter<String> adp = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, wantedClassList);
        ((MaterialAutoCompleteTextView) wantedClass.getEditText()).setAdapter(adp);

        editTexts = new EditText[]{firstName, lastName, city,
                street, addressNumber, homeNumber,
                neighborhood, zipCode, studentPhone, homePhone,
                birthDate, birthDateHebrew, studentMail, birthCountry, aliyaDate, tnuatNoar, comments};

        editTextsID.put(R.id.firstName, "firstName");
        editTextsID.put(R.id.lastName, "lastName");
        editTextsID.put(R.id.wantedClass, "wantedClass");
        editTextsID.put(R.id.city, "city");
        editTextsID.put(R.id.street, "street");
        editTextsID.put(R.id.addressNumber, "addressNumber");
        editTextsID.put(R.id.homeNumber, "homeNumber");
        editTextsID.put(R.id.neighborhood, "neighborhood");
        editTextsID.put(R.id.zipCode, "zipCode");
        editTextsID.put(R.id.studentPhone, "studentPhone");
        editTextsID.put(R.id.homePhone, "homePhone");
        editTextsID.put(R.id.birthDate, "birthDate");
        editTextsID.put(R.id.birthDateHebrew, "birthDateHebrew");
        editTextsID.put(R.id.currentSchool, "currentSchool");
        editTextsID.put(R.id.studentMail, "studentMail");
        editTextsID.put(R.id.kupatHolim, "kupatHolim");
        editTextsID.put(R.id.birthCountry, "birthCountry");
        editTextsID.put(R.id.aliyaDate, "aliyaDate");
        editTextsID.put(R.id.tnuatNoar, "tnuatNoar");
        editTextsID.put(R.id.maslul, "maslul");
        editTextsID.put(R.id.comments, "comments");

        //spinners = new Spinner[]{currentSchool, wantedClass, kupatHolim, maslul};

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
            editTexts[index].setText(data.get(editTextsID.get(editTexts[index].getId())));
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

        // save all the editTexts
        for (int index = 0; index < editTexts.length; index++)
        {
            if (editTexts[index].getText() == null)
                typedText = "";
            else
                typedText = editTexts[index].getText().toString();

            data.put(editTextsID.get(editTexts[index].getId()), typedText);
        }

        // todo: צריך לעבוד על זה....
        data.put("currentSchool", "");
        data.put("wantedClass", "");
        data.put("kupatHolim", "");
        data.put("maslul", "");

        XmlHelper.pushData(data, studentFormPath);
    }
}