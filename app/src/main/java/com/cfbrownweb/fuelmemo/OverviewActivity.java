package com.cfbrownweb.fuelmemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
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

public class OverviewActivity extends AppCompatActivity {

    private final static String TAG = "cfbrownweb"; //debug tag

    private final String lastNRecordsUrl = "http://cfbrownweb.ngrok.io/fuel/getLastNRecordsByPlate.php";

    private RelativeLayout overviewLayout;
    private String plate = "";
    private final String limit = "40";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        overviewLayout = (RelativeLayout) findViewById(R.id.overview_content_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Set titles
        TextView nameTitle = (TextView) findViewById(R.id.overview_name_title);
        plate = getIntent().getStringExtra(VehiclesActivity.EXTRA_PLATE);
        String name = getIntent().getStringExtra(VehiclesActivity.EXTRA_NAME);

        setTitle(plate.toUpperCase());
        nameTitle.setText(name);

        lastNRecordsReq();

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

}
