package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The type Form stud confirm activity.
 * @author Itey Weintraub <av5350@bs.amalnet.k12.il>
 * @version	1
 * short description:
 *
 *      This activity used to get confirms from the student's parents
 *      and to know how many brothers and sisters he has
 */
public class FormStudConfirmActivity extends AppCompatActivity {
    EditText numberBrothers;
    CheckBox checkboxAgreeContact, checkboxShareVideo;

    SeekBar seekbarState;

    // map between the xml tag name and the value that there (or would be)
    HashMap<String, String> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_stud_confirm);

        numberBrothers = (EditText) findViewById(R.id.numberBrothers);
        checkboxAgreeContact = (CheckBox) findViewById(R.id.checkboxAgreeContact);
        checkboxShareVideo = (CheckBox) findViewById(R.id.checkboxShareVideo);

        seekbarState = (SeekBar) findViewById(R.id.seekbarState);

        // make that the user could not change the seekbar state by clicking
        seekbarState.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                return true;
            }
        });

        // all the numeric fields accepts just numbers
        numberBrothers.setTransformationMethod(null);

        getFieldsData();
    }

    /**
     * Get the updated elements fields values from the xml form file
     */
     private void getFieldsData()
     {
         ArrayList<String> elementsIds = new ArrayList<>();

         // the fields we want their data from the xml file
         elementsIds.add("agreeContact");
         elementsIds.add("shareVideo");
         elementsIds.add("numberBrothers");

         // get the current data from the xml file
         data = XmlHelper.getData(elementsIds);

         // update elements with their current values (from the xml)
         numberBrothers.setText(data.get("numberBrothers"));
         checkboxAgreeContact.setChecked(Boolean.parseBoolean(data.get("agreeContact")));
         checkboxShareVideo.setChecked(Boolean.parseBoolean(data.get("shareVideo")));
     }

    /**
     * Save elements fields to the student's xml form.
     * Or also move activity if the next button was clicked
     *
     * @param view the view
     */
    public void saveData(View view) {
        // if one of the fields is empty
        if (!checkboxShareVideo.isChecked() || !checkboxAgreeContact.isChecked() || (numberBrothers.getText().toString().equals("")))
        {
            Toast.makeText(FormStudConfirmActivity.this, "???????? ?????????? ???????????? ?????? ?????????? ????????????????", Toast.LENGTH_SHORT).show();
        }
        else {
            data.put("agreeContact", String.valueOf(checkboxAgreeContact.isChecked()));
            data.put("shareVideo", String.valueOf(checkboxShareVideo.isChecked()));
            data.put("numberBrothers", numberBrothers.getText().toString());

            XmlHelper.pushData(data);

            // if want to move page
            // this view has parameter of tag - so we know to move page
            if (view.getTag().equals("move"))
            {
                FBref.refStudents.child(Helper.currentStudentId).child("secondParentEmail")
                        .addListenerForSingleValueEvent(new ValueEventListener(){
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // if the student have 2 parents
                                if (!snapshot.getValue(String.class).equals(""))
                                {
                                    Intent si = new Intent(FormStudConfirmActivity.this, FormParentsActivity.class);
                                    si.putExtra("dad", true); // false = mom
                                    si.putExtra("showActivityOnce", false);
                                    startActivity(si);
                                }
                                else {
                                    // if needs to show just 1 parent - check who is the filler (logged in user) gender
                                    FBref.refUsers.child(FBref.auth.getUid()).child("isDad")
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    boolean isDad = snapshot.getValue(Boolean.class);

                                                    Intent si = new Intent(FormStudConfirmActivity.this, FormParentsActivity.class);
                                                    si.putExtra("dad", isDad); // false = mom, true = dad
                                                    si.putExtra("showActivityOnce", true);
                                                    startActivity(si);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        }
    }

    /**
     * Go back to the last screen (FormActivity)
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
            Intent si = new Intent(FormStudConfirmActivity.this, CreditsActivity.class);
            startActivity(si);
        }

        return true;
    }
}