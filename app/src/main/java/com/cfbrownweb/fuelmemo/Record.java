/*Author: Chris Brown
*Date: 22/02/2016
*Description: A class to represent a record */
package com.cfbrownweb.fuelmemo;

public class Record {
    private Vehicle vehicle;
    private String date;
    private double miles;
    private double cost;

    public Record(Vehicle vehicle, String date, double miles, double cost){
        this.vehicle = vehicle;
        this.date = date;
        this.miles = miles;
        this.cost = cost;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getDate() {
        return date;
    }

    public double getMiles() {
        return miles;
    }

    public double getCost() {
        return cost;
    }
}
