package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StudentInfoActivity extends AppCompatActivity {
    TextView studentFirstName, studentLastName, studentID, studentCity, studentSchool, studentGrade,
            studentMaslul, phoneParent, emailParent, typeParent, parentName;
    String parentPhone, parentEmail;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);

        /*

        בform המתאים של האבא או אמא לחסום עריכה של הפרטים שם שם משפחה... טלפון... אחרי שעשינו להם SAVE פעם ראשונה
        זאת אומרת מתי שיש שם כבר נתונים אחנו יודעים שהיה עריכה- אז לחסום שלא ישנו את זה יותר


        ****************************
        מה קורה אם אני רוצה להוסיף פעמיים את הילד שלי (את התעודת זהות שלו פעמיים)
         */

        studentFirstName = (TextView) findViewById(R.id.studentFirstName);
        studentLastName = (TextView) findViewById(R.id.studentLastName);
        studentID = (TextView) findViewById(R.id.studentID);
        studentCity = (TextView) findViewById(R.id.studentCity);
        studentSchool = (TextView) findViewById(R.id.studentSchool);
        studentGrade = (TextView) findViewById(R.id.studentGrade);
        studentMaslul = (TextView) findViewById(R.id.studentMaslul);
        phoneParent = (TextView) findViewById(R.id.phoneParent);
        emailParent = (TextView) findViewById(R.id.emailParent);
        typeParent = (TextView) findViewById(R.id.typeParent);
        parentName = (TextView) findViewById(R.id.parentName);

        Intent gi = getIntent();
        String localFormPath = getApplicationContext().getCacheDir().getAbsolutePath() + "/" + gi.getStringExtra("studentID") + ".xml";
        XmlHelper.init(localFormPath, false);

        studentID.setText("תעודת זהות: " + gi.getStringExtra("studentID"));

        // get all relevant data from the xml
        HashMap<String, String> fields = getFieldsData();
        studentFirstName.setText("שם פרטי: " + fields.get("firstName"));
        studentLastName.setText("שם משפחה: " + fields.get("lastName"));
        studentCity.setText("יישוב: " + fields.get("city"));
        studentSchool.setText("בית ספר: " + fields.get("currentSchool"));
        studentGrade.setText("כיתה: " + fields.get("wantedClass"));
        studentMaslul.setText("מסלול: " + fields.get("maslul"));

        // get logged user info (its mail + phone)
        FBref.refStudents.child(gi.getStringExtra("studentID")).child("parentUID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String parentUID = snapshot.getValue(String.class);

                FBref.refUsers.child(parentUID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // get the relevant(the authenticated) user details
                        String isParentDad = snapshot.child("isDad").getValue(Boolean.class) ? "dad" : "mom";

                        typeParent.setText("ממלא: " + (snapshot.child("isDad").getValue(Boolean.class) ? "אבא" : "אמא"));
                        parentName.setText("שם הורה: " + snapshot.child("firstName").getValue(String.class) + " " + snapshot.child("lastName").getValue(String.class));

                        ArrayList<String> wantedTags = new ArrayList<>();
                        wantedTags.add(isParentDad + "Phone");
                        wantedTags.add(isParentDad + "Email");

                        HashMap<String, String> parentFields = XmlHelper.getData(wantedTags);

                        parentPhone = parentFields.get(isParentDad + "Phone");
                        parentEmail = parentFields.get(isParentDad + "Email");

                        phoneParent.setText("מספר טלפון: " + parentPhone);
                        emailParent.setText("כתובת מייל: " + parentEmail);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private HashMap<String, String> getFieldsData()
    {
        ArrayList<String> wantedTags = new ArrayList<>();
        wantedTags.add("firstName");
        wantedTags.add("lastName");
        wantedTags.add("city");
        wantedTags.add("currentSchool");
        wantedTags.add("wantedClass");
        wantedTags.add("maslul");

        return XmlHelper.getData(wantedTags);
    }

    public void openWhatsapp(View view) {
        // send the user to whatsapp view (app)
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wa.me/" + parentPhone));
        startActivity(intent);
    }

    public void openEmail(View view) {
        Uri uri = Uri.parse("mailto:").buildUpon()
                .appendQueryParameter("to", parentEmail)
                .build();

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
        startActivity(Intent.createChooser(emailIntent, "שלח באמצעות..."));
    }

    public void createPDF(View view)
    {
        StorageReference pathReference;
        File localFile;

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("מוריד קובץ עזר");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setProgress(0);
        progressDialog.show();

        pathReference = FBref.storageRef.child("form_pdf.pdf");
        localFile = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/form_pdf.pdf");

        try {
            localFile.createNewFile();
            localFile.setReadable(true);
            localFile.setWritable(true);

            pathReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    editPDF();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    localFile.delete();
                    Toast.makeText(StudentInfoActivity.this, "An error occurred. Try again!", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>(){
                @Override
                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot taskSnapshot) {
                    //calculating progress percentage
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    //displaying percentage in progress dialog
                    progressDialog.setMessage("Downloaded " + ((int) progress) + "%...");
                    progressDialog.setProgress((int) progress);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editPDF() {
        File file = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/form_pdf.pdf");

        try {
            PdfReader reader = new PdfReader(file.toURL());
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(file));
            AcroFields form = stamper.getAcroFields();
            form.setGenerateAppearances(true);
            BaseFont unicode = BaseFont.createFont("res/font/rubik.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            form.addSubstitutionFont(unicode);
            form.setField("first_name", "aaa");
            form.setField("last_name", "בונה סוכה");
            stamper.setFormFlattening(true);
            stamper.close();
            reader.close();

            showPDF(file.getPath());

            Toast.makeText(StudentInfoActivity.this, "סתפם הודעה", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPDF(String pdfPath)
    {
        File pdfFile = new File(pdfPath);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        // for newer build versions (24 or above) there is another way to open pdf files
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri pdfUri = FileProvider.getUriForFile(this, "com.example.betaapp.fileprovider", pdfFile);

            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            // the pdf viewer needs permission to get the file that its in the app's directory
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        else{
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
        }

        // not all phones has a pdf reader :(
        try {
            startActivity(intent);
        }
        catch (Exception e){
            Toast.makeText(StudentInfoActivity.this, "אפליקציה לפתיחת קובץ זה לא נמצאה במכשיר", Toast.LENGTH_SHORT).show();
        }
    }
}