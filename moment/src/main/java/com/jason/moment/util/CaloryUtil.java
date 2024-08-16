package com.jason.moment.util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

public class CaloryUtil {
    private static final String TAG = "CaloryUtil";
    /**
     * Calculated the energy expenditure for an activity. Adapted from the following website https://sites.google.com/site/compendiumofphysicalactivities/corrected-mets
     *
     * @param height               The height in metres.
     * @param age                  The date of birth.
     * @param weight               The weight of the user.
     * @param gender               The gender of the user.
     * @param durationInSeconds    The duration of the activity in seconds.
     * @param stepsTaken           The steps taken.
     * @param strideLengthInMetres The stride length of the user
     * @return The number of calories burnt (kCal)
     */
    public static float calculateEnergyExpenditure(float height, Date age, float weight, int gender, int durationInSeconds, int stepsTaken, float strideLengthInMetres) {

        float ageCalculated = getAgeFromDateOfBirth(age);
        Log.d(TAG, "ageCalculated:" + ageCalculated);

        float harrisBenedictRmR = convertKilocaloriesToMlKmin(harrisBenedictRmr(gender, weight, ageCalculated,   convertMetresToCentimetre(height)), weight);
        Log.d(TAG, "harrisBenedictRmR:" + harrisBenedictRmR);
        float kmTravelled = calculateDistanceTravelledInKM(stepsTaken, strideLengthInMetres);
        Log.d(TAG, "kmTravelled:" + kmTravelled);

        float hours = UnitConverter.convertSecondsToHours(durationInSeconds);
        Log.d(TAG, "hours:" + hours);
        float speedInMph = UnitConverter.convertKilometersToMiles(kmTravelled) / hours;
        Log.d(TAG, "speedInMph:" + speedInMph);
        float metValue = getMetForActivity(speedInMph);
        Log.d(TAG, "metValue:" + metValue);

        float constant = 3.5f;

        float correctedMets = metValue * (constant / harrisBenedictRmR);
        Log.d(TAG, "correctedMets:" + correctedMets);

        return correctedMets * hours * weight;
    }

    public static float calculateEnergyExpenditure(float kmTravelled, int durationInSeconds) {
        Date age            = Config._age;
        int gender          = Config._gender;
        float ageCalculated = getAgeFromDateOfBirth(age);
        float height        = Config._height;
        float weight        = Config._weight;

        float harrisBenedictRmR = convertKilocaloriesToMlKmin(harrisBenedictRmr(gender, weight, ageCalculated,  convertMetresToCentimetre(height)), weight);
        float hours = UnitConverter.convertSecondsToHours(durationInSeconds);
        float speedInMph = UnitConverter.convertKilometersToMiles(kmTravelled) / hours;
        float metValue = getMetForActivity(speedInMph);
        float constant = 3.5f;
        float correctedMets = metValue * (constant / harrisBenedictRmR);
        return correctedMets * hours * weight;
    }
    /**
     * Gets a users age from a date. Only takes into account years.
     *
     * @param age The date of birth.
     * @return The age in years.
     */
    private static float getAgeFromDateOfBirth(Date age) {
        Calendar currentDate = Calendar.getInstance();
        Calendar dateOfBirth = Calendar.getInstance();
        dateOfBirth.setTime(age);

        if (dateOfBirth.after(currentDate)) {
            throw new IllegalArgumentException("Can't be born in the future");
        }
        int currentYear = currentDate.get(Calendar.YEAR);
        int dateOfBirthYear = dateOfBirth.get(Calendar.YEAR);
        int age2 = currentYear - dateOfBirthYear;
        int currentMonth = currentDate.get(Calendar.MONTH);
        int dateOfBirthMonth = dateOfBirth.get(Calendar.MONTH);

        if (dateOfBirthMonth > currentMonth) {
            age2--;
        } else if (currentMonth == dateOfBirthMonth) {
            int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
            int dateOfBirthDay = dateOfBirth.get(Calendar.DAY_OF_MONTH);
            if (dateOfBirthDay > currentDay) {
                age2--;
            }
        }
        return age2;
    }
    public static float convertKilocaloriesToMlKmin(float kilocalories, float weightKgs) {
        float kcalMin = kilocalories / 1440;
        kcalMin /= 5;

        return ((kcalMin / (weightKgs)) * 1000);
    }
    public static float convertMetresToCentimetre(float metres) {
        return metres * 100;
    }
    public static float calculateDistanceTravelledInKM(int stepsTaken, float entityStrideLength) {
        return (((float) stepsTaken * entityStrideLength) / 1000);
    }
    /**
     * Gets the MET value for an activity. Based on https://sites.google.com/site/compendiumofphysicalactivities/Activity-Categories/walking .
     *
     * @param speedInMph The speed in miles per hour
     * @return The met value.
     */
    private static float getMetForActivity(float speedInMph) {
        if (speedInMph < 2.0) {
            return 2.0f;
        } else if (Float.compare(speedInMph, 2.0f) == 0) {
            return 2.8f;
        } else if (Float.compare(speedInMph, 2.0f) > 0 && Float.compare(speedInMph, 2.7f) <= 0) {
            return 3.0f;
        } else if (Float.compare(speedInMph, 2.8f) > 0 && Float.compare(speedInMph, 3.3f) <= 0) {
            return 3.5f;
        } else if (Float.compare(speedInMph, 3.4f) > 0 && Float.compare(speedInMph, 3.5f) <= 0) {
            return 4.3f;
        } else if (Float.compare(speedInMph, 3.5f) > 0 && Float.compare(speedInMph, 4.0f) <= 0) {
            return 5.0f;
        } else if (Float.compare(speedInMph, 4.0f) > 0 && Float.compare(speedInMph, 4.5f) <= 0) {
            return 7.0f;
        } else if (Float.compare(speedInMph, 4.5f) > 0 && Float.compare(speedInMph, 5.0f) <= 0) {
            return 8.3f;
        } else if (Float.compare(speedInMph, 5.0f) > 0) {
            return 9.8f;
        }
        return 0;
    }

    /**
     * Calculates the Harris Benedict RMR value for an entity. Based on above calculation for Com
     *
     * @param gender   Users gender.
     * @param weightKg Weight in Kg.
     * @param age      Age in years.
     * @param heightCm Height in CM.
     * @return Harris benedictRMR value.
     */
    private static float harrisBenedictRmr(int gender, float weightKg, float age, float heightCm) {
        if (gender == Config.GENDER_FEMALE) {
            return 655.0955f + (1.8496f * heightCm) + (9.5634f * weightKg) - (4.6756f * age);
        } else {
            return 66.4730f + (5.0033f * heightCm) + (13.7516f * weightKg) - (6.7550f * age);
        }
    }
}
