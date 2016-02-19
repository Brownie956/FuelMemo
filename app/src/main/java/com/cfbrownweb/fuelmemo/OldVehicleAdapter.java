package com.cfbrownweb.fuelmemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class OldVehicleAdapter extends ArrayAdapter<String> {

    OldVehicleAdapter(Context context, String[] cars) {
        super(context, R.layout.custom_row, cars);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //How do we want to lay it out
        //Ready the layout/get it set up with an inflater
        LayoutInflater myInflater = LayoutInflater.from(getContext());
        View customView = myInflater.inflate(R.layout.custom_row, parent, false);

        //Set the various elements of the row view
        String singleCarItem = getItem(position);
        TextView rowPlate = (TextView) customView.findViewById(R.id.row_plate);
        TextView rowVehicleName = (TextView) customView.findViewById(R.id.row_vehicle_name);

        //TODO Change this to correct text
        rowPlate.setText(singleCarItem);
        rowVehicleName.setText(singleCarItem);
        return customView;
    }
}
