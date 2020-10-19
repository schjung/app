package com.menupan.translate.apps.java.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class ZhTc {

    public String zh;
    public String en;
    public String ko;
    public String upyn;
    public String imgurl;

    /**
     * Empty constructor needed for Firebase object deserialization.
     */
    public ZhTc() {
    }

    public ZhTc(String zh, String en, String ko, String upyn, String imgurl) {
        this.zh = zh;
        this.en = en;
        this.ko = ko;
        this.imgurl = imgurl;
        this.upyn = upyn;
    }
}
