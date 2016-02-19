package com.cfbrownweb.fuelmemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class OverviewActivity extends AppCompatActivity {

    private final static String TAG = "cfbrownweb"; //debug tag

    private final String lastNRecordsUrl = "http://cfbrownweb.ngrok.io/fuel/getLastNRecordsByPlate.php";

    private String plate = "";
    private final String limit = "4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
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
                        TextView nRecords = (TextView) findViewById(R.id.last_n_records);
                        nRecords.setText(response);
//                        responseString = response;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                TextView nRecords = (TextView) findViewById(R.id.last_n_records);
                nRecords.setText("Something went wrong");
//                responseString = "Something went wrong";
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

}
