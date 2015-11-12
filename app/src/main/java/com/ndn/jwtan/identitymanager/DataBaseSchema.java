package com.ndn.jwtan.identitymanager;

import android.provider.BaseColumns;

public final class DataBaseSchema {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DataBaseSchema() {}

    /* Inner class that defines the table contents */
    public static abstract class IdentityEntry implements BaseColumns {
        public static final String TABLE_NAME = "identities";
        public static final String COLUMN_NAME_IDENTITY = "identity";
        public static final String COLUMN_NAME_CERTIFICATE = "certificate";
    }

    /* Inner class that defines the table contents */
    public static abstract class AppEntry implements BaseColumns {
        public static final String TABLE_NAME = "apps";
        public static final String COLUMN_NAME_IDENTITY = "identity";
        public static final String COLUMN_NAME_APP = "app";
        public static final String COLUMN_NAME_CERTIFICATE = "certificate";
    }

    public static abstract class DeviceEntry implements BaseColumns {
        public static final String TABLE_NAME = "device_ids";
        public static final String COLUMN_NAME_IDENTITY = "identity";
        // The device name is redundant here, as it's also represented in the identity name; adding this here could make query by device name easier...
        public static final String COLUMN_NAME_DEVICE = "device";
        public static final String COLUMN_NAME_CERTIFICATE = "certificate";
    }
}
