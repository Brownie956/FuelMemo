/*Author: Chris Brown
*Date: 21/02/2016
*Description: A class to represent a vehicle */
package com.cfbrownweb.fuelmemo;

public class Vehicle {
    private String plate;
    private String name;

    public Vehicle(String plate, String name){
        this.plate = plate;
        this.name = name;
    }

    public String getPlate() {
        return plate;
    }

    public String getName() {
        return name;
    }
}
