/*Author: Chris Brown
* Date: 23/02/2016
* Description: Activity class for the settings page.
* Handles displaying page content and setting user settings*/
package com.cfbrownweb.fuelmemo;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "cfbrownweb";
    private TextView nRecordsValueTv;
    private ArrayList<Button> tileColourButtons;

    int selectedColourIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Store all tile colour buttons
        tileColourButtons = new ArrayList<Button>();
        tileColourButtons.add((Button) findViewById(R.id.settings_colour_btn_grey));
        tileColourButtons.add((Button) findViewById(R.id.settings_colour_btn_blue));
        tileColourButtons.add((Button) findViewById(R.id.settings_colour_btn_green));
        tileColourButtons.add((Button) findViewById(R.id.settings_colour_btn_red));

        //Select the tile colour button
        SharedPreferences settings = Configuration.getConfig().getSharedPrefs(SettingsActivity.this);
        selectedColourIndex = settings.getInt("tileColour", 0);
        Log.i(TAG, "Selected colour Index: " + selectedColourIndex);
        Button selectedButton = tileColourButtons.get(selectedColourIndex);

        switch (selectedColourIndex){
            case 0:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_grey_selected));
                break;
            case 1:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_blue_selected));
                break;
            case 2:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_green_selected));
                break;
            case 3:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_red_selected));
                break;
            default:
                break;
        }

        //Set values from shared preferences
        nRecordsValueTv = (TextView) findViewById(R.id.settings_n_records_value);
        int nRecordsVal = settings.getInt("nRecords", 3);
        Log.i(TAG, "nRecords Val: " + nRecordsVal);
        nRecordsValueTv.setText(String.valueOf(nRecordsVal));
    }

    public void selectColour(View view){
        Button clickedButton = (Button) view;

        //Clear current selected
        clearSelectedTileColour();

        //Select the new colour
        //Get index and set colour
        int clickedButtonIndex = tileColourButtons.indexOf(clickedButton);
        switch (clickedButtonIndex){
            case 0:
                setDrawableBackground(clickedButton, ContextCompat.getDrawable(this, R.drawable.round_grey_selected));
                break;
            case 1:
                setDrawableBackground(clickedButton, ContextCompat.getDrawable(this, R.drawable.round_blue_selected));
                break;
            case 2:
                setDrawableBackground(clickedButton, ContextCompat.getDrawable(this, R.drawable.round_green_selected));
                break;
            case 3:
                setDrawableBackground(clickedButton, ContextCompat.getDrawable(this, R.drawable.round_red_selected));
                break;
            default:
                break;
        }

        //Update current selected
        SharedPreferences settings = Configuration.getConfig().getSharedPrefs(SettingsActivity.this);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putInt("tileColour", clickedButtonIndex);
        settingsEditor.apply();
        selectedColourIndex = clickedButtonIndex;
    }

    private void clearSelectedTileColour(){
        Button selectedButton = tileColourButtons.get(selectedColourIndex);
        switch (selectedColourIndex){
            case 0:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_grey));
                break;
            case 1:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_blue));
                break;
            case 2:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_green));
                break;
            case 3:
                setDrawableBackground(selectedButton, ContextCompat.getDrawable(this, R.drawable.round_red));
                break;
            default:
                break;
        }
    }

    public void showNumberPicker(View view){
        final Dialog npDialog = new Dialog(this);
        npDialog.setTitle(getString(R.string.settings_n_records_dialog_title));
        npDialog.setContentView(R.layout.number_picker_dialog);

        final NumberPicker np = (NumberPicker) npDialog.findViewById(R.id.n_records_numberPicker);

        np.setMinValue(1);
        np.setMaxValue(10);
        np.setWrapSelectorWheel(false);

        Button cancelBtn = (Button) npDialog.findViewById(R.id.np_cancel_button);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                npDialog.cancel();
            }
        });

        Button setBtn = (Button) npDialog.findViewById(R.id.np_set_button);
        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Display on page
                nRecordsValueTv.setText(String.valueOf(np.getValue()));

                //Store number selected
                SharedPreferences settings = Configuration.getConfig().getSharedPrefs(SettingsActivity.this);
                SharedPreferences.Editor prefEditor = settings.edit();
                prefEditor.putInt("nRecords", np.getValue());
                prefEditor.apply();

                //Hide dialog
                npDialog.dismiss();

                Log.i(TAG, String.valueOf(np.getValue()));
            }
        });

        npDialog.show();
    }

    /*Required to call setBackground for API <16*/
    private void setDrawableBackground(View view, Drawable background){
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(background);
        } else {
            view.setBackground(background);
        }
    }

}
