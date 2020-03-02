package com.kassette.wifip2p;


public class DeviceInfo {
    public String address;
    public String tripName;
    public String password;
    public String username;

    public boolean equals (DeviceInfo x) {
        if (x.address == address){
            return true;
        }
        return false;
    }
}
