package com.menupan.translate.apps.java.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ZhTcPosts {

    private String zh;
    private String en;
    private String ko;
    private String upyn;
    private String imgurl;
    private String enPron;
    private String koPron;

    /**
     * Empty constructor needed for Firebase object deserialization.
     */
    public ZhTcPosts() {
    }

    public ZhTcPosts(String zh, String en, String ko, String upyn, String imgurl) {
        this.zh = zh;
        this.en = en;
        this.ko = ko;
        this.imgurl = imgurl;
        this.upyn = upyn;
        this.enPron = "";
        this.koPron = "";
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("zh", zh);
        result.put("en", en);
        result.put("ko", ko);
        result.put("upyn", upyn);
        result.put("imgurl", imgurl);
        result.put("enPron", enPron);
        result.put("koPron", koPron);
        return result;
    }
}
