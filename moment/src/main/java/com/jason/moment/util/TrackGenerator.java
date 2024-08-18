package com.jason.moment.util;

import java.util.ArrayList;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;

public class TrackGenerator {
    private static final double EARTH_RADIUS = 6371000; // Earth's radius in meters
    private static final double SEOUL_LAT = 37.5665; // Latitude of Seoul
    private static final double SEOUL_LON = 126.9780; // Longitude of Seoul
    private static final double AVERAGE_SPEED = 2.77; // Average running speed in m/s (10 km/h)

    public static ArrayList<MyActivity> generateRandomRealisticTrack() {
        Random random = new Random();

        // Generate random number of points between 100 and 1000
        int pointCount = random.nextInt(901) + 100; // 901 = 1000 - 100 + 1

        // Generate random distance between 1 and 10 km
        double distanceKm = 1 + random.nextDouble() * 9; // 1 to 10 km

        return generateRealisticTrack(pointCount, distanceKm);
    }

    private static ArrayList<MyActivity> generateRealisticTrack(int pointCount, double distanceKm) {
        ArrayList<MyActivity> activities = new ArrayList<>();
        Random random = new Random();

        // Generate start point within Seoul
        double centerLat = SEOUL_LAT + (random.nextDouble() - 0.5) * 0.1; // +/- 0.05 degrees
        double centerLon = SEOUL_LON + (random.nextDouble() - 0.5) * 0.1;

        double totalDistance = distanceKm * 1000; // Convert to meters
        double coveredDistance = 0;
        Date startTime = new Date();

        while (coveredDistance < totalDistance) {
            double remainingDistance = totalDistance - coveredDistance;
            double segmentDistance = Math.min(remainingDistance, 50 + random.nextDouble() * 100); // 50-150m segments

            double bearing = random.nextDouble() * 2 * Math.PI;
            double[] newPoint = calculateNewPoint(centerLat, centerLon, segmentDistance, bearing);

            // Add some noise to make it more realistic
            newPoint[0] += (random.nextDouble() - 0.5) * 0.00005;
            newPoint[1] += (random.nextDouble() - 0.5) * 0.00005;

            // Round to 6 decimal places
            double lat = Math.round(newPoint[0] * 1e6) / 1e6;
            double lon = Math.round(newPoint[1] * 1e6) / 1e6;

            // Calculate time based on average speed and add some variation
            double timeSeconds = (segmentDistance / AVERAGE_SPEED) * (0.9 + random.nextDouble() * 0.2);
            Date currentTime = new Date(startTime.getTime() + (long)(coveredDistance / AVERAGE_SPEED * 1000));

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

            activities.add(new MyActivity(lat, lon, dateFormat.format(currentTime), timeFormat.format(currentTime)));

            coveredDistance += segmentDistance;
            centerLat = lat;
            centerLon = lon;

            // Break if we've generated enough points
            if (activities.size() >= pointCount) break;
        }

        return activities;
    }

    private static double[] calculateNewPoint(double lat, double lon, double distance, double bearing) {
        double angularDistance = distance / EARTH_RADIUS;
        double lat1 = Math.toRadians(lat);
        double lon1 = Math.toRadians(lon);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDistance) +
                Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearing));
        double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));

        return new double[]{Math.toDegrees(lat2), Math.toDegrees(lon2)};
    }
}