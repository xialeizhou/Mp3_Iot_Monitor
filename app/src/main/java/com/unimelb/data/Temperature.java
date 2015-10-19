package com.unimelb.data;

/**
 * Created by xialeizhou on 10/10/15.
 */
public class Temperature implements Record {
    private String date = null;
    private float value = 0.f;
    public Temperature(String date, float value) {
        this.date = date;
        this.value = value;
    }
    public String getDate() {
        return this.date;
    }
    public float getValue() {
        return this.value;
    }
}
