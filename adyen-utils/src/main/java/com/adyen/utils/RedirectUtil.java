package com.adyen.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by timon on 11/09/2017.
 */
public final class RedirectUtil {
    private static final String SYSTEM_PACKAGE_NAME = "android";

    /**
     * Determine how a {@link Uri} will be opened.
     *
     * @param context The current {@link Context}.
     * @param uri The {@link Uri} to determine the {@link ResolveResult} for.
     * @return See {@link ResolveResult}.
     */
    @NonNull
    public static ResolveResult determineResolveResult(@NonNull Context context, @Nullable Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));

        PackageManager packageManager = context.getPackageManager();
        ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
        ResolveInfo browserInfo = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
        String resolvedPackageName = resolveInfo != null && resolveInfo.activityInfo != null ? resolveInfo.activityInfo.packageName : null;
        String browserPackageName = browserInfo != null && browserInfo.activityInfo != null ? browserInfo.activityInfo.packageName : null;

        if (resolvedPackageName != null) {
            if (resolvedPackageName.equals(SYSTEM_PACKAGE_NAME)) {
                return new ResolveResult(ResolveType.SYSTEM_COMPONENT, resolveInfo);
            } else if (resolvedPackageName.equals(browserPackageName)) {
                return new ResolveResult(ResolveType.DEFAULT_BROWSER, resolveInfo);
            } else {
                return new ResolveResult(ResolveType.APPLICATION, resolveInfo);
            }
        }

        return new ResolveResult(ResolveType.UNKNOWN, null);
    }

    private RedirectUtil() {
        // Hide utility class constructor.
    }

    /**
     * Describes the way an {@link Intent} will be opened.
     */
    public enum ResolveType {
        /**
         * The {@link Intent} will be opened by an Android system component, e.g. a resolver dialog.
         */
        SYSTEM_COMPONENT,
        /**
         * The {@link Intent} will be opened with the default browser application.
         */
        DEFAULT_BROWSER,
        /**
         * The {@link Intent} will be opened with an application other than the default browser.
         */
        APPLICATION,
        /**
         * It could not be determined how the {@link Intent} will be opened.
         */
        UNKNOWN
    }

    /**
     * Holds the information the resulted from resolving a URL.
     */
    public static final class ResolveResult {
        private ResolveType mResolveType;

        private ResolveInfo mResolveInfo;

        private ResolveResult(@NonNull ResolveType resolveType, @Nullable ResolveInfo resolveInfo) {
            mResolveType = resolveType;
            mResolveInfo = resolveInfo;
        }

        /**
         * @return See {@link ResolveType}.
         */
        @NonNull
        public ResolveType getResolveType() {
            return mResolveType;
        }

        /**
         * @return See {@link ResolveInfo}.
         */
        @NonNull
        public ResolveInfo getResolveInfo() {
            return mResolveInfo;
        }
    }
}
