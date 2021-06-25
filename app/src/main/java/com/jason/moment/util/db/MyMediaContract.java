package com.jason.moment.util.db;

import android.provider.BaseColumns;

public final class MyMediaContract {
    private MyMediaContract() {}

    /* Inner class that defines the table contents */
    public static class MediaEntry implements BaseColumns {
        public static final String TABLE_NAME               = "media";
        public static final String COLUMN_NAME_NAME         = "name";
        public static final String COLUMN_NAME_MEMO         = "memo";
        public static final String COLUMN_NAME_PLACE        = "place";
        public static final String COLUMN_NAME_ADDRESS      = "address";
        public static final String COLUMN_NAME_GRADE        = "grade";
        public static final String COLUMN_NAME_LATITUDE     = "latitude";
        public static final String COLUMN_NAME_LONGITUDE    = "longitude";
        public static final String COLUMN_NAME_CR_DATETIME  = "cr_datetime";
        public static final String COLUMN_NAME_MO_DATETIME  = "mo_datetime";


    }

}
