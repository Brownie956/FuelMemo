/*Author: Chris Brown
* Date: 22/02/2016
* Description: Fragment class for confirmation dialog when deleting records*/
package com.cfbrownweb.fuelmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

public class DelRecordConfDialogFragment extends DialogFragment {
    private static final String TAG = "cfbrownweb";

    private ArrayList<Record> items; //Items queued for delete

    public DelRecordConfDialogFragment(){
        this.items = new ArrayList<Record>();
    }

    public void setItems(ArrayList<Record> items) {
        this.items = items;
    }

    public interface confirmDelDialogListener {
        public void onDialogDeleteClick(DialogFragment dialog, ArrayList<Record> selectedItems);
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
                .setMessage(getString(R.string.del_rec_conf_message) + "\n" + constructDelQueueString())
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogDeleteClick(DelRecordConfDialogFragment.this, items);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogNegativeClick(DelRecordConfDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private String constructDelQueueString(){
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for(Record record : items){
            sb.append(i);
            sb.append(". ");
            sb.append(record.getDate());
            sb.append("\n");
            sb.append(record.getMiles());
            sb.append(" miles");
            sb.append("\n£");
            sb.append(record.getCost());
            sb.append("\n\n");
            i++;
        }

        /*Creates this:
        * 1. DATE 1
        * MILES 1
        * COST 1
        *
        * 2. DATE 2
        * MILES 2
        * COST 2
        * etc...*/

        return sb.toString();
    }
}
