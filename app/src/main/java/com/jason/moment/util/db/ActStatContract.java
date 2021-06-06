package com.jason.moment.util.db;

import android.provider.BaseColumns;

public final class ActStatContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ActStatContract() {}

    /* Inner class that defines the table contents */
    public static class LocEntry implements BaseColumns {
        public static final String TABLE_NAME               = "actstat";
        public static final String COLUMN_NAME_NAME         = "name";
        public static final String COLUMN_NAME_DIST         = "dist";
        public static final String COLUMN_NAME_DURATION     = "duration";
        public static final String COLUMN_NAME_MINPKM       = "minpkm";
        public static final String COLUMN_NAME_CAL          = "cal";
    }
}
