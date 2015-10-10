package com.unimelb.data;

/**
 * Created by xialeizhou on 10/10/15.
 */
public class Temperature implements Record {
    private String date = null;
    private String value = null;
    public Temperature(String date, String value) {
        this.date = date;
        this.value = value;
    }
    public String getDate() {
        return this.date;
    }
    public String getValue() {
        return this.value;
    }
}
