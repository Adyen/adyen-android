package com.adyen.ui.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

import com.adyen.ui.R;

public final class IconUtil {

    private static final int CORNER_RADIUS_DP = 3;

    public static String addScaleFactorToIconUrl(@NonNull Context context, @NonNull String url) {
        if (url.contains(".png")) {
            url = url.substring(0, url.indexOf(".png")) + IconUtil.getIosImageScaleFactor(context) + ".png";
        }
        return url;
    }

    private static String getIosImageScaleFactor(@NonNull Context context) {
        final int density = context.getResources().getDisplayMetrics().densityDpi;
        String result = "";
        if (density <= DisplayMetrics.DENSITY_MEDIUM) {
            //do nothing
        } else if (density > DisplayMetrics.DENSITY_MEDIUM && density <= DisplayMetrics.DENSITY_XHIGH) {
            result = "@2x";
        } else if (density > DisplayMetrics.DENSITY_XHIGH) {
            result = "@3x";
        }
        return result;
    }

    public static Drawable resizeRoundCornersAndAddBorder(@NonNull final Context context, final Bitmap bitmap,
                                                          final int targetHeight) {
        return IconUtil.getLayerDrawableWithGrayBorder(context, new BitmapDrawable(context.getResources(),
                IconUtil.roundCorners(IconUtil.resizeBitmap(bitmap, targetHeight),
                        IconUtil.dpToPx(CORNER_RADIUS_DP))));
    }

    private static Bitmap roundCorners(Bitmap bitmap, int radiusPx) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = radiusPx;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private static Bitmap resizeBitmap(Bitmap bitmap, final int targetHeight) {

        final int originalHeight = bitmap.getHeight();
        final int originalWidth = bitmap.getWidth();
        final double ratio = (double) originalWidth / originalHeight;

        final int targetWidth = (int) (targetHeight * ratio);

        final Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);

        return bitmapResized;
    }

    private static LayerDrawable getLayerDrawableWithGrayBorder(@NonNull Context context, Drawable drawable) {
        Drawable[] drawables = new Drawable[2];
        drawables[0] = drawable;
        drawables[1] = getGradientDrawable(context);
        return new LayerDrawable(drawables);
    }

    private static GradientDrawable getGradientDrawable(Context context) {
        GradientDrawable gd = new GradientDrawable();
        gd.setCornerRadius(IconUtil.dpToPx(CORNER_RADIUS_DP));
        gd.setStroke(1, ContextCompat.getColor(context, R.color.black_20_percent_opacity));
        return gd;
    }

    private static int dpToPx(final int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private IconUtil() {
        // Private constructor.
    }
}
