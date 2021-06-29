package com.jason.quote.util;

public class Progress {
    public Progress(int lap, long distM, double mpk, String mpkStr, double time, String timeStr) {
        this.lap = lap;
        this.distM = distM;
        this.mpk = mpk;
        this.timeMs = time;
        this.mpkStr = mpkStr;
        this.timeStr = timeStr;
    }

    public String toString() {
        return "" + lap + ": " + distM + "M  " + mpk + "M/K (double)" + mpkStr + "M/K(str) " + timeStr + "";
    }

    public int lap;
    public long distM;
    public double mpk;
    public String mpkStr;
    public double timeMs;
    public String timeStr;
}