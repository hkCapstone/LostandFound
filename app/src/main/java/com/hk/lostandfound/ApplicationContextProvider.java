package com.hk.lostandfound;

import android.app.Application;
import android.content.Context;

/**
 * Created by David on 5/4/2016.
 */
public class ApplicationContextProvider extends Application {

    /**
     * Keeps a reference of the application context
     */
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        sContext = getApplicationContext();

    }

    /**
     * Returns the application context
     *
     * @return application context
     */
    public static Context getContext() {
        return sContext;
    }

}
