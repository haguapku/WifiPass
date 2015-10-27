package com.haguapku.wificlient.bean;

import java.io.Serializable;

/**
 * Created by MarkYoung on 15/10/21.
 */
public class DmWifiInfo implements Serializable {

    private static final long serialVersionUID = -3602529583266444008L;
    private String BSSID;
    private String SSID;
    private String capabilities;
    private int level;
    private int frequency;
    private int describeContents;
    private int type;
    private String pwd;
    private long timestamp;

    public DmWifiInfo() {

    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setDescribeContents(int describeContents) {
        this.describeContents = describeContents;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public int getLevel() {
        return level;
    }

    public int getFrequency() {
        return frequency;
    }

    public int getDescribeContents() {
        return describeContents;
    }

    public int getType() {
        return type;
    }

    public String getPwd() {
        return pwd;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
