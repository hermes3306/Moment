package com.jason.moment.util.db;

import android.provider.BaseColumns;

public final class MyRunContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private MyRunContract() {}

    /* Inner class that defines the table contents */
    public static class E implements BaseColumns {
        public static final String TAB_NAME = "myrun";
        public static final String COL_RUN = "runid";
        public static final String COL_LAT = "lat";
        public static final String COL_LON = "lon";
        public static final String COL_ALT = "alt";
        public static final String COL_DATE = "crdate";
    }
}
