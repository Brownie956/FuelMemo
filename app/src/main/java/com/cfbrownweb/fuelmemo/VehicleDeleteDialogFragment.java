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

public class VehicleDeleteDialogFragment extends DialogFragment {
    private static final String TAG = "cfbrownweb";

    private ArrayList<Vehicle> mSelectedItemsAL;  //List of the selected items
    private ArrayList<Vehicle> itemsAL; //Selectable items
    private Activity hostActivity;

    public VehicleDeleteDialogFragment(){
        this.itemsAL = new ArrayList<Vehicle>();
        this.mSelectedItemsAL = new ArrayList<Vehicle>();
    }

    public void setItems(ArrayList<Vehicle> items) {
        this.itemsAL = items;
    }

    public interface DeleteDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, ArrayList<Vehicle> selectedItems);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    DeleteDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        hostActivity = activity;

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
        builder.setTitle(getString(R.string.del_veh_title))
                .setMultiChoiceItems(genItemStrings(itemsAL), null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        Vehicle vehicle = itemsAL.get(which);
                        if (isChecked) {
                            mSelectedItemsAL.add(vehicle);
                        } else if (mSelectedItemsAL.contains(vehicle)) {
                            // Else, if the item is already in the array, remove it
                            mSelectedItemsAL.remove(vehicle);
                        }

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogPositiveClick(VehicleDeleteDialogFragment.this, mSelectedItemsAL);
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

    private String[] genItemStrings(ArrayList<Vehicle> items){
        //Create valid strings for the delete list
        String[] itemStrings = new String[items.size()];

        //Put all the values into the itemStrings array
        for(int i = 0; i < items.size(); i++){
            StringBuilder sb = new StringBuilder();
            sb.append(items.get(i).getPlate().toUpperCase());
            sb.append("\n");
            sb.append(items.get(i).getName());
            sb.append("\n");
            itemStrings[i] = sb.toString();
        }

        return itemStrings;
    }
}
