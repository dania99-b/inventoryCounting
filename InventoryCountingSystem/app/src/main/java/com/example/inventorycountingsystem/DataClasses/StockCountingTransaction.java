package com.example.inventorycountingsystem.DataClasses;

public class StockCountingTransaction {
        int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

        String item_code;
        String count_name;
        double quantity;
        String posting_date_time;
        String conter_name;
        int is_corrective;
        String stage;
        int is_synced;
        String warehouse_id;
        String location_id;
        String type;
        String sync_time;
        String item_site;
        String job_id;
        String device_mac;

    public String getDevice_mac() {
        return device_mac;
    }

    public void setDevice_mac(String device_mac) {
        this.device_mac = device_mac;
    }

    public String getJob_id() {
        return job_id;
    }

    public void setJob_id(String job_id) {
        this.job_id = job_id;
    }

    public String getItem_site() {
        return item_site;
    }

    public void setItem_site(String item_site) {
        this.item_site = item_site;
    }

    public String getSync_time() {
        return sync_time;
    }

    public void setSync_time(String sync_time) {
        this.sync_time = sync_time;
    }

    public int getIs_synced() {
        return is_synced;
    }

    public void setIs_synced(int is_synced) {
        this.is_synced = is_synced;
    }

    public String getItem_code() {
        return item_code;
    }

    public String getCount_name() {
        return count_name;
    }

    public void setCount_name(String count_name) {
        this.count_name = count_name;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setItem_code(String item_code) {
        this.item_code = item_code;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public String getPosting_date_time() {
        return posting_date_time;
    }

    public void setPosting_date_time(String posting_date_time) {
        this.posting_date_time = posting_date_time;
    }

    public String getConter_name() {
        return conter_name;
    }

    public void setConter_name(String conter_name) {
        this.conter_name = conter_name;
    }

    public int getIs_corrective() {
        return is_corrective;
    }

    public void setIs_corrective(int is_corrective) {
        this.is_corrective = is_corrective;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String getWarehouse_id() {
        return warehouse_id;
    }

    public void setWarehouse_id(String warehouse_id) {
        this.warehouse_id = warehouse_id;
    }

    public String getLocation_id() {
        return location_id;
    }

    public void setLocation_id(String location_id) {
        this.location_id = location_id;

    }


    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }
    }

