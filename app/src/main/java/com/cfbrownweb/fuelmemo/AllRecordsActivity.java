/*Author: Chris Brown
*Date: 21/02/2016
*Description: Activity that displays all of the records for the current vehicle*/
package com.cfbrownweb.fuelmemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.Map;

public class AllRecordsActivity extends AppCompatActivity {

    private static final String TAG = "cfbrownweb";

    private final String getAllRecordsUrl = "http://cfbrownweb.ngrok.io/fuel/getAllRecordsByPlate.php";
    private String plate = "";
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_records);

        plate = Configuration.getConfig().getVehicle().getPlate();
        name = Configuration.getConfig().getVehicle().getName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set titles
        TextView nameTitle = (TextView) findViewById(R.id.all_records_name_title);
        setTitle(plate.toUpperCase());
        nameTitle.setText(name);

        //Get all records for that vehicle
        allRecordsReq();

    }

    public void allRecordsReq() {
        RequestQueue queue = Volley.newRequestQueue(AllRecordsActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, getAllRecordsUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, response);
                        ScrollView allRecordsScroll = (ScrollView) findViewById(R.id.all_records_scroll);
                        if(response.equals("-1")){
                            //Display no records message
                            TextView noRecordsTv = new TextView(AllRecordsActivity.this);
                            noRecordsTv.setText(getString(R.string.no_records));
                            allRecordsScroll.addView(noRecordsTv);
                        }
                        else {
                            //Display heading
                            TextView heading = (TextView) findViewById(R.id.all_records_heading);
                            heading.setVisibility(View.VISIBLE);

                            //Generate table from data
                            allRecordsScroll.addView(parseJSONArray(response));
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_all_records, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.all_records_menu_del_records:
                return true;
            case R.id.all_records_menu_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}