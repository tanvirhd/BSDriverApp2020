package com.example.BSDriverApp2020.model;

public class ModelRequest {
    String userid,username;
    String lat,lang;
    String pickupstatus;
    boolean isrequestAccepted,ispickuprequestRejected;

    public ModelRequest() {
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public boolean isIsrequestAccepted() {
        return isrequestAccepted;
    }

    public void setIsrequestAccepted(boolean isrequestAccepted) {
        this.isrequestAccepted = isrequestAccepted;
    }

    public boolean isIspickuprequestRejected() {
        return ispickuprequestRejected;
    }

    public void setIspickuprequestRejected(boolean ispickuprequestRejected) {
        this.ispickuprequestRejected = ispickuprequestRejected;
    }

    public String getPickupstatus() {
        return pickupstatus;
    }

    public void setPickupstatus(String pickupstatus) {
        this.pickupstatus = pickupstatus;
    }
}
