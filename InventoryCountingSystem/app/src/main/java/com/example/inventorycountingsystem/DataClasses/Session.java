package com.example.inventorycountingsystem.DataClasses;

public class Session {
    public  int id;
    public  String counter_name;
    public  String open_date;
    public  String status;
    public  String api_key;

    public String getApi_key() {
        return api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public String getApi_secret() {
        return api_secret;
    }

    public void setApi_secret(String api_secret) {
        this.api_secret = api_secret;
    }

    public  String api_secret;


    public  int getId() {
        return this.id;
    }

    public  void setId(int id) {
        this.id = id;
    }

    public  String getCounter_name() {
        return this.counter_name;
    }

    public  void setCounter_name(String counter_name) {
        this.counter_name = counter_name;
    }

    public  String getOpen_date() {
        return this.open_date;
    }

    public  void setOpen_date(String open_date) {
        this.open_date = open_date;
    }
    public  void setStatus(String status) {
        this.status = status;
    }
    public  String getStatus() {
        return this.status;
    }
}
