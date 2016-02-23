/*Author: Chris Brown
*Date: 21/02/2016
*Description: A class to store configuration setting */
package com.cfbrownweb.fuelmemo;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;

public class Configuration {
    private static Configuration sessionInstance = new Configuration();

    public static Configuration getConfig() {
        return sessionInstance;
    }

    private Vehicle vehicle;
    private ArrayList<Integer> tileColourIds;

    private Configuration(){
        //set available colour ids
        this.tileColourIds = new ArrayList<Integer>();
        tileColourIds.add(R.color.tile_grey);
        tileColourIds.add(R.color.tile_blue);
        tileColourIds.add(R.color.tile_green);
        tileColourIds.add(R.color.tile_red);
    }

    public void setVehicle(Vehicle vehicle){
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle(){
        return vehicle;
    }

    public ArrayList<Integer> getTileColourIds(){
        return tileColourIds;
    }

    public int getTileColour(int index){
        return tileColourIds.get(index);
    }

    public SharedPreferences getSharedPrefs(Context context){
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

}
