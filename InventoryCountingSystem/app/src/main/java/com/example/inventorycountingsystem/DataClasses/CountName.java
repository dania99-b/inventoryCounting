package com.example.inventorycountingsystem.DataClasses;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import java.security.PublicKey;

public class CountName {
    public String count_name;
    public String type;
    public int manual_item_check;
    public int manual_site_check;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getManual_item_check() {
        return manual_item_check;
    }

    public void setManual_item_check(int manual_item_check) {
        this.manual_item_check = manual_item_check;
    }

    public int getManual_site_check() {
        return manual_site_check;
    }

    public void setManual_site_check(int manual_site_check) {
        this.manual_site_check = manual_site_check;
    }

    public void setName(String name) {
        this.count_name = name; // Corrected assignment
    }

    public String getName() {
        return count_name;
    }
}
