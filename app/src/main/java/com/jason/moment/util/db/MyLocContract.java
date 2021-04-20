package com.jason.moment.util.db;

import android.provider.BaseColumns;

public final class MyLocContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MyLocContract() {}

    /* Inner class that defines the table contents */
    public static class LocEntry implements BaseColumns {
        public static final String TABLE_NAME = "myloc";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITIDE = "longitude";
        public static final String COLUMN_NAME_CRDATE = "crdate";
        public static final String COLUMN_NAME_CRTIME = "crtime";
    }
}
