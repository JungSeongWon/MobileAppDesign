package com.scjeon.costom;

/**
 * Created by USER on 2018-05-05.
 */
// 현재 위치의 위도 및 경도를 저장하기 위한 클래스
public class GPSInfo {
    private String lt;
    private String lg;


    public String getLt() {
        return lt;
    }

    public void setLt(String lt) {
        this.lt = lt;
    }

    public String getLg() {
        return lg;
    }

    public void setLg(String lg) {
        this.lg = lg;
    }
}
