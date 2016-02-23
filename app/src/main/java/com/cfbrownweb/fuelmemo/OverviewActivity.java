/*Author: Chris Brown
*Date: 21/02/2016
*Description: Activity to show an overview of the current vehicle
*Shows all records submitted for that vehicle and has functionality to
*submit records for the current vehicle */
package com.cfbrownweb.fuelmemo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class OverviewActivity extends AppCompatActivity implements MaxRecordsDialogFragment.MaxRecordsDialogListener {

    private final static String TAG = "cfbrownweb"; //debug tag

    private final String lastNRecordsUrl = "http://cfbrownweb.ngrok.io/fuel/getLastNRecordsByPlate.php";
    private final String addRecordUrl = "http://cfbrownweb.ngrok.io/fuel/addRecord.php";
    private final String getNumberOfRecordsUrl = "http://cfbrownweb.ngrok.io/fuel/getNumberOfRecords.php";
    private final String removeOldestRecordUrl = "http://cfbrownweb.ngrok.io/fuel/deleteOldestRecord.php";

    private RelativeLayout overviewLayout;
    private String plate = "";
    private String name = "";
    private final int maxRecords = 20;
    private String miles;
    private String cost;
    private static TextView dateInput;
    private EditText milesInput;
    private EditText costInput;
    private static int year, month, day;
    private boolean nRecordsComputed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        //Reset date values
        year = 0;
        month = 0;
        day = 0;
        overviewLayout = (RelativeLayout) findViewById(R.id.overview_content_layout);
        dateInput = (TextView) findViewById(R.id.date_input);
        milesInput = (EditText) findViewById(R.id.miles_input);
        costInput = (EditText) findViewById(R.id.cost_input);
        plate = Configuration.getConfig().getVehicle().getPlate();
        name = Configuration.getConfig().getVehicle().getName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set titles
        TextView nameTitle = (TextView) findViewById(R.id.overview_name_title);
        setTitle(plate.toUpperCase());
        nameTitle.setText(name);

        lastNRecordsReq(String.valueOf(getLimit()));

        nameTitle.requestFocus();
        milesInput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        costInput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
    }

    public void lastNRecordsReq(final String limit) {
        Log.i(TAG, "IN LAST N RECORDS REQ");
        RequestQueue queue = Volley.newRequestQueue(OverviewActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, lastNRecordsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response);
                        ScrollView nRecordsScroll = (ScrollView) findViewById(R.id.last_n_records_scroll);
                        if(response.equals("-1")){
                            //Display no records message
                            TextView noRecordsTv = new TextView(OverviewActivity.this);
                            noRecordsTv.setText(getResources().getString(R.string.no_records));
                            nRecordsScroll.addView(noRecordsTv);
                        }
                        else {
                            //Display heading
                            TextView heading = (TextView) findViewById(R.id.last_n_records_heading);
                            heading.setVisibility(View.VISIBLE);

                            //Generate table from data
                            nRecordsScroll.addView(parseJSONArray(response));
                            nRecordsComputed = true;
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
                params.put("limit",limit);

                return params;
            }
        };
        queue.add(stringRequest);
    }

    private TableLayout parseJSONArray(String jString){
        TableLayout table = new TableLayout(this);
        //Add column names
        String[] columns = new String[]{"Date", "Miles", "Cost", "£ / 10 miles"};
        table.addView(genColRow(columns));

        try {
            JSONArray jsonArray = new JSONArray(jString);
            for(int i = 0; i < jsonArray.length(); i++){
                //New Row
                TableRow tableRow = new TableRow(this);

                //Text views for the row
                TextView dateTv = new TextView(this);
                TextView milesTv = new TextView(this);
                TextView costTv = new TextView(this);
                TextView perTenMileTv = new TextView(this);

                //Get all of the data for the row
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String date = jsonObject.getString("date");
                String miles = jsonObject.getString("miles");
                String cost = jsonObject.getString("cost");
                String formattedCost = "£" + cost;

                //Calculate cost per 10 miles
                double perTenMileDouble = Double.parseDouble(cost) / Double.parseDouble(miles);
                perTenMileDouble = perTenMileDouble * 10;
                perTenMileDouble = round(perTenMileDouble, 2);
                String perTenMileString = String.valueOf(perTenMileDouble);
                //Append any necessary 0s
                while(perTenMileString.substring(perTenMileString.indexOf(".")).length() < 3){
                    perTenMileString += "0";
                }
                //Add the £ sign
                perTenMileString = "£" + perTenMileString;

                //Set all of the text in TextViews
                dateTv.setText(date);
                milesTv.setText(miles);
                costTv.setText(formattedCost);
                perTenMileTv.setText(perTenMileString);

                //Set text size
                int textSize = 15;
                dateTv.setTextSize(textSize);
                milesTv.setTextSize(textSize);
                costTv.setTextSize(textSize);
                perTenMileTv.setTextSize(textSize);

                //Set all padding
                int cellPadding = 20;
                dateTv.setPadding(cellPadding,cellPadding,cellPadding,cellPadding);
                milesTv.setPadding(cellPadding,cellPadding,cellPadding,cellPadding);
                costTv.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
                perTenMileTv.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);

                //Centre all text
                dateTv.setGravity(Gravity.CENTER);
                milesTv.setGravity(Gravity.CENTER);
                costTv.setGravity(Gravity.CENTER);
                perTenMileTv.setGravity(Gravity.CENTER);

                //Add all textViews to row
                tableRow.addView(dateTv);
                tableRow.addView(milesTv);
                tableRow.addView(costTv);
                tableRow.addView(perTenMileTv);

                if(i % 2 == 0){
                    tableRow.setBackgroundColor(ContextCompat.getColor(this, R.color.pale_grey));
                }
                else {
                    tableRow.setBackgroundColor(ContextCompat.getColor(this, R.color.grey));
                }

                //Add row to the table
                table.addView(tableRow);
            }
        }
        catch (JSONException e){
            //TODO Handle Exception
        }
        return table;
    }

    /*Function taken from http://stackoverflow.com/questions/2808535/round-a-double-to-2-decimal-places*/
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private TableRow genColRow(String[] cols){
        TableRow colRow = new TableRow(this);
        for(String col : cols){
            //For all columns required, make and style a text view and add to row
            TextView colName = new TextView(this);
            colName.setText(col);
            colName.setTextSize(15);
            colName.setGravity(Gravity.CENTER);
            int cellPadding = 20;
            colName.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);

            colRow.addView(colName);
        }
        return colRow;
    }


    /*Date Picker Code*/
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog dialog = new DatePickerDialog(getActivity(), this, year, month, day);
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            return dialog;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day){
            //Can't pick a date in the future
            GregorianCalendar selected = new GregorianCalendar(year, month,day);
            if(selected.after(Calendar.getInstance())){
                Toast.makeText(getActivity(),"Can't pick a date in the future", Toast.LENGTH_LONG).show();
            }
            else {
                //Update editText
                OverviewActivity.year = year;
                OverviewActivity.month = month + 1; //months start at 0
                OverviewActivity.day = day;
                String formattedDate = OverviewActivity.day + "/" + OverviewActivity.month + "/" + OverviewActivity.year;
                dateInput.setText(formattedDate);
            }
        }

    }

    public void showDatePicker(View view){
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    private void submitRecord(){
        Log.i(TAG,"IN SUBMIT RECORD");
        final String date = parseDate(year, month, day, "-");


        RequestQueue queue = Volley.newRequestQueue(OverviewActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, addRecordUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response);
                        if (response.equals("1")) {
                            //Clear inputs
                            dateInput.setText("");
                            year = 0;
                            month = 0;
                            day = 0;
                            milesInput.getText().clear();
                            costInput.getText().clear();

                            //Update scroll elements - Remove all then recompute
                            clearNRecordsScroll();

                            lastNRecordsReq(String.valueOf(getLimit()));
                        } else {
                            //Something went wrong
                            Toast.makeText(OverviewActivity.this, "Oops, Something went wrong, please try again", Toast.LENGTH_LONG).show();
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
                params.put("plate", plate);
                params.put("date", date);
                params.put("miles", miles);
                params.put("cost", cost);

                return params;
            }
        };
        queue.add(stringRequest);
    }

    private String parseDate(int year, int month, int day, String delimeter){
        Log.i(TAG, "IN PARSE DATE");
        return String.valueOf(year + delimeter + month + delimeter + day);
    }

    public void checkLimitAndSubmit(View view){
        //Get all inputs
        miles = milesInput.getText().toString();
        cost = costInput.getText().toString();

        //Catch input errors
        if (year == 0 || month == 0 || day == 0) {
            Toast.makeText(this, getString(R.string.date_input_error), Toast.LENGTH_LONG).show();
        }
        else if (miles.length() < 1 || miles.endsWith(".")) {
            Toast.makeText(this, getString(R.string.miles_input_error), Toast.LENGTH_LONG).show();
        }
        else if (cost.length() < 1 || cost.endsWith(".")) {
            Toast.makeText(this, getString(R.string.cost_input_error), Toast.LENGTH_LONG).show();
        }
        else {
            RequestQueue queue = Volley.newRequestQueue(OverviewActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, getNumberOfRecordsUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "Number of Records: " + response);
                            try {
                                int numberOfRecords = Integer.parseInt(response);

                                //Max number of records met?
                                if (numberOfRecords >= maxRecords) {
                                    //Hide keyboard
                                    // Check if no view has focus:
                                    View view = OverviewActivity.this.getCurrentFocus();
                                    if (view != null) {
                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                    }

                                    //Need to delete oldest record - show alert dialog
                                    MaxRecordsDialogFragment maxRecordsDialogFragment = new MaxRecordsDialogFragment();
                                    maxRecordsDialogFragment.show(getFragmentManager(), "MaxRecordsReachedDialog");
                                } else {
                                    submitRecord();
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
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("plate", plate);

                    return params;
                }
            };
            queue.add(stringRequest);
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //Remove oldest record and store new one
        removeOldestRecordAndSubmit();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //Notify user that submission is cancelled
        Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
    }

    private void removeOldestRecordAndSubmit(){
        Log.i(TAG, "IN REMOVE OLDEST");
        RequestQueue queue = Volley.newRequestQueue(OverviewActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, removeOldestRecordUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Check for correct response
                        if (response.equals("1")) {
                            //Record deleted - Submit new record
                            submitRecord();
                        } else {
                            //Something went wrong
                            Toast.makeText(OverviewActivity.this, "Oops, Something went wrong, please try again", Toast.LENGTH_LONG).show();
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

                return params;
            }
        };
        queue.add(stringRequest);
    }

    private int getLimit(){
        SharedPreferences settings = Configuration.getConfig().getSharedPrefs(this);
        int nRecordsLimit = settings.getInt("nRecords", 3);
        return nRecordsLimit;
    }

    private void clearNRecordsScroll(){
        ScrollView nRecordsScroll = (ScrollView) findViewById(R.id.last_n_records_scroll);
        nRecordsScroll.removeAllViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.overview_menu_all_records:
                Intent allRecIntent = new Intent(this, AllRecordsActivity.class);
                startActivity(allRecIntent);
                return true;
            case R.id.overview_menu_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        //Only recompute if it's not the first time
        //onCreate will compute it for the first time
        if(nRecordsComputed){
            //Clear Scroll and Recompute
            clearNRecordsScroll();
            lastNRecordsReq(String.valueOf(getLimit()));
        }
        super.onResume();
    }
}
