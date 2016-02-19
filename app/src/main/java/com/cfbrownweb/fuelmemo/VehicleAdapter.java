package com.cfbrownweb.fuelmemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

class VehicleAdapter extends ArrayAdapter<String> {

    JSONArray jArray;

    VehicleAdapter(Context context, JSONArray jsonArray) {
        super(context, R.layout.custom_row, new String[jsonArray.length()]);
        this.jArray = jsonArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //How do we want to lay it out
        //Ready the layout/get it set up with an inflater
        LayoutInflater myInflater = LayoutInflater.from(getContext());
        View customView = myInflater.inflate(R.layout.custom_row, parent, false);

        //Set the various elements of the row view
        String plate = "";
        String name = "";
        try {
            plate = jArray.getJSONObject(position).getString("plate");
            name = jArray.getJSONObject(position).getString("name");
        }
        catch (JSONException e){
            //TODO Handle Excepetion
        }
        TextView rowPlate = (TextView) customView.findViewById(R.id.row_plate);
        TextView rowVehicleName = (TextView) customView.findViewById(R.id.row_vehicle_name);

        rowPlate.setText(plate.toUpperCase());
        rowVehicleName.setText(name);
        return customView;
    }
}
