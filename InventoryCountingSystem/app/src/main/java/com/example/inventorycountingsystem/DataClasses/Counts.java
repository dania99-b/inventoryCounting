package com.example.inventorycountingsystem.DataClasses;

import java.util.ArrayList;

public class Counts {
    public String count_name;
    public String type;
    public ArrayList <Locations>location;
    public String start_date;
    public ArrayList<Warehouse> warehouse;
    public String department;
    public int number_item;
    public int counted_items;
    public ArrayList<Items> items;


    public Counts() {
    }

    public void setCount_name(String count_name) {
        this.count_name = count_name; // Corrected assignment
    }
    public void setType(String type) {
        this.type = type; // Corrected assignment
    }

    public ArrayList<Locations> getLocation() {
        return location;
    }

    public void setLocation(ArrayList<Locations> location) {
        this.location = location;
    }

    public ArrayList<Warehouse> getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(ArrayList<Warehouse> warehouse) {
        this.warehouse = warehouse;
    }

    public int getNumber_item() {
        return number_item;
    }

    public void setNumber_item(int number_item) {
        this.number_item = number_item;
    }

    public ArrayList<Items> getItems() {
        return items;
    }

    public void setItems(ArrayList<Items> items) {
        this.items = items;
    }

    public void setItem_number(int number_item) {
        this.number_item = number_item; // Corrected assignment
    }
    public void setCounted_items(int counted_items) {
        this.counted_items = counted_items; // Corrected assignment
    }
    public void setDepartment(String department) {
        this.department = department; // Corrected assignment
    }
    public void setStart_date(String start_date) {
        this.start_date = start_date; // Corrected assignment
    }


    public String getCount_name() {
        return count_name;
    }
    public String getType() {
        return type;
    }

    public String getStart_date() {
        return start_date;
    }

    public String getDepartment() {
        return department;
    }
    public int getItem_number() {
        return number_item;
    }
    public int getCounted_items() {
        return counted_items;
    }
}