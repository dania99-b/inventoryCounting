package com.example.inventorycountingsystem.DataClasses;

public class Locations {
    public  String location_name;
    public  String barcode;

    public Locations(){

    }

    public  String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public  String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }
}
