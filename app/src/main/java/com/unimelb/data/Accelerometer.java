package com.unimelb.data;

/**
 * Created by xialeizhou on 10/10/15.
 */
public class Accelerometer implements Record {
    private String date = null;
    private String value = null;
    public Accelerometer(String date, String value) {
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
