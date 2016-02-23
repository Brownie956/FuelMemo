/*Author: Chris Brown
* Date: 18/02/2016
* Description: Launcher activity. Connects to db, pulls all vehicles and displays them
* Also implements functionality for deleting vehicles*/
package com.cfbrownweb.fuelmemo;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class VehiclesActivity extends AppCompatActivity implements VehicleDeleteDialogFragment.DeleteDialogListener,
        DelVehicleConfDialogFragment.confirmDelDialogListener {

    private final static String TAG = "cfbrownweb"; //debug tag

    private final String allVehiclesUrl = "http://cfbrownweb.ngrok.io/fuel/getAllVehicles.php";
    private final String deleteVehiclesUrl = "http://cfbrownweb.ngrok.io/fuel/deleteVehicles.php";

    private RelativeLayout vehiclesContent;
    private TextView connectionPrompt;
    private Button connectionRetryButton;
    private LinkedHashMap<String, String> vehicles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);
        setTitle(getResources().getString(R.string.vehicles_title));
        vehiclesContent = (RelativeLayout) findViewById(R.id.vehicles_content_layout);
        vehicles = new LinkedHashMap<String, String>();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(isConnected()) {
            //Get all of the vehicles and display
            allVehiclesReq();
        }
        else {
            //TODO move to own toggle function
            connectionPrompt = new TextView(this);
            connectionRetryButton = new Button(this);

            //Set prompt text properties
            connectionPrompt.setText(getResources().getString(R.string.net_retry_prompt));
            connectionPrompt.setTextSize(17);
            connectionPrompt.setGravity(Gravity.CENTER);

            //prompt layout details
            RelativeLayout.LayoutParams promptDetails = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            promptDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);
            promptDetails.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            promptDetails.setMargins(100, 150, 100, 50);

            //set retry button properties
            connectionRetryButton.setText(getResources().getString(R.string.net_con_btn_text));

            RelativeLayout.LayoutParams retryBtnDetails = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            retryBtnDetails.addRule(RelativeLayout.CENTER_VERTICAL);
            retryBtnDetails.addRule(RelativeLayout.CENTER_HORIZONTAL);

            //Retry connection when button is clicked
            connectionRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    retryNet();
                }
            });

            vehiclesContent.addView(connectionPrompt, promptDetails);
            vehiclesContent.addView(connectionRetryButton, retryBtnDetails);
        }
    }

    public void allVehiclesReq() {
        RequestQueue queue = Volley.newRequestQueue(VehiclesActivity.this);
        JsonArrayRequest jsArrayRequest = new JsonArrayRequest(allVehiclesUrl,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.i(TAG, response.toString());

                        final JSONArray returnedResponse = response;

                        //TODO check for no vehicles

                        //Store all vehicles in map
                        storeVehiclesInHashMap(returnedResponse);

                        //Add all returned vehicles to the list view
                        ListAdapter vehicleAdaptor = new VehicleAdapter(VehiclesActivity.this, returnedResponse);
                        ListView vehicleListView = (ListView) findViewById(R.id.vehicle_list);
                        vehicleListView.setAdapter(vehicleAdaptor);

                        //Click listener on each item
                        vehicleListView.setOnItemClickListener(
                                new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                        try{
                                            //Get the value of the position clicked
                                            String plate = returnedResponse.getJSONObject(position).getString("plate");
                                            String name = returnedResponse.getJSONObject(position).getString("name");
//                                            Toast.makeText(VehiclesActivity.this, plate, Toast.LENGTH_LONG).show();
                                            goToOverview(plate,name);
                                        }
                                        catch (JSONException e){
                                            //TODO Handle Json Exception
                                        }
                                    }
                                }
                        );
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO handle error
            }
        }
        );
        queue.add(jsArrayRequest);
    }

    public void deleteVehicles(final ArrayList<String> vehiclesToDelete) {
        RequestQueue queue = Volley.newRequestQueue(VehiclesActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteVehiclesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Check for success
                        if(response.equals("1")){
                            //Remove the vehicles from vehicles hashmap
                            for(String vehicle : vehiclesToDelete){
                                vehicles.remove(vehicle);
                            }

                            //Reload the vehicles list
                            allVehiclesReq();
                        }
                        else {
                            //Something went wrong
                            Toast.makeText(VehiclesActivity.this, "Oops, Something went wrong, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //TODO handle error
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                Iterator<String> delIterator = vehiclesToDelete.iterator();
                int i = 0;
                while(delIterator.hasNext()){
                    params.put("plates["+i+"]", delIterator.next());
                    i++;
                }

                return params;
            }
        };
        queue.add(stringRequest);
    }

    private void goToOverview(String plate, String name){
        //Store vehicle in config
        Vehicle vehicle = new Vehicle(plate, name);
        Configuration.getConfig().setVehicle(vehicle);

        Intent intent = new Intent(this, OverviewActivity.class);
        startActivity(intent);
    }

    private void retryNet(){
        //Get connection status and decide what to do
        if(!isConnected()){
            Toast.makeText(this,"No internet connection", Toast.LENGTH_LONG).show();
        }
        else {
            //Remove connection prompt
            vehiclesContent.removeView(connectionPrompt);
            vehiclesContent.removeView(connectionRetryButton);

            //Get all of the vehicles and display
            allVehiclesReq();
        }
    }

    private boolean isConnected(){
        //Get network information
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private void storeVehiclesInHashMap(JSONArray vehicles){
        Log.i(TAG, "Vehicles: " + vehicles.toString());
        for(int i = 0; i < vehicles.length(); i++){
            try {
                //Get vehicle
                JSONObject vehicle = vehicles.getJSONObject(i);
                //Store in hashmap
                this.vehicles.put(vehicle.getString("plate"), vehicle.getString("name"));
            }
            catch (JSONException e) {
                //TODO handle exception
            }
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, ArrayList<String> selectedItems) {
        //Are you sure? - Confirmation
        DelVehicleConfDialogFragment delConf = new DelVehicleConfDialogFragment();

        //Store selected vehicles in hashmap with associated name
        LinkedHashMap<String, String> delVehicles = new LinkedHashMap<String,String>();
        for(String vehicle : selectedItems){
            delVehicles.put(vehicle, vehicles.get(vehicle));
        }
        delConf.setItems(delVehicles);

        //Show confirmation dialog
        delConf.show(getFragmentManager(), "DelConfirmationDialog");
    }

    @Override
    public void onDialogDeleteClick(DialogFragment dialog, ArrayList<String> selectedItems) {
        //Delete items
        deleteVehicles(selectedItems);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //Cancel
        Toast.makeText(this, getString(R.string.submit_cancel), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vehicles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.vehicles_menu_add_vehicle:
                Intent addVehicleIntent = new Intent(this, AddVehicleActivity.class);
                startActivity(addVehicleIntent);
                return true;
            case R.id.vehicles_menu_delete_vehicle:
                VehicleDeleteDialogFragment deleteDialogFragment = new VehicleDeleteDialogFragment();
                deleteDialogFragment.setItems(vehicles);
                deleteDialogFragment.show(getFragmentManager(), "deleteVehicleDialog");
                return true;
            case R.id.vehicles_menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}