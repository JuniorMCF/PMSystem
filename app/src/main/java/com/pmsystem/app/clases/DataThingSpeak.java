package com.pmsystem.app.clases;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DataThingSpeak implements Parcelable {
    @SerializedName("field1")
    @Expose
    private String latitud;

    @SerializedName("field2")
    @Expose
    private String longitud;

    @SerializedName("field3")
    @Expose
    private String pm25;

    @SerializedName("field4")
    @Expose
    private String pm10;

    @SerializedName("field5")
    @Expose
    private String mac;

    public DataThingSpeak() {
    }

    protected DataThingSpeak(Parcel in) {
        latitud = in.readString();
        longitud = in.readString();
        pm25 = in.readString();
        pm10 = in.readString();
        mac = in.readString();
    }

    public static final Creator<DataThingSpeak> CREATOR = new Creator<DataThingSpeak>() {
        @Override
        public DataThingSpeak createFromParcel(Parcel in) {
            return new DataThingSpeak(in);
        }

        @Override
        public DataThingSpeak[] newArray(int size) {
            return new DataThingSpeak[size];
        }
    };

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getPm25() {
        return pm25;
    }

    public void setPm25(String pm25) {
        this.pm25 = pm25;
    }

    public String getPm10() {
        return pm10;
    }

    public void setPm10(String pm10) {
        this.pm10 = pm10;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(latitud);
        dest.writeString(longitud);
        dest.writeString(pm25);
        dest.writeString(pm10);
        dest.writeString(mac);
    }
}
