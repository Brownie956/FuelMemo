/*Author: Chris Brown
* Date: 21/02/2016
* Description: Activity class for adding a vehicle*/
package com.cfbrownweb.fuelmemo;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AddVehicleActivity extends AppCompatActivity implements MaxVehiclesDialogFragment.MaxVehiclesDialogListener {

    private static final String TAG = "cfbrownweb";

    private final String addVehicleUrl = "http://cfbrownweb.ngrok.io/fuel/addVehicle.php";
    private final String getNumberOfVehiclesUrl = "http://cfbrownweb.ngrok.io/fuel/getNumberOfVehicles.php";

    private EditText plateInput;
    private EditText nameInput;

    private final int maxVehicles = 10;

    private String plate;
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set title
        setTitle(getString(R.string.add_vehicle_title));

        plateInput = (EditText) findViewById(R.id.plate_input);
        nameInput = (EditText) findViewById(R.id.name_input);

        /*Plate and Name must only contain letters or numbers
        * Plate - Max 7 characters, no whitespace
        * Name - Max 50 characters, whitespace allowed */
        plateInput.setFilters(new InputFilter[]{new LettersNumberInputFilter(false), new InputFilter.LengthFilter(7)});
        nameInput.setFilters(new InputFilter[]{new LettersNumberInputFilter(true), new InputFilter.LengthFilter(50)});
    }

    private void addVehicleReq(){
        RequestQueue queue = Volley.newRequestQueue(AddVehicleActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, addVehicleUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Check for correct response
                        if (response.equals("1")) {
                            //success
                            goToVehicles();
                        } else {
                            //Something went wrong
                            Toast.makeText(AddVehicleActivity.this, "Oops, Something went wrong, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO handle error
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("plate",plate);
                params.put("name",name);

                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void submitVehicle(View view){
        //Check for error
        if(plateInput.getText().toString().length() < 1){
            Toast.makeText(this, "Enter a valid plate", Toast.LENGTH_LONG).show();
        }
        else if(nameInput.getText().toString().length() < 1){
            Toast.makeText(this, "Enter a valid name", Toast.LENGTH_LONG).show();
        }
        else {
            //get input values
            plate = plateInput.getText().toString();
            name = nameInput.getText().toString();

            RequestQueue queue = Volley.newRequestQueue(AddVehicleActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, getNumberOfVehiclesUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                int numberOfVehicles = Integer.parseInt(response);

                                //Max number of records met?
                                if (numberOfVehicles >= maxVehicles) {
                                    //Hide keyboard
                                    // Check if no view has focus:
                                    View view = AddVehicleActivity.this.getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }

                                    //Need to delete oldest record - show alert dialog
                                    MaxVehiclesDialogFragment maxVehiclesDialogFragment = new MaxVehiclesDialogFragment();
                                    maxVehiclesDialogFragment.show(getFragmentManager(), "MaxVehiclesReachedDialog");
                                } else {
                                    //Clear inputs
                                    plateInput.getText().clear();
                                    nameInput.getText().clear();

                                    //Send add vehicle request
                                    addVehicleReq();
                                }
                            } catch (NumberFormatException e) {
                                Log.d(TAG, "Didn't parse response");
                                //TODO handle exception
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //TODO handle error
                }
            });
            queue.add(stringRequest);
        }
    }

    private void goToVehicles(){
        Intent vehiclesIntent = new Intent(this, VehiclesActivity.class);
        startActivity(vehiclesIntent);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //Don't add vehicle and go to vehicles page
        goToVehicles();
    }
}
