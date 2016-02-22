/*Author: Chris Brown
* Date: 22/02/2016
* Description: Fragment class for dialog used in deleting vehicles
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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class VehicleDeleteDialogFragment extends DialogFragment {
    private static final String TAG = "cfbrownweb";

    private ArrayList<String> mSelectedItems;  //List of the selected items
    private LinkedHashMap<String,String> items; //Selectable items

    public VehicleDeleteDialogFragment(){
        this.items = new LinkedHashMap<String,String>();
        this.mSelectedItems = new ArrayList<String>();
    }

    public void setItems(LinkedHashMap<String, String> items) {
        this.items = items;
    }

    public interface DeleteDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, ArrayList<String> selectedItems);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    DeleteDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Check that the host has implemented the callback interface
        try {
            mListener = (DeleteDialogListener) activity;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement confirmDelDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Which to delete?")
                .setMultiChoiceItems(genItemStrings(items), null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        String plate = getPlateFromMap(which);
                        if(isChecked){
                            mSelectedItems.add(plate);
                        } else if (mSelectedItems.contains(plate)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedItems.remove(plate);
                        }

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(VehicleDeleteDialogFragment.this, mSelectedItems);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(VehicleDeleteDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String[] genItemStrings(HashMap<String,String> items){
        //Create valid strings for the delete list
        String[] itemStrings = new String[items.size()];

        //Put all the values into the itemStrings array
        int i = 0;
        for(HashMap.Entry<String, String> entry : items.entrySet()){
            itemStrings[i] = entry.getKey() + "\n" + entry.getValue();
            i++;
        }
        return itemStrings;
    }

    private String getPlateFromMap(int index){
        Iterator<String> itemsIterator = items.keySet().iterator();
        String plate = "";

        try{
            for(int i = 0; i < index; i++){
                itemsIterator.next();
            }
            plate = itemsIterator.next();
        }
        catch (NoSuchElementException e){
            //TODO handle exception
        }
        return plate;
    }
}
