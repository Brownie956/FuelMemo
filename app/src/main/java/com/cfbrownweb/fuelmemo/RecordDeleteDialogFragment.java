/*Author: Chris Brown
* Date: 22/02/2016
* Description: Fragment class for dialog used in deleting records
* The dialog shows a multi choice list*/
package com.cfbrownweb.fuelmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

public class RecordDeleteDialogFragment extends DialogFragment {
    private static final String TAG = "cfbrownweb";

    private ArrayList<Record> mSelectedItems;  //List of the selected records
    private ArrayList<Record> items; //Selectable records

    public RecordDeleteDialogFragment(){
        this.items = new ArrayList<Record>();
        this.mSelectedItems = new ArrayList<Record>();
    }

    public void setItems(ArrayList<Record> items) {
        this.items = items;
    }

    public interface DeleteRecDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, ArrayList<Record> selectedItems);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    DeleteRecDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Check that the host has implemented the callback interface
        try {
            mListener = (DeleteRecDialogListener) activity;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement deleteRecDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.del_rec_title))
                .setMultiChoiceItems(genItemStrings(items), null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Record record = items.get(which);
                        if (isChecked) {
                            mSelectedItems.add(record);
                        } else if (mSelectedItems.contains(record)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedItems.remove(record);
                        }

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(RecordDeleteDialogFragment.this, mSelectedItems);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(RecordDeleteDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String[] genItemStrings(ArrayList<Record> items){
        //Create valid strings for the delete list
        String[] itemStrings = new String[items.size()];

        //Put all the values into the itemStrings array
        int i = 0;
        for(Record record : items){
            StringBuilder sb = new StringBuilder();
            sb.append(record.getDate());
            sb.append("\n");
            sb.append(record.getMiles());
            sb.append(" miles\n£");
            sb.append(record.getCost());
            sb.append("\n");
            itemStrings[i] = sb.toString();
            i++;
        }
        return itemStrings;
    }

}
