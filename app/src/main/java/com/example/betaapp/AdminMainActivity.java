package com.example.betaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * The type Admin main activity.
 * @author Itey Weintraub <av5350@bs.amalnet.k12.il>
 * @version	1
 * short description:
 *
 *      This Activity gives to the admin shortcuts for the options he have in the app
 */
public class AdminMainActivity extends AppCompatActivity {
    TextView welcomeAdmin, submittedFormCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        welcomeAdmin = (TextView) findViewById(R.id.welcomeAdmin);
        submittedFormCount = (TextView) findViewById(R.id.submittedFormCount);

        getWelcomeMsg();
        getSubmittedFormCount();
    }

    /**
     * This function init the welcome message (with the admin's first name)
     */
    private void getWelcomeMsg()
    {
        FBref.refUsers.child(FBref.auth.getCurrentUser().getUid()).child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                welcomeAdmin.setText("ברוך הבא " + dS.getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /**
     * This function get from firebase the number of students that submitted the form
     */
    private void getSubmittedFormCount()
    {
        // filter just the students that were submitted the form
        Query query =  FBref.refStudents.orderByChild("status").equalTo(1);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                int studentsCount = (int) dS.getChildrenCount();

                // in hebrew for the number 1 there must be another text (for ux)
                if (studentsCount == 1)
                {
                    submittedFormCount.setText("עד כה הגישו את השאלון: תלמיד אחד");
                }
                else
                {
                    submittedFormCount.setText("עד כה הגישו את השאלון: " + studentsCount + " תלמידים");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * Go to sort students data activity.
     *
     * @param view the view
     */
    public void goSortData(View view) {
        Intent si = new Intent(AdminMainActivity.this, AdminSortDataActivity.class);
        startActivity(si);
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
            Intent si = new Intent(AdminMainActivity.this, CreditsActivity.class);
            startActivity(si);
        }

        return true;
    }
}