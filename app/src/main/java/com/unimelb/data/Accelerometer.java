package com.unimelb.data;

/**
 * Created by xialeizhou on 10/10/15.
 */
public class Accelerometer implements Record {
    private float date = 0.f;
    private float value = 0.f;
    public Accelerometer(float date, float value) {
        this.date = date;
        this.value = value;
    }
    public float getDate() {
        return this.date;
    }
    public float getValue() {
        return this.value;
    }
}
