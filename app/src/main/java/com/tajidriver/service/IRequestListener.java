package com.tajidriver.service;

import android.content.Context;

/**
 * Request Listener Interface. It is just to handle the HTTP request error
 */

public interface IRequestListener {

    void onNewToken(String token, Context context);

    void onComplete();

    void onError(String message);
}