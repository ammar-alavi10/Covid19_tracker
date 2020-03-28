package com.ammar.myapplication;

public class Global_plotted_coordinates {
    private int channel_id;
    private int status;
    private String username;
    private float latitude;
    private float longitude;
    private String last_fetch;

    public Global_plotted_coordinates(int channel_id, int status, String username, float latitude, float longitude, String last_fetch) {
        this.channel_id = channel_id;
        this.status = status;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.last_fetch = last_fetch;
    }

    public int getChannel_id() {
        return channel_id;
    }

    public void setChannel_id(int channel_id) {
        this.channel_id = channel_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public String getLast_fetch() {
        return last_fetch;
    }

    public void setLast_fetch(String last_fetch) {
        this.last_fetch = last_fetch;
    }
}
