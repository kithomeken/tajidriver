package com.tajidriver.configuration;


import com.google.android.gms.maps.model.LatLng;

public class TajiCabs {
    public final static String PASSENGER_DETAILS = "PASSENGER_DETAILS";

    public static String EMAIL;
    public static String NAMES;
    public static String IDNUM;
    public static String PHONE;

    public final static String GOOGLE_API = "AIzaSyAxLhVQdc7JXdSXcG5L_-fgwh_WcDtAeMY";

    public static LatLng ORIG_LTNG = null;
    public static String ORIG_NAME = null;

    public static LatLng DEST_LTNG = null;
    public static String DEST_NAME = null;

    public static final int DEFAULT_ZOOM = 18;
    public static final int REQUEST_LOCATION = 199;

    public static final String TOPIC_GLOBAL = "global";

    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";

    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

    public static final String SHARED_PREF = "ah_firebase";
}
