package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.Parcelables;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 17/08/2017.
 */
public final class RedirectUtil {
    private static final String RESOLVER_ACTIVITY_PACKAGE_NAME = "android";

    @NonNull
    public static ResolveResult determineResolveResult(@NonNull Context context, @NonNull Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"));

        try {
            PackageManager packageManager = context.getPackageManager();
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
            ResolveInfo browserInfo = packageManager.resolveActivity(browserIntent, PackageManager.MATCH_DEFAULT_ONLY);
            String resolvedPackageName = resolveInfo != null ? resolveInfo.activityInfo.packageName : null;
            String browserPackageName = browserInfo != null ? browserInfo.activityInfo.packageName : null;

            if (resolvedPackageName != null) {
                if (resolvedPackageName.equals(RESOLVER_ACTIVITY_PACKAGE_NAME)) {
                    return new ResolveResult(ResolveType.RESOLVER_ACTIVITY, resolveInfo);
                } else if (resolvedPackageName.equals(browserPackageName)) {
                    return new ResolveResult(ResolveType.DEFAULT_BROWSER, resolveInfo);
                } else {
                    return new ResolveResult(ResolveType.APPLICATION, resolveInfo);
                }
            }
        } catch (Exception e) {
            return new ResolveResult(ResolveType.UNKNOWN, null);
        }

        return new ResolveResult(ResolveType.UNKNOWN, null);
    }

    @NonNull
    public static ResolveResult determineResolveResult(@NonNull Context context, @NonNull String packageName) {
        PackageManager packageManager = context.getPackageManager();
        Intent launchIntent = packageManager.getLaunchIntentForPackage(packageName);

        if (launchIntent != null) {
            ResolveInfo resolveInfo = packageManager.resolveActivity(launchIntent, 0);
            return new ResolveResult(ResolveType.APPLICATION, resolveInfo);
        } else {
            return new ResolveResult(ResolveType.UNKNOWN, null);
        }
    }

    private RedirectUtil() {
        throw new IllegalStateException("No instances.");
    }

    public enum ResolveType {
        RESOLVER_ACTIVITY,
        DEFAULT_BROWSER,
        APPLICATION,
        UNKNOWN
    }

    public static final class ResolveResult implements Parcelable {
        public static final Creator<ResolveResult> CREATOR = new Creator<ResolveResult>() {
            @Override
            public ResolveResult createFromParcel(Parcel in) {
                return new ResolveResult(in);
            }

            @Override
            public ResolveResult[] newArray(int size) {
                return new ResolveResult[size];
            }
        };

        private ResolveType mResolveType;

        private ResolveInfo mResolveInfo;

        public ResolveResult(@NonNull Parcel in) {
            mResolveType = Parcelables.readSerializable(in);
            mResolveInfo = Parcelables.read(in, ResolveInfo.class);
        }

        private ResolveResult(@NonNull ResolveType resolveType, @Nullable ResolveInfo resolveInfo) {
            mResolveType = resolveType;
            mResolveInfo = resolveInfo;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            Parcelables.writeSerializable(out, mResolveType);
            Parcelables.write(out, mResolveInfo);
        }

        @NonNull
        public ResolveType getResolveType() {
            return mResolveType;
        }

        @Nullable
        public ResolveInfo getResolveInfo() {
            return mResolveInfo;
        }
    }
}
