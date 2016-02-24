/*Author: Chris Brown
* Date: 23/02/2016
* Description: Launcher activity that checks the network connection
* before loading the vehicles page. Can also be used for a prerequisite
* connection check*/
package com.cfbrownweb.fuelmemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.io.Serializable;

public class ConnectionActivity extends AppCompatActivity {

    private Class<?> destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Where are you trying to go?
        Serializable dest = getIntent().getSerializableExtra("dest");
        if(dest == null){
            //Only on start up
            destination = VehiclesActivity.class;
        }
        else {
            destination = (Class) dest;
        }

        if(Utils.isConnected(this)){
            //Forward them to destination
            Intent intent = new Intent(this, destination);
            startActivity(intent);
            finish();
        }
        else {
            //Display connection error
            setContentView(R.layout.activity_no_connection);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            setTitle(getString(R.string.no_connection_title));
        }
    }

    public void retryNet(View view){
        if(Utils.isConnected(this)){
            Intent intent = new Intent(this, destination);
            //Forward to destination and close connection prompt page
            startActivity(intent);
            this.finish();
        }
        else {
            //Notify the user
            Utils.netErrorToast(this);
        }
    }

}
