package com.cfbrownweb.fuelmemo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class OverviewActivity extends AppCompatActivity {

    private final static String TAG = "cfbrownweb"; //debug tag

    private final String lastNRecordsUrl = "http://cfbrownweb.ngrok.io/fuel/getLastNRecordsByPlate.php";
    private final String addRecordUrl = "http://cfbrownweb.ngrok.io/fuel/addRecord.php";

    private RelativeLayout overviewLayout;
    private String plate = "";
    private final String limit = "40";
    private String miles;
    private String cost;
    private static TextView dateInput;
    private EditText milesInput;
    private EditText costInput;
    private static int year, month, day;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set titles
        TextView nameTitle = (TextView) findViewById(R.id.overview_name_title);
        plate = getIntent().getStringExtra(VehiclesActivity.EXTRA_PLATE);
        String name = getIntent().getStringExtra(VehiclesActivity.EXTRA_NAME);

        setTitle(plate.toUpperCase());
        nameTitle.setText(name);

        lastNRecordsReq();

        nameTitle.requestFocus();
        milesInput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
        costInput.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(4, 2)});
    }

    public void lastNRecordsReq() {
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
        String[] colunms = new String[]{"Date", "Miles", "Cost", "£ / 10 miles"};
        table.addView(genColRow(colunms));

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
                OverviewActivity.month = month;
                OverviewActivity.day = day;
                dateInput.setText(day + "/" + month + "/" + year);
            }
        }

    }

    public void showDatePicker(View view){
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void submitRecord(View view){
        //Get all inputs
        String miles = milesInput.getText().toString();
        String cost = costInput.getText().toString();

        //Catch input errors
        if(year == 0 || month == 0 || year == 0){
            Toast.makeText(this, getString(R.string.date_input_error), Toast.LENGTH_LONG).show();
        }
        else if(miles.length() < 1 || miles.endsWith(".")) {
            Toast.makeText(this, getString(R.string.miles_input_error), Toast.LENGTH_LONG).show();
        }
        else if(cost.length() < 1 || cost.endsWith(".")){
            Toast.makeText(this, getString(R.string.cost_input_error), Toast.LENGTH_LONG).show();
        }
        else {
            String date = parseDate(year, month, day, "-");

            /*RequestQueue queue = Volley.newRequestQueue(OverviewActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, addRecordUrl,
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
                    params.put("date",limit);
                    params.put("miles",limit);
                    params.put("cost",limit);

                    return params;
                }
            };
            queue.add(stringRequest);*/
        }
    }

    private String parseDate(int year, int month, int day, String delimeter){
        return String.valueOf(year + delimeter + month + delimeter + day);
    }
}
