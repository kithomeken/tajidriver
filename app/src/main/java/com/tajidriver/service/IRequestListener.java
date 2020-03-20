package com.tajidriver.service;

import android.content.Context;

/**
 * Request Listener Interface. It is just to handle the HTTP request error
 */

public interface IRequestListener {

    void onNewToken(Context context, String token);

    void onTokenUpdate(Context context, String token);

    void onComplete();

    void onError(String message);
}