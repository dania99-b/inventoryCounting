package com.example.inventorycountingsystem.DataClasses;

public class Items {
    public  String countName;
    public  String item_code;
    public  String item_name; //Asset or Material
    public  String barcode;
    public  String type;

    public String getCountName() {
        return countName;
    }

    public void setCountName(String countName) {
        this.countName = countName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Items(){}
    public void setItem_code(String item_code){
        this.item_code = item_code;

    }
    public void setItem_name(String item_name){
        this.item_name = item_name;
    }


    public String getBarcode() {
        return this.barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getItem_code(){
        return this.item_code;
    }
    public String getItem_name(){
        return this.item_name;
    }



}
