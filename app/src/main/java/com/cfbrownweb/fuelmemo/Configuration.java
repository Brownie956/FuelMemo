/*Author: Chris Brown
*Date: 21/02/2016
*Description: A class to store configuration setting */
package com.cfbrownweb.fuelmemo;

import android.content.Context;
import android.content.SharedPreferences;

public class Configuration {
    private static Configuration sessionInstance = new Configuration();

    public static Configuration getConfig() {
        return sessionInstance;
    }

    private Vehicle vehicle;

    private Configuration(){
    }

    public void setVehicle(Vehicle vehicle){
        this.vehicle = vehicle;
    }

    public Vehicle getVehicle(){
        return vehicle;
    }

    public SharedPreferences getSharedPrefs(Context context){
        return context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

}
