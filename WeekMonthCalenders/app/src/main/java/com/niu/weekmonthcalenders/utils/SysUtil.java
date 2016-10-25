package com.niu.weekmonthcalenders.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * SysUtil - 工具类
 * 主要用于获取、计算系统宽高、单位换算等
 *
 * @author niuhongbin
 * @version 5.1.0
 * @since 2016-07-07 17:00
 */
public class SysUtil {

    /**
     * true or false
     */
    public static final boolean OPEN_SELFSIZE_HEADER_HEIGHT = true;

    private static final int NUM_1000 = 1000;
    private static final int NUM_900 = 900;
    private static final int NUM_700 = 700;
    private static final int NUM_300 = 300;
    private static final int NUM_240 = 240;
    private static final int NUM_15 = 15;
    private static final int NUM_13 = 13;
    private static final float FLOAT_0_5 = 0.5f;

    /**
     * 屏幕宽度
     */
    private static int screenWidth;
    /**
     * 屏幕高度
     */
    private static int screenHeight;
    /**
     * 状态栏高度
     */
    private static int statusHeight;
    /**
     * 状态栏高度
     */
    private static int actionBarHeight;

    private static float densityDpi;

    private static float density;
    private static int sdk_version;

    /**
     * 判断屏幕是否是高密度
     */
    private static boolean largeScreen = true;
    /**
     * 超大屏手机
     */
    private static boolean isXLarge = false;
    /**
     * 1080p
     */
    private static boolean isXXLarge = false;

    private static int MOVIE_HEADER_HEIGHT = 0;

    /**
     * init
     *
     * @param context Context
     */
    public static void init(Context context) {

        if (null == context || (screenWidth > 0 && screenHeight > 0)) {
            return;
        }
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metric);

        screenWidth = metric.widthPixels; // 屏幕宽度（像素）
        screenHeight = metric.heightPixels; // 屏幕高度（像素）

        density = metric.density; // 屏幕密度（0.75 / 1.0 / 1.5）
        densityDpi = metric.densityDpi; // 屏幕密度DPI（120 / 160 / 240）
        if (densityDpi <= NUM_240) {
            largeScreen = false;
        } else {
            largeScreen = true;
        }

        if (screenWidth > NUM_700 && screenHeight > NUM_900) {
            isXLarge = true;
        }

        if (screenWidth > NUM_1000) {
            isXXLarge = true;
        }

    }

    /**
     * getActionBarHeight
     *
     * @param context Context
     * @return int
     */
    public static int getActionBarHeight(Context context) {
        if (actionBarHeight == 0 && null != context) {
            TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[]{
                    android.R.attr.actionBarSize
            });
            actionBarHeight = (int) actionbarSizeTypedArray.getDimension(0, 0);
            actionbarSizeTypedArray.recycle();
        }
        return actionBarHeight;
    }

    /**
     * getSysActionBarHeight
     *
     * @param context Context
     * @return int
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static int getSysActionBarHeight(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            TypedValue tv = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true);
            result = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
        }
        return result;
    }

    /**
     * getScreenWidth
     *
     * @param context Context
     * @return int
     */
    public static int getScreenWidth(Context context) {
        init(context);
        return screenWidth;
    }

    /**
     * getScreenHeight
     *
     * @param context Context
     * @return int
     */
    public static int getScreenHeight(Context context) {
        init(context);
        return screenHeight;
    }

    /**
     * getDensityDpi
     *
     * @param context Context
     * @return float
     */
    public static float getDensityDpi(Context context) {
        init(context);
        return densityDpi;
    }

    /**
     * getStatusHeight
     *
     * @param context Context
     * @return int
     */
    public static int getStatusHeight(Context context) {
        if (statusHeight == 0) {
            Class<?> c = null;
            Object obj = null;
            Field field = null;
            int x = 0;
            try {
                c = Class.forName("com.android.internal.R$dimen");
                obj = c.newInstance();
                field = c.getField("status_bar_height");
                x = Integer.parseInt(field.get(obj).toString());
                statusHeight = context.getResources().getDimensionPixelSize(x);
            } catch (Exception e1) {
                e1.printStackTrace();
                Rect frame = new Rect();
                ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

                statusHeight = frame.top;
            }
        }
        return statusHeight;
    }

    /**
     * getStatusBarHeight
     *
     * @param context Context
     * @return int
     */
    public static int getStatusBarHeight(Context context) {
        Resources res = context.getResources();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return 0;
        }
        int result = 0;
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * isHighDensity
     *
     * @param context Context
     * @return true or false
     */
    public static boolean isHighDensity(Context context) {
        init(context);
        return density > 2.0;
    }

    /**
     * isMiddleDensity
     *
     * @param context Context
     * @return true or false
     */
    public static boolean isMiddleDensity(Context context) {
        init(context);
        return density >= 2.0;
    }

    /**
     * @param context Context
     * @return true or false
     */
    public static boolean isLargeScreen(Context context) {
        init(context);
        return largeScreen;
    }

    /**
     * @param context Context
     * @return true or false
     */
    public static boolean isXLargeScreen(Context context) {
        init(context);
        return isXLarge;
    }

    /**
     * @param context Context
     * @return true or false
     */
    public static boolean isXXLargeScreen(Context context) {
        init(context);
        return isXXLarge;
    }

    /**
     * getSdkVersion
     *
     * @param context Context
     * @return int
     */
    public static int getSdkVersion(Context context) {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     *
     * @param context Context
     * @param dpValue float
     * @return int
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + FLOAT_0_5);
    }

    /**
     * sp2px
     *
     * @param context Context
     * @param spValue float
     * @return int
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + FLOAT_0_5);
    }

}
