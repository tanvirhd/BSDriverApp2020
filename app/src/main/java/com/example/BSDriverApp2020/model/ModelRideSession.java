package com.example.BSDriverApp2020.model;

public class ModelRideSession {
    private String sessionId,driverId,busId;
    private boolean drivingstatus;

    public boolean isDrivingstatus() {
        return drivingstatus;
    }

    public void setDrivingstatus(boolean drivingstatus) {
        this.drivingstatus = drivingstatus;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }
}
