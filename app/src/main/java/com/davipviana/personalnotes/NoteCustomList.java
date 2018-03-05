package com.davipviana.personalnotes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Davi Viana on 25/02/2018.
 */

public class NoteCustomList extends LinearLayout {

    private Context context;
    private LinearLayout listItem;
    private List<EditText> editTextList = new ArrayList<>();

    public NoteCustomList(Context context) {
        super(context);
        this.context = context;
    }

    public void setUp() {
        setOrientation(VERTICAL);
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
    }

    public void setUpForHomeAdapter(String listEntries) {
        // Eggs$false%Ham$false%Bread$false%Vegemite$false
        setOrientation(VERTICAL);
        String[] listEntryTokens = listEntries.split("%");
        // [0] Eggs$false
        // [1] Ham$false
        // ...
        boolean isStrikeOut = false;
        String listItem = "";
        for(String entryDetails : listEntryTokens) {
            this.listItem = new LinearLayout(context);
            this.listItem.setOrientation(HORIZONTAL);
            String[] listEntry = entryDetails.split("\\$");
            //[0] Eggs
            // [1] false
            for(int i=0; i<listEntry.length; i++){
                if(i % 2 == 0) {
                    listItem = listEntry[i];
                } else {
                    isStrikeOut = Boolean.valueOf(listEntry[i]);
                }
            }

            CheckBox checkBox = new CheckBox(context);
            checkBox.setChecked(isStrikeOut);
            checkBox.setEnabled(false);
            TextView textView = new TextView(context);
            textView.setText(listItem);
            textView.setBackgroundColor(Color.TRANSPARENT);
            if(isStrikeOut) {
                textView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            this.listItem.addView(checkBox);
            this.listItem.addView(textView);
            addView(this.listItem);
        }
    }

    public void setUpForEditMode(String listEntries) {
        setOrientation(VERTICAL);
        editTextList = new ArrayList<>();
        String[] listEntryTokens = listEntries.split("%");
        boolean isStrikeOut = false;
        String listItem = "";

        for (String entryDetails : listEntryTokens) {
            this.listItem = new LinearLayout(context);
            this.listItem.setOrientation(HORIZONTAL);
            String[] listEntry = entryDetails.split("\\$");
            for (int i = 0; i < listEntry.length; i++) {
                if (i % 2 == 0)
                    listItem = listEntry[i];
                else
                    isStrikeOut = Boolean.valueOf(listEntry[i]);
            }

            final CheckBox checkBox = new CheckBox(context);
            checkBox.setChecked(isStrikeOut);

            final ImageView deleteImageView = new ImageView(context);
            deleteImageView.setImageResource(android.R.drawable.ic_menu_delete);

            final EditText textBox = new EditText(context);
            textBox.setText(listItem);
            textBox.setBackgroundColor(Color.TRANSPARENT);

            if (isStrikeOut) {
                textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            this.listItem.addView(deleteImageView);
            this.listItem.addView(checkBox);
            this.listItem.addView(textBox);
            editTextList.add(textBox);
            addView(this.listItem);

            deleteImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = (LinearLayout) v.getParent();
                    editTextList.remove(editTextList.indexOf(layout.getChildAt(2)));
                    layout.removeAllViews();
                }
            });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        textBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                }
            });
        }
    }

    public void setUpForListNotification(String listEntries) {

        setOrientation(VERTICAL);
        editTextList = new ArrayList<>();
        String[] listEntryTokens = listEntries.split("%");
        boolean isStrikeOut = false;
        String listItem = "";

        for (String entryDetails : listEntryTokens) {
            this.listItem = new LinearLayout(context);
            this.listItem.setOrientation(HORIZONTAL);
            //
            if(entryDetails.contains(AppConstant.TRUE)) {
                listItem = entryDetails.split(AppConstant.TRUE)[0];
                isStrikeOut = true;
            } else if(entryDetails.contains(AppConstant.FALSE)){
                listItem = entryDetails.split(AppConstant.FALSE)[0];
                isStrikeOut = false;
            }

            final CheckBox checkBox = new CheckBox(context);
            checkBox.setChecked(isStrikeOut);
            checkBox.setEnabled(false);
            final EditText textBox = new EditText(context);
            textBox.setText(listItem);
            textBox.setEnabled(false);
            textBox.setBackgroundColor(Color.TRANSPARENT);

            if (isStrikeOut) {
                textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            this.listItem.addView(checkBox);
            this.listItem.addView(textBox);
            editTextList.add(textBox);
            addView(this.listItem);
        }
    }
    public void addNewCheckBox() {
        this.listItem = new LinearLayout(context);
        this.listItem.setOrientation(HORIZONTAL);
        final ImageView deleteImageView = new ImageView(context);
        deleteImageView.setImageResource(android.R.drawable.ic_menu_delete);

        final EditText textBox = new EditText(context);
        textBox.setBackgroundColor(Color.TRANSPARENT);
        textBox.setHint("....");
        textBox.setSelection(0);
        textBox.requestFocus();
        final CheckBox checkBox = new CheckBox(context);
        checkBox.setPadding(10, 10, 10, 10);


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    textBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
        });
        this.listItem.addView(deleteImageView);
        this.listItem.addView(checkBox);
        this.listItem.addView(textBox);
        editTextList.add(textBox);
        addView(this.listItem);

        deleteImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) v.getParent();
                editTextList.remove(editTextList.indexOf(layout.getChildAt(2)));
                layout.removeAllViews();
            }
        });
    }

    public String getLists() {

        String stringToReturn = "";
        boolean isStrikeThrough;
        int itemsCount = editTextList.size();

        for (int i = 0; i < itemsCount; i++) {
            EditText editText = editTextList.get(i);
            if (editText.getPaintFlags() == Paint.STRIKE_THRU_TEXT_FLAG)
                isStrikeThrough = true;
            else
                isStrikeThrough = false;
            if (editText.getText().toString().length() > 0)
                stringToReturn = stringToReturn + editText.getText().toString() + "$" + isStrikeThrough + "%";
        }

        return stringToReturn;
    }
}
