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
}
