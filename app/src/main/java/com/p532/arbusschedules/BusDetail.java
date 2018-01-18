package com.p532.arbusschedules;

/**
 * Created by sudhanshu on 12/4/17.
 */

public class BusDetail {

    private String busNumber;
    private String busTiming;

    public BusDetail(){

    }

    public BusDetail(String busNumber, String busTiming){
        this.busNumber = busNumber;
        this.busTiming = busTiming;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getBusTiming() {
        return busTiming;
    }

    public void setBusTiming(String busTiming) {
        this.busTiming = busTiming;
    }
}
