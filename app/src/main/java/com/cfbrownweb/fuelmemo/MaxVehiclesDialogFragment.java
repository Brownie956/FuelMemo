/*Author: Chris Brown
* Date: 21/02/2016
* Description: Fragment class for alert dialog when the max number of vehicles is met*/
package com.cfbrownweb.fuelmemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MaxVehiclesDialogFragment extends DialogFragment {
    private static final String TAG = "cfbrownweb";

    public interface MaxVehiclesDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }

    MaxVehiclesDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        //Check that the host has implemented the callback interface
        try {
            mListener = (MaxVehiclesDialogListener) activity;
        }
        catch(ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MaxVehiclesDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.max_vehicles_reached_message))
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(MaxVehiclesDialogFragment.this);
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }


}
