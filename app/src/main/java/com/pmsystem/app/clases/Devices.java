package com.pmsystem.app.clases;

public class Devices {
    private String device;
    private String MAC;


    public Devices() {

    }

    public Devices(String device, String MAC) {
        this.device = device;
        this.MAC = MAC;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }


    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        if(obj instanceof Devices)
        {
            Devices temp = (Devices) obj;
            if(this.device==temp.getDevice() && this.MAC ==temp.getMAC())
                return true;
        }
        return false;

    }
    @Override
    public int hashCode() {
        // TODO Auto-generated method stub

        return (this.device.hashCode() + this.MAC.hashCode());
    }
}
