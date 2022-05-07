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

public class CustomStudentsLvAdapter extends BaseAdapter {
    Context context;
    ArrayList<String> registerStatus;
    ArrayList<String> firstNames;
    ArrayList<String> lastNames;
    ArrayList<String> studentsIDS;
    LayoutInflater inflter;

    public CustomStudentsLvAdapter(Context applicationContext, ArrayList<String> registerStatus,
                                   ArrayList<String> firstNames, ArrayList<String> lastNames, ArrayList<String> studentsIDS) {
        this.context = applicationContext;
        this.registerStatus = registerStatus;
        this.firstNames = firstNames;
        this.lastNames = lastNames;
        this.studentsIDS = studentsIDS;
        inflter = (LayoutInflater.from(applicationContext));
    }

    @Override
    public int getCount() {
        return registerStatus.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

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
