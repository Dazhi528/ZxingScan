package com.dazhi.scan.util;

import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;

/**
 * Created by aaron on 16/8/3.
 */
public final class UtScanDisplay {
    public static int screenWidthPx; //屏幕宽 px
    public static int screenhightPx; //屏幕高 px
    public static float density;//屏幕密度
    public static int densityDPI;//屏幕密度
    public static float screenWidthDip;//  dp单位
    public static float screenHightDip;//  dp单位
    // 批量扫描时需添加此回调
    private static BatchScanCallback batchScanCallback;
    public interface BatchScanCallback {
        void call(String scanCode);
    }

    private UtScanDisplay() {}


    public static void addBatchScanCallback(BatchScanCallback mBatchScanCallback) {
        batchScanCallback = mBatchScanCallback;
    }
    public static BatchScanCallback getBatchScanCallback() {
        return batchScanCallback;
    }

    public static void initDisplayOpinion(Context context) {
        if (context == null) {
            return;
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        UtScanDisplay.density = dm.density;
        UtScanDisplay.densityDPI = dm.densityDpi;
        UtScanDisplay.screenWidthPx = dm.widthPixels;
        UtScanDisplay.screenhightPx = dm.heightPixels;
        UtScanDisplay.screenWidthDip = UtScanDisplay.px2dip(context, dm.widthPixels);
        UtScanDisplay.screenHightDip = UtScanDisplay.px2dip(context, dm.heightPixels);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
