/*Author: Chris Brown
* Date: 22/02/2016
* Description: Fragment class for confirmation dialog when deleting vehicles*/
package com.cfbrownweb.fuelmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeleteConfirmationDialogFragment extends DialogFragment {
    private static final String TAG = "cfbrownweb";

    private LinkedHashMap<String,String> items; //Items queued for delete
    private ArrayList<String> delPlates;

    public DeleteConfirmationDialogFragment(){
        this.items = new LinkedHashMap<String,String>();
        this.delPlates = new ArrayList<String>();
    }

    public void setItems(LinkedHashMap<String, String> items) {
        this.items = items;
        for(String plate : items.keySet()){
            delPlates.add(plate);
        }

    }

    public interface confirmDelDialogListener {
        public void onDialogDeleteClick(DialogFragment dialog, ArrayList<String> selectedItems);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    confirmDelDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Check that the host has implemented the callback interface
        try {
            mListener = (confirmDelDialogListener) activity;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DeleteConfirmationDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure?")
                .setMessage("Are you sure you want to delete the following vehicles:\n" + constructDelQueueString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogDeleteClick(DeleteConfirmationDialogFragment.this, delPlates);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(DeleteConfirmationDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String constructDelQueueString(){
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(Map.Entry<String, String> vehicle : items.entrySet()){
            sb.append(i);
            sb.append(". ");
            sb.append(vehicle.getKey().toUpperCase());
            sb.append("\n");
            sb.append(vehicle.getValue());
            sb.append("\n");
            i++;
        }

        /*Creates this:
        * 1. PLATE 1
        * NAME 1
        * 2. PLATE 2
        * NAME 2
        * etc...*/

        return sb.toString();
    }
}
