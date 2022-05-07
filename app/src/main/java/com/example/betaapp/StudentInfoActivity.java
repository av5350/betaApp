package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class StudentInfoActivity extends AppCompatActivity {
    TextView studentFirstName, studentLastName, studentID, studentCity, studentSchool, studentGrade,
            studentMaslul, phoneParent, emailParent, typeParent, parentName;
    String parentPhone, parentEmail;

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
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_EMAIL, new String[]{parentEmail});
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "בחר אפליקציה כדי להמשיך: "));
    }
}