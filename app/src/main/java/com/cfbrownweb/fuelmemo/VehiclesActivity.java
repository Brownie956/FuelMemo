/*Author: Chris Brown
* Date: 18/02/2016
* Description: Connects to db, pulls all vehicles and displays them
* Also implements functionality for deleting vehicles*/
package com.cfbrownweb.fuelmemo;

import android.app.DialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Map;

public class VehiclesActivity extends AppCompatActivity implements VehicleDeleteDialogFragment.DeleteDialogListener,
        DelVehicleConfDialogFragment.confirmDelDialogListener {

    private final static String TAG = "cfbrownweb"; //debug tag

    private final String allVehiclesUrl = "http://cfbrownweb.ngrok.io/fuel/getAllVehicles.php";
    private final String deleteVehiclesUrl = "http://cfbrownweb.ngrok.io/fuel/deleteVehicles.php";

    private Menu optionsMenu;
    private RelativeLayout vehiclesContent;
    private ListView vehicleListView;
    protected static ArrayList<Vehicle> vehiclesAL; //allow access from other activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicles);

        //Initial setup
        setTitle(getResources().getString(R.string.vehicles_title));
        vehiclesContent = (RelativeLayout) findViewById(R.id.vehicles_content_layout);
        vehiclesAL = new ArrayList<Vehicle>();
        vehicleListView = (ListView) findViewById(R.id.vehicle_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get all of the vehicles and display
        allVehiclesReq();
    }

    public void allVehiclesReq() {
        RequestQueue queue = Volley.newRequestQueue(VehiclesActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, allVehiclesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Reset refresh icon
                        Utils.setRefreshIconState(false, optionsMenu);

                        TextView noRecordsTv = (TextView) findViewById(R.id.no_vehicles_message);

                        if (response.equals("0")) {
                            vehiclesAL.clear();
                            vehicleListView.setVisibility(View.GONE);

                            //No Vehicles - Display message
                            noRecordsTv.setText(getResources().getString(R.string.no_vehicles));
                            noRecordsTv.setVisibility(View.VISIBLE);

                            //Disable delete vehicle menu item
                            optionsMenu.findItem(R.id.vehicles_menu_delete_vehicle).setEnabled(false);
                        } else {
                            //Hide no vehicles message
                            noRecordsTv.setVisibility(View.GONE);
                            //Enable delete vehicle menu item
                            optionsMenu.findItem(R.id.vehicles_menu_delete_vehicle).setEnabled(true);

                            //Store all vehicles in arrayList
                            if(storeVehiclesLocally(response)) {
                                for(Vehicle v : vehiclesAL){
                                    Log.i(TAG, v.getPlate());
                                }
                                //Add all returned vehicles to the list view
                                ListAdapter vehicleAdaptor = new VehicleAdapter(VehiclesActivity.this, vehiclesAL);
                                vehicleListView.setAdapter(vehicleAdaptor);

                                //Click listener on each item
                                vehicleListView.setOnItemClickListener(
                                    new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            //Get the value of the position clicked
                                            String plate = vehiclesAL.get(position).getPlate();
                                            String name = vehiclesAL.get(position).getName();
                                            goToOverview(plate, name);
                                        }
                                    }
                                );
                                //Show the listview
                                vehicleListView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null){
                    //Network error
                    Utils.netErrorToast(VehiclesActivity.this);
                }
                else {
                    //A different error
                    Utils.serverErrorToast(VehiclesActivity.this);
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user", Configuration.getConfig().getUser().getUsername());

                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void deleteVehicles(final ArrayList<Vehicle> vehiclesToDelete) {
        for(Vehicle v : vehiclesToDelete){
            Log.i(TAG,"Vehicles to delete: " + v.getPlate());
        }
        RequestQueue queue = Volley.newRequestQueue(VehiclesActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, deleteVehiclesUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Check for success
                        if(response.equals("1")){

                            //Find the vehicle in array list to delete
                            for(Vehicle vehicle : vehiclesToDelete){
                                //For every vehicle to delete...
                                for(int i = 0; i < vehiclesAL.size(); i++){
                                    //...loop over vehicles and find it
                                    if(vehiclesAL.get(i).getPlate().equals(vehicle.getPlate())){
                                        //Found vehicle - Delete it from arrayList
                                        vehiclesAL.remove(i);
                                    }
                                }
                            }

                            //Reload the vehicles list
                            allVehiclesReq();
                        }
                        else {
                            //Something went wrong
                            Utils.defaultErrorToast(VehiclesActivity.this);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse == null){
                    //Network error
                    Utils.netErrorToast(VehiclesActivity.this);
                }
                else {
                    //A different error
                    Utils.defaultErrorToast(VehiclesActivity.this);
                }
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user", Configuration.getConfig().getUser().getUsername());

                for(int i = 0; i < vehiclesToDelete.size(); i++){
                    Log.i(TAG, "Param: " + vehiclesToDelete.get(i).getPlate());
                    params.put("plates["+i+"]", vehiclesToDelete.get(i).getPlate());
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

        Intent intent = new Intent(this, ConnectionActivity.class);
        intent.putExtra("dest", OverviewActivity.class);
        startActivity(intent);
    }

    private boolean storeVehiclesLocally(String vehicles){
        vehiclesAL.clear();
        Log.i(TAG, vehicles);
        try {
            JSONArray vehiclesjArray = new JSONArray(vehicles);
            for(int i = 0; i < vehiclesjArray.length(); i++){
                //Get vehicle
                JSONObject vehicle = vehiclesjArray.getJSONObject(i);

                //Create Vehicle object and store
                vehiclesAL.add(new Vehicle(vehicle.getString("plate"), vehicle.getString("name")));
            }
        }
        catch (JSONException e) {
            //JSON error - server-side
            Utils.serverErrorToast(this);
            return false;
        }
        return true;
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, ArrayList<Vehicle> selectedItems) {
        //Are you sure? - Confirmation
        DelVehicleConfDialogFragment delConf = new DelVehicleConfDialogFragment();
        delConf.setItems(selectedItems);

        //Show confirmation dialog
        delConf.show(getFragmentManager(), "DelConfirmationDialog");
    }

    @Override
    public void onDialogDeleteClick(DialogFragment dialog, ArrayList<Vehicle> selectedItems) {
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
        this.optionsMenu = menu;
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
            case R.id.vehicles_menu_delete_vehicle: //TODO disable me
                VehicleDeleteDialogFragment deleteDialogFragment = new VehicleDeleteDialogFragment();
                deleteDialogFragment.setItems(vehiclesAL);
                deleteDialogFragment.show(getFragmentManager(), "deleteVehicleDialog");
                return true;
            case R.id.vehicles_menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.menu_refresh:
                Utils.setRefreshIconState(true, optionsMenu);
                allVehiclesReq();
                return true;
            case R.id.menu_sign_out:
                //Remove user from shared prefs
                SharedPreferences settings = Configuration.getConfig().getSharedPrefs(this);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("user", null);
                editor.apply();
                //Navigate to the login screen
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        if(vehicleListView != null){
            vehicleListView.invalidateViews();
        }
        super.onResume();
    }
}