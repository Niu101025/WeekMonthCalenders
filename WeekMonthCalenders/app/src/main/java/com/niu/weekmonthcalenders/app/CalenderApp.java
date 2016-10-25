package com.niu.weekmonthcalenders.app;

import android.app.Application;
import android.content.Context;

/**
 * Desp.
 *
 * @author hongbin.niu
 * @version 3.1.1
 * @since 2016-10-21 14:48
 */
public class CalenderApp extends Application {
    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }
}
