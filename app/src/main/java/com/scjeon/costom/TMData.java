package com.scjeon.costom;

/**
 * Created by USER on 2018-05-14.
 */

public class TMData {
    private String address;
    private String tm_x;
    private String tm_y;

    public TMData(String address, String tm_x, String tm_y) {
        this.address = address;
        this.tm_x = tm_x;
        this.tm_y = tm_y;
    }

    public String getAddress() {
        return address;
    }

    public String getTm_x() {
        return tm_x;
    }

    public String getTm_y() {
        return tm_y;
    }
}
