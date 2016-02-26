package com.cfbrownweb.fuelmemo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

class VehicleAdapter extends ArrayAdapter<String> {

    ArrayList<Vehicle> vehicles;
    private Context context;

    VehicleAdapter(Context context, ArrayList<Vehicle> vehicles) {
        super(context, R.layout.custom_row, new String[vehicles.size()]);
        this.vehicles = vehicles;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //How do we want to lay it out
        //Ready the layout/get it set up with an inflater
        LayoutInflater myInflater = LayoutInflater.from(getContext());
        View customView = myInflater.inflate(R.layout.custom_row, parent, false);
        RelativeLayout rowLayout = (RelativeLayout) customView.findViewById(R.id.custom_row_layout);

        //Set background colour
        SharedPreferences settings = Configuration.getConfig().getSharedPrefs(context);
        int tileColourIndex = settings.getInt("tileColour", 0);
        rowLayout.setBackgroundResource(Configuration.getConfig().getTileColour(tileColourIndex));

        //Set the various elements of the row view
        String plate = vehicles.get(position).getPlate();
        String name = vehicles.get(position).getName();
        Log.i("cfbrownweb", "here 1");
        TextView rowPlate = (TextView) customView.findViewById(R.id.row_plate);
        TextView rowVehicleName = (TextView) customView.findViewById(R.id.row_vehicle_name);

        rowPlate.setText(plate.toUpperCase());
        rowVehicleName.setText(name);
        return customView;
    }
}
