package com.pmsystem.app.clases;

public class Data {
    private String PM25;
    private String PM10;


    public Data(String PM25, String PM10) {
        this.PM25 = PM25;
        this.PM10 = PM10;
    }

    public String getPM25() {
        return PM25;
    }

    public void setPM25(String PM25) {
        this.PM25 = PM25;
    }

    public String getPM10() {
        return PM10;
    }

    public void setPM10(String PM10) {
        this.PM10 = PM10;
    }
}
