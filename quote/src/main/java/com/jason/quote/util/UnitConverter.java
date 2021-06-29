package com.jason.quote.util;

public class UnitConverter {
    public static float convertSecondsToHours(int durationInSeconds) {
        double d = (double)durationInSeconds / 60.0 / 60.0;
        return (float)d;
    }

    public static float convertKilometersToMiles(float kmTravelled) {
        double d = (double)kmTravelled * 1000.0;
        return (float)d;
    }
}
