package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
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
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The type Student info activity.
 */
public class StudentInfoActivity extends AppCompatActivity {
    TextView studentFirstName, studentLastName, studentID, studentCity, studentSchool, studentGrade,
            studentMaslul, phoneParent, emailParent, typeParent, parentName;
    String parentPhone, parentEmail, studentId;

    ProgressDialog progressDialog;

    HashMap<String, String> fields; // student's form fields

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_info);

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
        studentId = gi.getStringExtra("studentID");
        String localFormPath = getApplicationContext().getCacheDir().getAbsolutePath() + "/" + studentId + ".xml";
        XmlHelper.init(localFormPath, false);

        studentID.setText("תעודת זהות: " + studentId);

        // get all relevant data from the xml
        fields = getFieldsData();
        studentFirstName.setText("שם פרטי: " + fields.get("firstName"));
        studentLastName.setText("שם משפחה: " + fields.get("lastName"));
        studentCity.setText("יישוב: " + fields.get("city"));
        studentSchool.setText("בית ספר: " + fields.get("currentSchool"));
        studentGrade.setText("כיתה: " + fields.get("wantedClass"));
        studentMaslul.setText("מסלול: " + fields.get("maslul"));

        // get the info of the parent who filled the form (its mail + phone)
        getFillerInfo(studentId);
    }

    /**
     * This function gets info (name, phone, email) of the parent who filled the form to this student
     *
     * @param studentID the id of the student
     */
    private void getFillerInfo(String studentID)
    {
        FBref.refStudents.child(studentID).child("parentUID").addListenerForSingleValueEvent(new ValueEventListener() {
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

    /**
     * This function creates a list of the fields we want to get from the xml form
     * and then, it gets those fields values from the form
     *
     * @return hash map of those fields and their values
     */
    private HashMap<String, String> getFieldsData()
    {
        ArrayList<String> wantedTags = new ArrayList<>();
        wantedTags.add("firstName");
        wantedTags.add("lastName");
        wantedTags.add("city");
        wantedTags.add("currentSchool");
        wantedTags.add("wantedClass");
        wantedTags.add("maslul");

        // also fields for the pdf option
        wantedTags.add("birthDateHebrew");
        wantedTags.add("wantedClass");
        wantedTags.add("street");
        wantedTags.add("addressNumber");
        wantedTags.add("city");
        wantedTags.add("maslul");
        wantedTags.add("dadFirstName");
        wantedTags.add("dadLastName");
        wantedTags.add("dadID");
        wantedTags.add("dadProfession");
        wantedTags.add("dadEmail");
        wantedTags.add("dadPhone");
        wantedTags.add("momFirstName");
        wantedTags.add("momLastName");
        wantedTags.add("momID");
        wantedTags.add("momProfession");
        wantedTags.add("momEmail");
        wantedTags.add("momPhone");
        wantedTags.add("comments");

        return XmlHelper.getData(wantedTags);
    }

    /**
     * Open whatsapp app with the parent phone number.
     *
     * @param view the view
     */
    public void openWhatsapp(View view) {
        // send the user to whatsapp view (app)
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://wa.me/" + parentPhone));
        startActivity(intent);
    }

    /**
     * Open email with new conversation with the parent email address.
     *
     * @param view the view
     */
    public void openEmail(View view) {
        Uri uri = Uri.parse("mailto:").buildUpon()
                .appendQueryParameter("to", parentEmail)
                .build();

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);
        startActivity(Intent.createChooser(emailIntent, "שלח באמצעות..."));
    }

    /**
     * download a template pdf form from firebase. and then call to a function that fill it
     *
     * @param view the view
     */
    public void downloadTemplatePDF(View view)
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

    /**
     * This function edits a pdf form with the data from the student's xml form
     */
    private void editPDF() {
        File file = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/form_pdf.pdf");

        try {
            PdfReader reader = new PdfReader(file.toURL());
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(file));
            AcroFields form = stamper.getAcroFields();
            form.setGenerateAppearances(true);
            BaseFont unicode = BaseFont.createFont("res/font/rubik.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            form.addSubstitutionFont(unicode);

            // put all the form's values
            form.setField("first_name", fields.get("firstName"));
            form.setField("last_name", fields.get("lastName"));
            form.setField("birthDateHebrew", fields.get("birthDateHebrew"));
            form.setField("stud_id", studentId);
            form.setField("wantedClass", fields.get("wantedClass"));
            form.setField("address", fields.get("street") + " " + fields.get("addressNumber") + ", " + fields.get("city"));
            form.setField("maslul", fields.get("maslul"));
            form.setField("dadFirstName", fields.get("dadFirstName"));
            form.setField("dadLastName", fields.get("dadLastName"));
            form.setField("dadID", fields.get("dadID"));
            form.setField("dadProfession", fields.get("dadProfession"));
            form.setField("dadEmail", fields.get("dadEmail"));
            form.setField("dadPhone", fields.get("dadPhone"));
            form.setField("momFirstName", fields.get("momFirstName"));
            form.setField("momLastName", fields.get("momLastName"));
            form.setField("momID", fields.get("momID"));
            form.setField("momProfession", fields.get("momProfession"));
            form.setField("momEmail", fields.get("momEmail"));
            form.setField("momPhone", fields.get("momPhone"));
            form.setField("comments", fields.get("comments"));

            stamper.setFormFlattening(true); // set the form to read only state
            stamper.close();
            reader.close();

            // if the student have a profile picture - add it to the pdf
            // otherwise - just show the current pdf
            addStudentPicture();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This function adds to the student's profile picture f it exists
     * If is so, the function adds the picture to the pdf and then show the result pdf
     * If the picture not exists, the function just show the pdf without the picture
     */
    private void addStudentPicture()
    {
        File studentPhotoFile = new File(getApplicationContext().getCacheDir().getAbsolutePath() + "/studImg.png");

        // try to get the student picture from firebase (and download it)
        FBref.storageRef.child("files").child(studentId).child("student_picture").getFile(studentPhotoFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // Local file has been created
                try {
                    PdfReader pdfReader = new PdfReader(getApplicationContext().getFilesDir().getAbsolutePath() + "/form_pdf.pdf");
                    PdfStamper pdfStamper = new PdfStamper(pdfReader,
                            new FileOutputStream(getApplicationContext().getFilesDir().getAbsolutePath() + "/form.pdf"));

                    Image image = Image.getInstance(Uri.fromFile(studentPhotoFile).toString());

                    image.scaleToFit(200, 200);
                    image.setAbsolutePosition(40, 40);
                    pdfStamper.getOverContent(1).addImage(image);

                    pdfStamper.close();
                    pdfReader.close();

                    showPDF(getApplicationContext().getFilesDir().getAbsolutePath() + "/form.pdf");

                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // If the student doesn't have already a picture of himself - just show the form until now
                showPDF(getApplicationContext().getFilesDir().getAbsolutePath() + "/form_pdf.pdf");
            }
        });
    }

    /**
     * This function opens a pdf in new application that the user choose
     *
     * @param pdfPath the path to the pdf file
     */
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
            Intent si = new Intent(StudentInfoActivity.this, CreditsActivity.class);
            startActivity(si);
        }

        return true;
    }
}