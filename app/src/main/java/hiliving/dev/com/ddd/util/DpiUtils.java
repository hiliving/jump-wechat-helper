package hiliving.dev.com.ddd.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by huangyong on 2017/12/15.
 */

public class DpiUtils {
    private int width = 0;
    private int height = 0;
    private int dpi = 0;


    public static int[] getMobileInfo(Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int height = metrics.widthPixels;
        int width = metrics.heightPixels;
        int dpi = metrics.densityDpi;
        int[] arr = {width,height,dpi};
        return arr;
    }
    public static float convertPixelToDp(float px, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float px = dp * (metrics.densityDpi / 160f);
        float  dp = px/ (metrics.densityDpi / 160f);
        return dp;
    }

    // 将px值转换为sp值，保证文字大小不变
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
}
