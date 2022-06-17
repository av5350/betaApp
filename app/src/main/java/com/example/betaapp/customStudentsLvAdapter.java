package com.example.betaapp;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * The type Custom students listView adapter.
 * @author Itey Weintraub <av5350@bs.amalnet.k12.il>
 * @version	1
 * short description:
 *
 *      This Activity adapter used in the sort students activity (in the admin)
 *      This adapter created the ui ehind it
 */
public class CustomStudentsLvAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> registerStatus, firstNames, lastNames, studentsIDS;
    LayoutInflater inflter;

    /**
     * Instantiates a new Custom students lv adapter.
     *
     * @param applicationContext the application context
     * @param registerStatus     the register status
     * @param firstNames         the first names
     * @param lastNames          the last names
     * @param studentsIDS        the students ids
     */
    public CustomStudentsLvAdapter(Context applicationContext, ArrayList<String> registerStatus,
                                   ArrayList<String> firstNames, ArrayList<String> lastNames, ArrayList<String> studentsIDS) {
        this.context = applicationContext;
        this.registerStatus = registerStatus;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.studentsIDS = studentsIDS;
        inflter = (LayoutInflater.from(applicationContext));
    }

    /**
     * Gets the data arrays length
     *
     * @return the length
     */
    @Override
    public int getCount() {
        return registerStatus.size();
    }

    /**
     * Gets item at specific location
     *
     * @param i the location
     * @return the object
     */
    @Override
    public Object getItem(int i) {
        return null;
    }

    /**
     * Gets the id of an item at some location
     *
     * @param i the location
     * @return the id
     */
    @Override
    public long getItemId(int i) {
        return 0;
    }

    /**
     * Gets the view for the lv
     *
     * @param i the row number
     * @param view the view
     * @param viewGroup the view group
     * @return the view for the list view
     */
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.custom_students_lv_layout, null);
        TextView studRegisterStatus = (TextView) view.findViewById(R.id.studRegisterStatus);
        TextView studFirstName = (TextView) view.findViewById(R.id.studFirstName);
        TextView studLastName = (TextView) view.findViewById(R.id.studLastName);
        TextView studID = (TextView) view.findViewById(R.id.studID);

        studRegisterStatus.setText(registerStatus.get(i));
        studFirstName.setText(firstNames.get(i));
        studLastName.setText(lastNames.get(i));
        studID.setText(studentsIDS.get(i));

        // set title's color to bold (0 location)
        if (i == 0)
        {
            studRegisterStatus.setTypeface(Typeface.DEFAULT_BOLD);
            studFirstName.setTypeface(Typeface.DEFAULT_BOLD);
            studLastName.setTypeface(Typeface.DEFAULT_BOLD);
            studID.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return view;
    }
}
