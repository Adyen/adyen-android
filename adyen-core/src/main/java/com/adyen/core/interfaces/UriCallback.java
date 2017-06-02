package com.adyen.core.interfaces;

import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * The callback for notifying the SDK with URI in case of redirect.
 */
public interface UriCallback {
    /**
     * The callback for client application to call when the return URI is received.
     * @param uri return URI that is received via onNewIntent method.
     */
    void completionWithUri(@NonNull Uri uri);
}
