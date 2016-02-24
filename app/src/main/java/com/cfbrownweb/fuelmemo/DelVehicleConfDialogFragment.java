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

public class DelVehicleConfDialogFragment extends DialogFragment {
    private static final String TAG = "cfbrownweb";

    private ArrayList<Vehicle> itemsAL; //Items queued for delete

    public DelVehicleConfDialogFragment(){
        this.itemsAL = new ArrayList<Vehicle>();
    }

    public void setItems(ArrayList<Vehicle> items) {
        this.itemsAL = items;
    }

    public interface confirmDelDialogListener {
        public void onDialogDeleteClick(DialogFragment dialog, ArrayList<Vehicle> selectedItems);
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
        builder.setTitle(getString(R.string.del_conf_title))
                .setMessage(getString(R.string.del_veh_conf_message) + "\n" + constructDelQueueString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogDeleteClick(DelVehicleConfDialogFragment.this, itemsAL);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(DelVehicleConfDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String constructDelQueueString(){
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(Vehicle vehicle : itemsAL){
            sb.append(i);
            sb.append(". ");
            sb.append(vehicle.getPlate().toUpperCase());
            sb.append("\n");
            sb.append(vehicle.getName());
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
